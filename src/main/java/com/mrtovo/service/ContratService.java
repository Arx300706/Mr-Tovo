package com.mrtovo.service;

import com.mrtovo.entity.*;
import com.mrtovo.entity.reference.StatutContrat;
import com.mrtovo.entity.reference.StatutProposition;
import com.mrtovo.exception.RegleMetierException;
import com.mrtovo.exception.RessourceIntrouvableException;
import com.mrtovo.repository.ContratRepository;
import com.mrtovo.repository.EmployeRepository;
import com.mrtovo.repository.HistoriqueEmployeRepository;
import com.mrtovo.security.UtilisateurConnecte;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Contrat : cree (RH) -> signe (candidat) -> valide (RH, embauche) ; annule possible avant validation.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ContratService {

    private final ContratRepository contratRepository;
    private final EmployeRepository employeRepository;
    private final HistoriqueEmployeRepository historiqueEmployeRepository;
    private final PropositionService propositionService;
    private final OffreService offreService;
    private final ReferenceService references;
    private final TraceService traceService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Contrat trouver(Integer id) {
        return contratRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Contrat introuvable"));
    }

    @Transactional(readOnly = true)
    public Contrat trouverPourCandidat(Integer id, UtilisateurConnecte u) {
        Contrat c = trouver(id);
        if (!Objects.equals(c.getCandidature().getCompte().getId(), u.getId())) {
            throw new RessourceIntrouvableException("Contrat introuvable");
        }
        return c;
    }

    @Transactional(readOnly = true)
    public List<Contrat> listerParStatut(String statut) {
        return statut == null || statut.isBlank()
                ? contratRepository.findAllByOrderByDateModificationDesc()
                : contratRepository.findByStatutLibelleOrderByDateModificationDesc(statut);
    }

    @Transactional(readOnly = true)
    public List<Contrat> listerPourCompte(Integer idCompte) {
        return contratRepository.findByCandidatureCompteIdOrderByDateCreationDesc(idCompte);
    }

    @Transactional(readOnly = true)
    public Contrat pourProposition(Integer idProposition) {
        return contratRepository.findByPropositionId(idProposition).orElse(null);
    }

    // ---------------------------------------------------------------- RH : génération

    public Contrat generer(Integer idProposition, UtilisateurConnecte u) {
        Proposition p = propositionService.trouver(idProposition);
        if (!StatutProposition.VALIDE.equals(p.getStatut().getLibelle())) {
            throw new RegleMetierException("Le contrat ne peut être généré que pour une proposition validée");
        }
        if (contratRepository.findByPropositionId(idProposition).isPresent()) {
            throw new RegleMetierException("Un contrat existe déjà pour cette proposition");
        }
        Contrat c = new Contrat();
        c.setProposition(p);
        c.setCandidature(p.getCandidature());
        c.setPoste(p.getPoste());
        c.setTypeContrat(p.getTypeContrat());
        c.setSalaire(p.getSalaire());
        c.setDateDebut(p.getDateCommencement());
        c.setDateFin(p.getDateFin());
        c.setStatut(references.parLibelle(StatutContrat.class, StatutContrat.CREE));
        contratRepository.save(c);
        traceService.tracer(HistoriqueStatut.CONTRAT, c.getId(), null, StatutContrat.CREE, u, null);
        notificationService.notifierCompte(c.getCandidature().getCompte(),
                "Votre contrat de travail pour « " + p.getOffre().getIntitule() + " » est prêt : vous pouvez le signer.",
                "/candidat/contrats/" + c.getId());
        return c;
    }

    // ---------------------------------------------------------------- Candidat

    public void signerParCandidat(Integer id, UtilisateurConnecte u) {
        Contrat c = trouverPourCandidat(id, u);
        verifierStatut(c, StatutContrat.CREE);
        c.setDateSignatureCandidat(LocalDateTime.now());
        changerStatut(c, StatutContrat.SIGNE, u, "Signé par le candidat");
    }

    public void annulerParCandidat(Integer id, UtilisateurConnecte u) {
        Contrat c = trouverPourCandidat(id, u);
        verifierStatut(c, StatutContrat.CREE);
        changerStatut(c, StatutContrat.ANNULE, u, "Annulé par le candidat");
    }

    // ---------------------------------------------------------------- RH : validation / annulation

    public void annulerParRh(Integer id, UtilisateurConnecte u) {
        Contrat c = trouver(id);
        String statut = c.getStatut().getLibelle();
        if (!StatutContrat.CREE.equals(statut) && !StatutContrat.SIGNE.equals(statut)) {
            throw new RegleMetierException("Seul un contrat « cree » ou « signe » peut être annulé");
        }
        changerStatut(c, StatutContrat.ANNULE, u, "Annulé par la RH");
        notificationService.notifierCompte(c.getCandidature().getCompte(),
                "Votre contrat pour « " + c.getCandidature().getOffre().getIntitule() + " » a été annulé par l'entreprise.",
                "/candidat/contrats/" + c.getId());
    }

    /** Signature de la RH : le contrat devient valide et le candidat devient employé. */
    public Employe validerParRh(Integer id, UtilisateurConnecte u) {
        Contrat c = trouver(id);
        verifierStatut(c, StatutContrat.SIGNE);
        c.setDateSignatureRh(LocalDateTime.now());
        changerStatut(c, StatutContrat.VALIDE, u, "Signé par la RH");
        Employe e = embaucher(c, u);

        notificationService.notifierCompte(c.getCandidature().getCompte(),
                "Bienvenue ! Votre contrat est validé. Votre matricule est " + e.getMatricule()
                        + " : connectez-vous à l'espace employé avec ce matricule et le mot de passe de votre compte candidat.",
                "/candidat/contrats/" + c.getId());
        notificationService.notifierEmploye(c.getCandidature().getOffre().getBesoin().getManager(),
                e.getNomComplet() + " rejoint votre équipe au poste « " + c.getPoste().getLibelle() + " ».", null);

        OffreEmploi offre = c.getCandidature().getOffre();
        long recrutes = contratRepository.countByCandidatureOffreIdAndStatutLibelle(offre.getId(), StatutContrat.VALIDE);
        if (recrutes >= offre.getBesoin().getNombre()) {
            offreService.terminerSiPublie(offre);
        }
        return e;
    }

    /**
     * Crée le nouvel employé (ou met à jour l'employé existant si le candidat était/est déjà employé),
     * libère l'ancien poste, occupe le nouveau et trace l'affectation dans historique_employe.
     */
    private Employe embaucher(Contrat c, UtilisateurConnecte u) {
        Poste nouveauPoste = c.getPoste();
        if (nouveauPoste.isEstOccupe()) {
            throw new RegleMetierException("Le poste « " + nouveauPoste.getLibelle() + " » est déjà occupé");
        }
        Employe e = c.getCandidature().getEmploye();
        if (e == null) {
            Compte compte = c.getCandidature().getCompte();
            e = new Employe();
            e.setNom(compte.getNom());
            e.setPrenom(compte.getPrenom());
            e.setEmail(compte.getEmail());
            e.setMotDePasse(compte.getMotDePasse());
            e.setMatricule("TMP-" + UUID.randomUUID());
            e.setDateEmbauche(c.getDateDebut());
            employeRepository.saveAndFlush(e);
            e.setMatricule(String.format("EMP%04d", e.getId()));
        } else {
            Poste ancienPoste = e.getPosteActuel();
            if (ancienPoste != null && !ancienPoste.getId().equals(nouveauPoste.getId())) {
                ancienPoste.setEstOccupe(false);
            }
            Contrat ancienContrat = e.getContratActuel();
            if (ancienContrat != null && StatutContrat.VALIDE.equals(ancienContrat.getStatut().getLibelle())) {
                changerStatut(ancienContrat, StatutContrat.ROMPU, u, "Remplacé par le contrat n°" + c.getId());
            }
            if (e.getDateEmbauche() == null) {
                e.setDateEmbauche(c.getDateDebut());
            }
        }
        nouveauPoste.setEstOccupe(true);
        e.setPosteActuel(nouveauPoste);
        e.setContratActuel(c);
        c.setEmploye(e);
        if (nouveauPoste.isEstManager() && nouveauPoste.getDepartement() != null) {
            nouveauPoste.getDepartement().setManagerActuel(e);
        }

        HistoriqueEmploye h = new HistoriqueEmploye();
        h.setEmploye(e);
        h.setPoste(nouveauPoste);
        h.setContrat(c);
        historiqueEmployeRepository.save(h);
        return e;
    }

    private void verifierStatut(Contrat c, String attendu) {
        if (!attendu.equals(c.getStatut().getLibelle())) {
            throw new RegleMetierException("Action impossible : le contrat est au statut « " + c.getStatut().getLibelle() + " »");
        }
    }

    private void changerStatut(Contrat c, String nouveau, UtilisateurConnecte u, String commentaire) {
        String ancien = c.getStatut().getLibelle();
        c.setStatut(references.parLibelle(StatutContrat.class, nouveau));
        traceService.tracer(HistoriqueStatut.CONTRAT, c.getId(), ancien, nouveau, u, commentaire);
    }
}
