package com.mrtovo.repository;

import com.mrtovo.entity.Proposition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PropositionRepository extends JpaRepository<Proposition, Integer> {

    List<Proposition> findAllByOrderByDateModificationDesc();

    List<Proposition> findByStatutLibelleOrderByDateModificationDesc(String statut);

    List<Proposition> findByCandidatureIdOrderByDateCreationDesc(Integer idCandidature);

    List<Proposition> findByCandidatureCompteIdOrderByDateCreationDesc(Integer idCompte);

    long countByStatutLibelle(String statut);

    long countByCandidatureCompteIdAndStatutLibelle(Integer idCompte, String statut);

    boolean existsByCandidatureIdAndStatutLibelleNot(Integer idCandidature, String statut);

    /** Propositions acceptées pour lesquelles la RH n'a pas encore généré de contrat. */
    @Query("""
            select p from Proposition p
            where p.statut.libelle = 'valide'
              and not exists (select c from Contrat c where c.proposition = p)
            order by p.dateModification desc""")
    List<Proposition> findValidesSansContrat();

    @Query("""
            select count(p) from Proposition p
            where p.statut.libelle = 'valide'
              and not exists (select c from Contrat c where c.proposition = p)""")
    long countValidesSansContrat();
}
