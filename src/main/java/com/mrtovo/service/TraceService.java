package com.mrtovo.service;

import com.mrtovo.entity.HistoriqueStatut;
import com.mrtovo.repository.HistoriqueStatutRepository;
import com.mrtovo.security.UtilisateurConnecte;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Traçabilité : chaque changement de statut est enregistré dans historique_statut. */
@Service
@RequiredArgsConstructor
public class TraceService {

    private final HistoriqueStatutRepository repository;

    @Transactional
    public void tracer(String entite, Integer idEntite, String ancienStatut, String nouveauStatut,
                       UtilisateurConnecte auteur, String commentaire) {
        HistoriqueStatut h = new HistoriqueStatut();
        h.setEntite(entite);
        h.setIdEntite(idEntite);
        h.setAncienStatut(ancienStatut);
        h.setNouveauStatut(nouveauStatut);
        h.setAuteur(auteur == null ? "Système" : auteur.getNomComplet());
        h.setCommentaire(commentaire);
        repository.save(h);
    }

    @Transactional(readOnly = true)
    public List<HistoriqueStatut> historique(String entite, Integer idEntite) {
        return repository.findByEntiteAndIdEntiteOrderByDateChangementAscIdAsc(entite, idEntite);
    }
}
