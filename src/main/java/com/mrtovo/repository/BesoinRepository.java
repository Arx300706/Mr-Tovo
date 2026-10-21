package com.mrtovo.repository;

import com.mrtovo.entity.Besoin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface BesoinRepository extends JpaRepository<Besoin, Integer> {

    List<Besoin> findAllByOrderByDateCreationDesc();

    List<Besoin> findByStatutLibelleOrderByDateCreationDesc(String statut);

    List<Besoin> findByDepartementIdOrderByDateCreationDesc(Integer idDepartement);

    long countByStatutLibelle(String statut);

    /** Besoins validés par la Finance pour lesquels la RH n'a pas encore créé d'offre. */
    @Query("""
            select b from Besoin b
            where b.statut.libelle = 'valide Finance'
              and not exists (select o from OffreEmploi o where o.besoin = b)
            order by b.dateModification desc""")
    List<Besoin> findValidesSansOffre();

    @Query("""
            select count(b) from Besoin b
            where b.statut.libelle = 'valide Finance'
              and not exists (select o from OffreEmploi o where o.besoin = b)""")
    long countValidesSansOffre();

    /** Total des dépenses annuelles déjà engagées (besoins validés) sur le budget d'une année. */
    @Query("""
            select coalesce(sum(b.depenseAnnuelle), 0) from Besoin b
            where b.statut.libelle = 'valide Finance' and b.anneeBudget = :annee""")
    BigDecimal sommeDepensesValidees(int annee);
}
