package com.mrtovo.repository;

import com.mrtovo.entity.Contrat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContratRepository extends JpaRepository<Contrat, Integer> {

    List<Contrat> findAllByOrderByDateModificationDesc();

    List<Contrat> findByStatutLibelleOrderByDateModificationDesc(String statut);

    List<Contrat> findByCandidatureCompteIdOrderByDateCreationDesc(Integer idCompte);

    Optional<Contrat> findByPropositionId(Integer idProposition);

    long countByStatutLibelle(String statut);

    long countByCandidatureCompteIdAndStatutLibelle(Integer idCompte, String statut);

    long countByCandidatureOffreIdAndStatutLibelle(Integer idOffre, String statut);
}
