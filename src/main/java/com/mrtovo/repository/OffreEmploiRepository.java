package com.mrtovo.repository;

import com.mrtovo.entity.OffreEmploi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OffreEmploiRepository extends JpaRepository<OffreEmploi, Integer> {

    List<OffreEmploi> findAllByOrderByDateCreationDesc();

    List<OffreEmploi> findByStatutLibelleOrderByDateCreationDesc(String statut);

    long countByStatutLibelle(String statut);

    Optional<OffreEmploi> findByBesoinId(Integer idBesoin);

    /** Offres visibles par le public : publiées et dont la date de fin n'est pas dépassée. */
    @Query("""
            select o from OffreEmploi o
            where o.statut.libelle = 'publie' and (o.dateFin is null or o.dateFin >= :aujourdhui)
            order by o.dateCreation desc""")
    List<OffreEmploi> findPubliees(LocalDate aujourdhui);
}
