package com.mrtovo.service;

import com.mrtovo.entity.reference.*;
import com.mrtovo.repository.*;
import com.mrtovo.security.Roles;
import com.mrtovo.security.UtilisateurConnecte;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pastilles du menu : nombre d'éléments qui attendent une action de l'utilisateur connecté.
 * Si quelque chose existe, l'entrée du menu est marquée.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BadgeService {

    private final BesoinRepository besoinRepository;
    private final OffreEmploiRepository offreRepository;
    private final CandidatureRepository candidatureRepository;
    private final PropositionRepository propositionRepository;
    private final ContratRepository contratRepository;
    private final NotificationService notificationService;

    public Map<String, Long> calculer(UtilisateurConnecte u) {
        Map<String, Long> b = new HashMap<>();
        if (u == null) {
            return b;
        }
        b.put("notifications", notificationService.nonLues(u));
        if (u.estCandidat()) {
            b.put("propositions", propositionRepository.countByCandidatureCompteIdAndStatutLibelle(u.getId(), StatutProposition.ENREGISTRE));
            b.put("contrats", contratRepository.countByCandidatureCompteIdAndStatutLibelle(u.getId(), StatutContrat.CREE));
            return b;
        }
        if (u.aRole(Roles.RH)) {
            b.put("rhBesoinsCrees", besoinRepository.countByStatutLibelle(StatutBesoin.CREE));
            b.put("rhBesoinsValides", besoinRepository.countValidesSansOffre());
            b.put("rhOffresValides", offreRepository.countByStatutLibelle(StatutOffre.VALIDE));
            b.put("rhCandidaturesCreees", candidatureRepository.countByStatutLibelle(StatutCandidature.CREE));
            b.put("rhCandidaturesEnCours", candidatureRepository.countByStatutLibelleIn(List.of(
                    StatutCandidature.SELECTIONNE, StatutCandidature.TESTE_TECHNIQUEMENT, StatutCandidature.ENTRETIEN_OK)));
            b.put("rhCandidaturesSucces", candidatureRepository.countSuccesSansProposition());
            b.put("rhPropositionsEnAttente", propositionRepository.countByStatutLibelle(StatutProposition.EN_ATTENTE));
            b.put("rhPropositionsValides", propositionRepository.countValidesSansContrat());
            b.put("rhContratsSignes", contratRepository.countByStatutLibelle(StatutContrat.SIGNE));
        }
        if (u.aRole(Roles.FINANCE)) {
            b.put("financeBesoins", besoinRepository.countByStatutLibelle(StatutBesoin.ENVOYE_FINANCE));
        }
        if (u.aRole(Roles.DIRECTEUR)) {
            b.put("directeurOffres", offreRepository.countByStatutLibelle(StatutOffre.ENREGISTRE));
        }
        return b;
    }
}
