package com.mrtovo.repository;

import com.mrtovo.entity.Candidature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface CandidatureRepository extends JpaRepository<Candidature, Integer> {

    List<Candidature> findAllByOrderByDateCandidatureDesc();

    List<Candidature> findByStatutLibelleOrderByDateCandidatureDesc(String statut);

    List<Candidature> findByCompteIdOrderByDateCandidatureDesc(Integer idCompte);

    List<Candidature> findByOffreIdOrderByDateCandidatureDesc(Integer idOffre);

    boolean existsByCompteIdAndOffreId(Integer idCompte, Integer idOffre);

    long countByStatutLibelle(String statut);

    long countByStatutLibelleIn(Collection<String> statuts);

    /** Candidatures réussies qui n'ont pas encore de proposition en cours ou validée. */
    @Query("""
            select c from Candidature c
            where c.statut.libelle = 'succes'
              and not exists (select p from Proposition p where p.candidature = c and p.statut.libelle <> 'refuse')
            order by c.dateModification desc""")
    List<Candidature> findSuccesSansProposition();

    @Query("""
            select count(c) from Candidature c
            where c.statut.libelle = 'succes'
              and not exists (select p from Proposition p where p.candidature = c and p.statut.libelle <> 'refuse')""")
    long countSuccesSansProposition();
}
