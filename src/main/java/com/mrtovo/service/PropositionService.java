package com.mrtovo.service;

import com.mrtovo.dto.PropositionForm;
import com.mrtovo.dto.ReponseForm;
import com.mrtovo.entity.*;
import com.mrtovo.entity.reference.StatutCandidature;
import com.mrtovo.entity.reference.StatutProposition;
import com.mrtovo.entity.reference.TypeContrat;
import com.mrtovo.exception.RegleMetierException;
import com.mrtovo.exception.RessourceIntrouvableException;
import com.mrtovo.repository.*;
import com.mrtovo.security.UtilisateurConnecte;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Proposition d'emploi :
 * enregistre (RH) -> réponse du candidat -> valide / refuse / en attente ;
 * en attente -> la RH révise (retour à enregistre) ou accepte la contre-proposition (valide).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PropositionService {

    private final PropositionRepository propositionRepository;
    private final ReponsePropositionRepository reponseRepository;
    private final MessagePropositionRepository messageRepository;
    private final PosteRepository posteRepository;
    private final EmployeRepository employeRepository;
    private final CandidatureService candidatureService;
    private final ReferenceService references;
    private final TraceService traceService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Proposition trouver(Integer id) {
        return propositionRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Proposition introuvable"));
    }

    @Transactional(readOnly = true)
    public Proposition trouverPourCandidat(Integer id, UtilisateurConnecte u) {
        Proposition p = trouver(id);
        if (!Objects.equals(p.getCandidature().getCompte().getId(), u.getId())) {
            throw new RessourceIntrouvableException("Proposition introuvable");
        }
        return p;
    }

    @Transactional(readOnly = true)
    public List<Proposition> listerParStatut(String statut) {
        return statut == null || statut.isBlank()
                ? propositionRepository.findAllByOrderByDateModificationDesc()
                : propositionRepository.findByStatutLibelleOrderByDateModificationDesc(statut);
    }

    @Transactional(readOnly = true)
    public List<Proposition> listerPourCompte(Integer idCompte) {
        return propositionRepository.findByCandidatureCompteIdOrderByDateCreationDesc(idCompte);
    }

    @Transactional(readOnly = true)
    public List<Proposition> listerPourCandidature(Integer idCandidature) {
        return propositionRepository.findByCandidatureIdOrderByDateCreationDesc(idCandidature);
    }

    @Transactional(readOnly = true)
    public List<Proposition> listerValidesSansContrat() {
        return propositionRepository.findValidesSansContrat();
    }

    @Transactional(readOnly = true)
    public List<ReponseProposition> reponses(Integer idProposition) {
        return reponseRepository.findByPropositionIdOrderByDateReponseDesc(idProposition);
    }

    @Transactional(readOnly = true)
    public List<MessageProposition> messages(Integer idProposition) {
        return messageRepository.findByPropositionIdOrderByDateEnvoiAsc(idProposition);
    }

    /** Postes vacants du service de l'offre, proposables au candidat. */
    @Transactional(readOnly = true)
    public List<Poste> postesVacants(Candidature c) {
        return posteRepository.findByServiceIdAndEstOccupeFalseAndEstActifTrueOrderByLibelleAsc(c.getOffre().getService().getId());
    }

    /**
     * Pré-remplit la proposition à partir de l'offre à laquelle le candidat a postulé :
     * poste visé, salaire retenu par la RH, type de contrat et date de commencement du besoin,
     * date de fin par défaut (CDD : 1 an, CE : 6 mois) et 7 jours pour répondre.
     */
    @Transactional(readOnly = true)
    public PropositionForm formulairePour(Candidature c) {
        OffreEmploi offre = c.getOffre();
        Besoin b = offre.getBesoin();
        PropositionForm f = new PropositionForm();
        if (b.getPoste() != null && !b.getPoste().isEstOccupe()) {
            f.setIdPoste(b.getPoste().getId());
        }
        f.setLibelleNouveauPoste(offre.getIntitule());
        f.setSalaire(b.getSalaireMensuel());
        f.setIdTypeContrat(b.getTypeContrat().getId());

        LocalDate aujourdhui = LocalDate.now();
        LocalDate debut = b.getDateCommencement() != null && b.getDateCommencement().isAfter(aujourdhui)
                ? b.getDateCommencement() : aujourdhui.plusMonths(1);
        f.setDateCommencement(debut);
        f.setDateFin(switch (b.getTypeContrat().getLibelle()) {
            case TypeContrat.CDD -> debut.plusYears(1).minusDays(1);
            case TypeContrat.CE -> debut.plusMonths(6).minusDays(1);
            default -> null;
        });
        LocalDate limite = aujourdhui.plusDays(7);
        f.setDateLimiteValidation(limite.isAfter(debut) ? debut : limite);
        return f;
    }

    @Transactional(readOnly = true)
    public PropositionForm formulairePour(Proposition p) {
        PropositionForm f = new PropositionForm();
        f.setIdPoste(p.getPoste().getId());
        f.setSalaire(p.getSalaire());
        f.setIdTypeContrat(p.getTypeContrat().getId());
        f.setDateCommencement(p.getDateCommencement());
        f.setDateFin(p.getDateFin());
        f.setDateLimiteValidation(p.getDateLimiteValidation());
        return f;
    }

    // ---------------------------------------------------------------- RH

    public Proposition creer(Integer idCandidature, PropositionForm f, UtilisateurConnecte u) {
        Candidature c = candidatureService.trouver(idCandidature);
        if (!StatutCandidature.SUCCES.equals(c.getStatut().getLibelle())) {
            throw new RegleMetierException("Une proposition ne peut être faite qu'à une candidature « succes »");
        }
        if (propositionRepository.existsByCandidatureIdAndStatutLibelleNot(idCandidature, StatutProposition.REFUSE)) {
            throw new RegleMetierException("Cette candidature a déjà une proposition en cours");
        }
        Proposition p = new Proposition();
        p.setCandidature(c);
        p.setOffre(c.getOffre());
        appliquer(p, f, true);
        p.setStatut(references.parLibelle(StatutProposition.class, StatutProposition.ENREGISTRE));
        propositionRepository.save(p);
        traceService.tracer(HistoriqueStatut.PROPOSITION, p.getId(), null, StatutProposition.ENREGISTRE, u, null);
        notificationService.notifierCompte(c.getCompte(),
                "Vous avez reçu une proposition d'emploi pour « " + c.getOffre().getIntitule() + " ».",
                "/candidat/propositions/" + p.getId());
        return p;
    }

    /** Nouvelle version de la proposition après une contre-proposition (ou une proposition expirée). */
    public void reviser(Integer id, PropositionForm f, UtilisateurConnecte u) {
        Proposition p = trouver(id);
        String statut = p.getStatut().getLibelle();
        boolean enregistreeExpiree = StatutProposition.ENREGISTRE.equals(statut) && p.estExpiree();
        if (!StatutProposition.EN_ATTENTE.equals(statut) && !enregistreeExpiree) {
            throw new RegleMetierException("Seule une proposition « en attente » (ou expirée) peut être révisée");
        }
        appliquer(p, f, false);
        changerStatut(p, StatutProposition.ENREGISTRE, u, "Proposition révisée par la RH");
        notificationService.notifierCompte(p.getCandidature().getCompte(),
                "La RH a révisé sa proposition pour « " + p.getOffre().getIntitule() + " ». Merci d'y répondre.",
                "/candidat/propositions/" + p.getId());
    }

    /** La RH accepte toutes les valeurs souhaitées par le candidat dans sa dernière réponse. */
    public void accepterContreProposition(Integer id, UtilisateurConnecte u) {
        Proposition p = trouver(id);
        verifierStatut(p, StatutProposition.EN_ATTENTE);
        ReponseProposition r = reponseRepository.findFirstByPropositionIdOrderByDateReponseDesc(id)
                .orElseThrow(() -> new RegleMetierException("Aucune réponse du candidat"));
        if (!r.isAccepteSalaire()) {
            p.setSalaire(r.getSalaireSouhaite());
        }
        if (!r.isAccepteTypeContrat()) {
            p.setTypeContrat(r.getTypeContratSouhaite());
            if (!r.getTypeContratSouhaite().aUneDateFin()) {
                p.setDateFin(null);
            }
        }
        if (!r.isAccepteDateCommencement()) {
            p.setDateCommencement(r.getDateCommencementSouhaite());
        }
        if (Boolean.FALSE.equals(r.getAccepteDateFin())) {
            p.setDateFin(r.getDateFinSouhaite());
        }
        changerStatut(p, StatutProposition.VALIDE, u, "Contre-proposition du candidat acceptée par la RH");
        notificationService.notifierCompte(p.getCandidature().getCompte(),
                "La RH a accepté votre contre-proposition pour « " + p.getOffre().getIntitule() + " ». Le contrat va être préparé.",
                "/candidat/propositions/" + p.getId());
    }

    /** La RH met fin à la négociation. */
    public void clore(Integer id, UtilisateurConnecte u) {
        Proposition p = trouver(id);
        String statut = p.getStatut().getLibelle();
        if (!StatutProposition.EN_ATTENTE.equals(statut) && !StatutProposition.ENREGISTRE.equals(statut)) {
            throw new RegleMetierException("Cette proposition est déjà terminée");
        }
        changerStatut(p, StatutProposition.REFUSE, u, "Négociation close par la RH");
        notificationService.notifierCompte(p.getCandidature().getCompte(),
                "La négociation de la proposition « " + p.getOffre().getIntitule() + " » a été close.",
                "/candidat/propositions/" + p.getId());
    }

    // ---------------------------------------------------------------- Candidat

    public String repondre(Integer id, ReponseForm f, UtilisateurConnecte u) {
        Proposition p = trouverPourCandidat(id, u);
        verifierStatut(p, StatutProposition.ENREGISTRE);
        if (p.estExpiree()) {
            throw new RegleMetierException("La date limite de réponse est dépassée : contactez la RH");
        }
        boolean avecDateFin = p.getDateFin() != null;
        List<Boolean> choix = new ArrayList<>(List.of(
                oui(f.getAccepteSalaire()), oui(f.getAccepteTypeContrat()), oui(f.getAccepteDateCommencement())));
        if (avecDateFin) {
            choix.add(oui(f.getAccepteDateFin()));
        }
        String decision = DecisionProposition.decider(choix);

        ReponseProposition r = new ReponseProposition();
        r.setProposition(p);
        r.setAccepteSalaire(f.getAccepteSalaire());
        r.setAccepteTypeContrat(f.getAccepteTypeContrat());
        r.setAccepteDateCommencement(f.getAccepteDateCommencement());
        r.setAccepteDateFin(avecDateFin ? f.getAccepteDateFin() : null);
        r.setCommentaire(f.getCommentaire());

        // En négociation, chaque « Non » doit être accompagné de la valeur souhaitée
        if (StatutProposition.EN_ATTENTE.equals(decision)) {
            if (!f.getAccepteSalaire()) {
                if (f.getSalaireSouhaite() == null || f.getSalaireSouhaite().signum() <= 0) {
                    throw new RegleMetierException("Indiquez le salaire souhaité");
                }
                r.setSalaireSouhaite(f.getSalaireSouhaite());
            }
            if (!f.getAccepteTypeContrat()) {
                TypeContrat t = references.parId(TypeContrat.class, f.getIdTypeContratSouhaite());
                if (t == null || t.getId().equals(p.getTypeContrat().getId())) {
                    throw new RegleMetierException("Choisissez le type de contrat souhaité");
                }
                r.setTypeContratSouhaite(t);
            }
            if (!f.getAccepteDateCommencement()) {
                if (f.getDateCommencementSouhaite() == null) {
                    throw new RegleMetierException("Indiquez la date de commencement souhaitée");
                }
                r.setDateCommencementSouhaite(f.getDateCommencementSouhaite());
            }
            if (avecDateFin && !f.getAccepteDateFin()) {
                if (f.getDateFinSouhaite() == null) {
                    throw new RegleMetierException("Indiquez la date de fin souhaitée");
                }
                r.setDateFinSouhaite(f.getDateFinSouhaite());
            }
        }
        reponseRepository.save(r);
        if (f.getCommentaire() != null && !f.getCommentaire().isBlank()) {
            ajouterMessage(p, MessageProposition.AUTEUR_CANDIDAT, null, f.getCommentaire());
        }
        changerStatut(p, decision, u, "Réponse du candidat");
        return decision;
    }

    // ---------------------------------------------------------------- Discussion

    public void envoyerMessageCandidat(Integer id, String contenu, UtilisateurConnecte u) {
        Proposition p = trouverPourCandidat(id, u);
        ajouterMessage(p, MessageProposition.AUTEUR_CANDIDAT, null, contenu);
    }

    public void envoyerMessageRh(Integer id, String contenu, UtilisateurConnecte u) {
        Proposition p = trouver(id);
        ajouterMessage(p, MessageProposition.AUTEUR_RH, employeRepository.findById(u.getId()).orElse(null), contenu);
        notificationService.notifierCompte(p.getCandidature().getCompte(),
                "Nouveau message de la RH au sujet de la proposition « " + p.getOffre().getIntitule() + " ».",
                "/candidat/propositions/" + p.getId());
    }

    private void ajouterMessage(Proposition p, String auteur, Employe employe, String contenu) {
        if (contenu == null || contenu.isBlank()) {
            throw new RegleMetierException("Le message est vide");
        }
        MessageProposition m = new MessageProposition();
        m.setProposition(p);
        m.setAuteur(auteur);
        m.setEmploye(employe);
        m.setContenu(contenu.trim());
        messageRepository.save(m);
    }

    // ----------------------------------------------------------------

    private void appliquer(Proposition p, PropositionForm f, boolean creation) {
        TypeContrat type = references.parId(TypeContrat.class, f.getIdTypeContrat());
        if (type == null) {
            throw new RegleMetierException("Type de contrat invalide");
        }
        if (type.aUneDateFin()) {
            if (f.getDateFin() == null || !f.getDateFin().isAfter(f.getDateCommencement())) {
                throw new RegleMetierException("Pour un " + type.getLibelle() + ", la date de fin doit être après la date de commencement");
            }
        }
        if (f.getDateLimiteValidation().isAfter(f.getDateCommencement())) {
            throw new RegleMetierException("La date limite de réponse doit précéder la date de commencement");
        }
        p.setPoste(posteChoisi(p, f, creation));
        p.setSalaire(f.getSalaire());
        p.setTypeContrat(type);
        p.setDateCommencement(f.getDateCommencement());
        p.setDateFin(type.aUneDateFin() ? f.getDateFin() : null);
        p.setDateLimiteValidation(f.getDateLimiteValidation());
    }

    /** Poste vacant choisi, ou nouveau poste créé dans le service de l'offre. */
    private Poste posteChoisi(Proposition p, PropositionForm f, boolean creation) {
        OffreEmploi offre = p.getOffre();
        if (f.getIdPoste() != null) {
            if (!creation && p.getPoste() != null && p.getPoste().getId().equals(f.getIdPoste())) {
                return p.getPoste();
            }
            return posteRepository.findById(f.getIdPoste())
                    .filter(x -> !x.isEstOccupe() && x.isEstActif())
                    .orElseThrow(() -> new RegleMetierException("Ce poste n'est pas vacant"));
        }
        if (f.getLibelleNouveauPoste() == null || f.getLibelleNouveauPoste().isBlank()) {
            throw new RegleMetierException("Choisissez un poste vacant ou saisissez le libellé du nouveau poste");
        }
        Poste nouveau = new Poste();
        nouveau.setLibelle(f.getLibelleNouveauPoste().trim());
        nouveau.setDepartement(offre.getDepartement());
        nouveau.setService(offre.getService());
        return posteRepository.save(nouveau);
    }

    private static boolean oui(Boolean b) {
        if (b == null) {
            throw new RegleMetierException("Répondez Oui ou Non pour chaque champ");
        }
        return b;
    }

    private void verifierStatut(Proposition p, String attendu) {
        if (!attendu.equals(p.getStatut().getLibelle())) {
            throw new RegleMetierException("Action impossible : la proposition est au statut « " + p.getStatut().getLibelle() + " »");
        }
    }

    private void changerStatut(Proposition p, String nouveau, UtilisateurConnecte u, String commentaire) {
        String ancien = p.getStatut().getLibelle();
        p.setStatut(references.parLibelle(StatutProposition.class, nouveau));
        traceService.tracer(HistoriqueStatut.PROPOSITION, p.getId(), ancien, nouveau, u, commentaire);
    }
}
