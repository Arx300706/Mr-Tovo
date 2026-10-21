package com.mrtovo.service;

import com.mrtovo.dto.CandidatureForm;
import com.mrtovo.dto.PlanificationForm;
import com.mrtovo.entity.*;
import com.mrtovo.entity.reference.StatutCandidature;
import com.mrtovo.entity.reference.TypeCandidature;
import com.mrtovo.exception.RegleMetierException;
import com.mrtovo.exception.RessourceIntrouvableException;
import com.mrtovo.repository.CandidatureRepository;
import com.mrtovo.repository.CompteRepository;
import com.mrtovo.repository.EmployeRepository;
import com.mrtovo.security.UtilisateurConnecte;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Cycle de vie d'une candidature :
 * cree -> selectionne (dates fixées) -> teste techniquement -> entretien ok -> succes ; rejete possible avant succes.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CandidatureService {

    /** Statuts depuis lesquels la RH peut encore rejeter une candidature. */
    private static final Set<String> STATUTS_EN_COURS = Set.of(StatutCandidature.CREE, StatutCandidature.SELECTIONNE,
            StatutCandidature.TESTE_TECHNIQUEMENT, StatutCandidature.ENTRETIEN_OK);

    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");

    private final CandidatureRepository candidatureRepository;
    private final CompteRepository compteRepository;
    private final EmployeRepository employeRepository;
    private final OffreService offreService;
    private final ReferenceService references;
    private final TraceService traceService;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Candidature trouver(Integer id) {
        return candidatureRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Candidature introuvable"));
    }

    /** Candidature appartenant au candidat connecté (sinon 404). */
    @Transactional(readOnly = true)
    public Candidature trouverPourCandidat(Integer id, UtilisateurConnecte u) {
        Candidature c = trouver(id);
        if (!Objects.equals(c.getCompte().getId(), u.getId())) {
            throw new RessourceIntrouvableException("Candidature introuvable");
        }
        return c;
    }

    @Transactional(readOnly = true)
    public List<Candidature> listerParStatut(String statut) {
        return statut == null || statut.isBlank()
                ? candidatureRepository.findAllByOrderByDateCandidatureDesc()
                : candidatureRepository.findByStatutLibelleOrderByDateCandidatureDesc(statut);
    }

    @Transactional(readOnly = true)
    public List<Candidature> listerPourCompte(Integer idCompte) {
        return candidatureRepository.findByCompteIdOrderByDateCandidatureDesc(idCompte);
    }

    @Transactional(readOnly = true)
    public List<Candidature> listerSuccesSansProposition() {
        return candidatureRepository.findSuccesSansProposition();
    }

    // ---------------------------------------------------------------- Dépôt (public / candidat)

    /**
     * Enregistre la candidature. Si le visiteur n'est pas connecté comme candidat, son compte est créé
     * (ou retrouvé si l'e-mail existe déjà et que le mot de passe est correct).
     */
    public Candidature postuler(Integer idOffre, CandidatureForm f, UtilisateurConnecte u) {
        OffreEmploi offre = offreService.trouver(idOffre);
        if (!offre.estOuverte()) {
            throw new RegleMetierException("Cette offre n'accepte plus de candidatures");
        }
        TypeCandidature type = references.parId(TypeCandidature.class, f.getIdTypeCandidature());
        if (type == null) {
            throw new RegleMetierException("Type de candidature invalide");
        }

        Compte compte = (u != null && u.estCandidat())
                ? compteRepository.findById(u.getId()).orElseThrow()
                : compteDuFormulaire(f);
        if (compte.getId() != null && candidatureRepository.existsByCompteIdAndOffreId(compte.getId(), idOffre)) {
            throw new RegleMetierException("Vous avez déjà postulé à cette offre");
        }

        Candidature c = new Candidature();
        c.setCompte(compteRepository.save(compte));
        c.setOffre(offre);
        c.setDepartement(offre.getDepartement());
        c.setEmploye(employeLie(type, f.getMatricule()));
        c.setTypeCandidature(type);
        c.setTypeContrat(offre.getBesoin().getTypeContrat());
        c.setDiplome(f.getDiplome());
        c.setAnneesExperience(f.getAnneesExperience());
        c.setLettreMotivation(f.getLettreMotivation());
        c.setStatut(references.parLibelle(StatutCandidature.class, StatutCandidature.CREE));
        candidatureRepository.save(c);
        traceService.tracer(HistoriqueStatut.CANDIDATURE, c.getId(), null, StatutCandidature.CREE, u, null);
        notificationService.notifierCompte(c.getCompte(),
                "Votre candidature à l'offre « " + offre.getIntitule() + " » a bien été enregistrée.",
                "/candidat/candidatures/" + c.getId());
        return c;
    }

    private Compte compteDuFormulaire(CandidatureForm f) {
        String email = f.getEmail() == null ? "" : f.getEmail().trim().toLowerCase();
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new RegleMetierException("Adresse e-mail invalide");
        }
        if (f.getMotDePasse() == null || f.getMotDePasse().length() < 4) {
            throw new RegleMetierException("Le mot de passe doit contenir au moins 4 caractères");
        }
        Compte existant = compteRepository.findByEmailIgnoreCase(email).orElse(null);
        if (existant != null) {
            if (!passwordEncoder.matches(f.getMotDePasse(), existant.getMotDePasse())) {
                throw new RegleMetierException("Un compte existe déjà avec cet e-mail : saisissez son mot de passe ou connectez-vous");
            }
            return existant;
        }
        if (estVide(f.getNom()) || estVide(f.getPrenom())) {
            throw new RegleMetierException("Le nom et le prénom sont obligatoires");
        }
        if (!f.getMotDePasse().equals(f.getConfirmation())) {
            throw new RegleMetierException("La confirmation du mot de passe ne correspond pas");
        }
        Compte compte = new Compte();
        compte.setEmail(email);
        compte.setMotDePasse(passwordEncoder.encode(f.getMotDePasse()));
        compte.setNom(f.getNom().trim());
        compte.setPrenom(f.getPrenom().trim());
        compte.setTelephone(f.getTelephone());
        compte.setDateNaissance(f.getDateNaissance());
        compte.setSexe(f.getSexe());
        compte.setAdresse(f.getAdresse());
        return compte;
    }

    /** Employé : matricule obligatoire et en poste. Ancienne relation : matricule facultatif. */
    private Employe employeLie(TypeCandidature type, String matricule) {
        String m = matricule == null ? "" : matricule.trim().toUpperCase();
        switch (type.getLibelle()) {
            case TypeCandidature.EMPLOYE -> {
                Employe e = employeRepository.findByMatricule(m)
                        .orElseThrow(() -> new RegleMetierException("Matricule inconnu : un employé doit saisir son matricule"));
                if (e.getPosteActuel() == null) {
                    throw new RegleMetierException("Ce matricule n'a pas de poste actuel : choisissez « ancienne relation »");
                }
                return e;
            }
            case TypeCandidature.ANCIENNE_RELATION -> {
                if (m.isEmpty()) {
                    return null;
                }
                return employeRepository.findByMatricule(m)
                        .orElseThrow(() -> new RegleMetierException("Matricule inconnu"));
            }
            default -> {
                return null;
            }
        }
    }

    // ---------------------------------------------------------------- Traitement RH

    /** Sélectionne une candidature « cree » (ou replanifie une candidature déjà sélectionnée). */
    public void planifier(Integer id, PlanificationForm f, UtilisateurConnecte u) {
        Candidature c = trouver(id);
        String statut = c.getStatut().getLibelle();
        if (!StatutCandidature.CREE.equals(statut) && !StatutCandidature.SELECTIONNE.equals(statut)) {
            throw new RegleMetierException("Seule une candidature « cree » ou « selectionne » peut être planifiée");
        }
        c.setDateTestTechnique(f.getDateTestTechnique());
        c.setDateEntretien(f.getDateEntretien());
        if (f.getCommentaire() != null && !f.getCommentaire().isBlank()) {
            c.setCommentaireRh(f.getCommentaire());
        }
        if (StatutCandidature.CREE.equals(statut)) {
            changerStatut(c, StatutCandidature.SELECTIONNE, u, f.getCommentaire());
        } else {
            traceService.tracer(HistoriqueStatut.CANDIDATURE, c.getId(), statut, statut, u, "Dates replanifiées");
        }
        notificationService.notifierCompte(c.getCompte(),
                "Votre candidature « " + c.getOffre().getIntitule() + " » est sélectionnée. Test technique le "
                        + FORMAT_DATE.format(f.getDateTestTechnique()) + ", entretien le "
                        + FORMAT_DATE.format(f.getDateEntretien()) + ".",
                "/candidat/candidatures/" + c.getId());
    }

    public void marquerTeste(Integer id, UtilisateurConnecte u) {
        avancer(id, StatutCandidature.SELECTIONNE, StatutCandidature.TESTE_TECHNIQUEMENT, u);
    }

    public void marquerEntretienOk(Integer id, UtilisateurConnecte u) {
        avancer(id, StatutCandidature.TESTE_TECHNIQUEMENT, StatutCandidature.ENTRETIEN_OK, u);
    }

    public void marquerSucces(Integer id, UtilisateurConnecte u) {
        Candidature c = avancer(id, StatutCandidature.ENTRETIEN_OK, StatutCandidature.SUCCES, u);
        notificationService.notifierCompte(c.getCompte(),
                "Félicitations ! Vous avez réussi les épreuves pour « " + c.getOffre().getIntitule()
                        + " ». Une proposition d'emploi va vous être envoyée.",
                "/candidat/candidatures/" + c.getId());
    }

    public void rejeter(Integer id, String commentaire, UtilisateurConnecte u) {
        Candidature c = trouver(id);
        if (!STATUTS_EN_COURS.contains(c.getStatut().getLibelle())) {
            throw new RegleMetierException("Cette candidature ne peut plus être rejetée");
        }
        c.setCommentaireRh(commentaire);
        changerStatut(c, StatutCandidature.REJETE, u, commentaire);
        notificationService.notifierCompte(c.getCompte(),
                "Votre candidature « " + c.getOffre().getIntitule() + " » n'a pas été retenue.",
                "/candidat/candidatures/" + c.getId());
    }

    private Candidature avancer(Integer id, String attendu, String nouveau, UtilisateurConnecte u) {
        Candidature c = trouver(id);
        if (!attendu.equals(c.getStatut().getLibelle())) {
            throw new RegleMetierException("Action impossible : la candidature est au statut « " + c.getStatut().getLibelle() + " »");
        }
        changerStatut(c, nouveau, u, null);
        return c;
    }

    private void changerStatut(Candidature c, String nouveau, UtilisateurConnecte u, String commentaire) {
        String ancien = c.getStatut().getLibelle();
        c.setStatut(references.parLibelle(StatutCandidature.class, nouveau));
        traceService.tracer(HistoriqueStatut.CANDIDATURE, c.getId(), ancien, nouveau, u, commentaire);
    }

    private static boolean estVide(String s) {
        return s == null || s.isBlank();
    }
}
