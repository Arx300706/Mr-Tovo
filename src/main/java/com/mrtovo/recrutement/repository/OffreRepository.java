package com.mrtovo.recrutement.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mrtovo.recrutement.entity.Offre;

public interface OffreRepository extends JpaRepository<Offre, Integer> {

	List<Offre> findAllByOrderByDateCreationDescIdOffreDesc();

	List<Offre> findByStatutOffreLibelleOrderByDateCreationDescIdOffreDesc(String libelle);

	/**
	 * Offres visibles au public : publiées, non expirées, filtrées par mot-clé (titre, lieu) et type de contrat.
	 * {@code motCle} doit déjà être en minuscules et entouré de « % », ou null.
	 */
	@Query("""
		select o from Offre o left join o.typeContrat t
		where o.statutOffre.libelle = :statut
		  and (o.dateLimite is null or o.dateLimite >= :aujourdhui)
		  and (:idTypeContrat is null or t.idTypeContrat = :idTypeContrat)
		  and (:motCle is null or lower(o.titre) like :motCle or lower(o.lieu) like :motCle)
		order by o.datePublication desc, o.idOffre desc""")
	List<Offre> rechercherVisibles(@Param("statut") String statut, @Param("aujourdhui") LocalDate aujourdhui,
			@Param("idTypeContrat") Integer idTypeContrat, @Param("motCle") String motCle);

}
