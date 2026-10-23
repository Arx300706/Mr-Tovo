package com.mrtovo.service;

import com.mrtovo.dto.OffreForm;
import com.mrtovo.entity.Besoin;
import com.mrtovo.entity.HistoriqueStatut;
import com.mrtovo.entity.OffreEmploi;
import com.mrtovo.entity.reference.StatutBesoin;
import com.mrtovo.entity.reference.StatutOffre;
import com.mrtovo.exception.RegleMetierException;
import com.mrtovo.exception.RessourceIntrouvableException;
import com.mrtovo.repository.OffreEmploiRepository;
import com.mrtovo.security.UtilisateurConnecte;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Cycle de vie d'une offre : enregistre (RH) -> valide (directeur) -> publie (RH) -> fin.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OffreService {

    private final OffreEmploiRepository offreRepository;
    private final BesoinService besoinService;
    private final ReferenceService references;
    private final TraceService traceService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public OffreEmploi trouver(Integer id) {
        return offreRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Offre introuvable"));
    }

    @Transactional(readOnly = true)
    public List<OffreEmploi> listerPubliees() {
        return offreRepository.findPubliees(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<OffreEmploi> listerParStatut(String statut) {
        return statut == null || statut.isBlank()
                ? offreRepository.findAllByOrderByDateCreationDesc()
                : offreRepository.findByStatutLibelleOrderByDateCreationDesc(statut);
    }

    /** Pré-remplit le formulaire d'offre à partir du besoin. */
    @Transactional(readOnly = true)
    public OffreForm formulairePour(Besoin b) {
        OffreForm f = new OffreForm();
        f.setIntitule(b.getIntitule());
        f.setDescription(b.getMotif());
        return f;
    }

    public OffreEmploi creer(Integer idBesoin, OffreForm f, UtilisateurConnecte u) {
        Besoin b = besoinService.trouver(idBesoin);
        if (!StatutBesoin.VALIDE_FINANCE.equals(b.getStatut().getLibelle())) {
            throw new RegleMetierException("Une offre ne peut être créée que pour un besoin validé par la Finance");
        }
        if (offreRepository.findByBesoinId(idBesoin).isPresent()) {
            throw new RegleMetierException("Une offre existe déjà pour ce besoin");
        }
        OffreEmploi o = new OffreEmploi();
        o.setBesoin(b);
        o.setIntitule(f.getIntitule().trim());
        o.setDepartement(b.getDepartement());
        o.setService(b.getService());
        o.setDescription(f.getDescription());
        o.setProfil(f.getProfil());
        o.setDateFin(f.getDateFin());
        o.setStatut(references.parLibelle(StatutOffre.class, StatutOffre.ENREGISTRE));
        offreRepository.save(o);
        traceService.tracer(HistoriqueStatut.OFFRE, o.getId(), null, StatutOffre.ENREGISTRE, u, null);
        return o;
    }

    /** Étape directeur. */
    public void valider(Integer id, UtilisateurConnecte u) {
        OffreEmploi o = trouver(id);
        changerStatut(o, StatutOffre.ENREGISTRE, StatutOffre.VALIDE, u);
    }

    public void publier(Integer id, UtilisateurConnecte u) {
        OffreEmploi o = trouver(id);
        if (o.getDateFin() != null && o.getDateFin().isBefore(LocalDate.now())) {
            throw new RegleMetierException("La date limite de candidature est dépassée");
        }
        changerStatut(o, StatutOffre.VALIDE, StatutOffre.PUBLIE, u);
        notificationService.notifierEmploye(o.getBesoin().getManager(),
                "L'offre « " + o.getIntitule() + " » issue de votre besoin est publiée.", "/manager/besoins/" + o.getBesoin().getId());
    }

    public void cloturer(Integer id, UtilisateurConnecte u) {
        OffreEmploi o = trouver(id);
        changerStatut(o, StatutOffre.PUBLIE, StatutOffre.FIN, u);
    }

    /** Appelé par le système quand tous les postes de l'offre sont pourvus. */
    public void terminerSiPublie(OffreEmploi o) {
        if (StatutOffre.PUBLIE.equals(o.getStatut().getLibelle())) {
            o.setStatut(references.parLibelle(StatutOffre.class, StatutOffre.FIN));
            traceService.tracer(HistoriqueStatut.OFFRE, o.getId(), StatutOffre.PUBLIE, StatutOffre.FIN, null,
                    "Tous les postes sont pourvus");
        }
    }

    private void changerStatut(OffreEmploi o, String attendu, String nouveau, UtilisateurConnecte u) {
        String ancien = o.getStatut().getLibelle();
        if (!attendu.equals(ancien)) {
            throw new RegleMetierException("Action impossible : l'offre est au statut « " + ancien + " »");
        }
        o.setStatut(references.parLibelle(StatutOffre.class, nouveau));
        traceService.tracer(HistoriqueStatut.OFFRE, o.getId(), ancien, nouveau, u, null);
    }
}
