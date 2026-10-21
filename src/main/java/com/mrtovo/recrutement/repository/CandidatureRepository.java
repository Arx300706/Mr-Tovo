package com.mrtovo.recrutement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mrtovo.recrutement.entity.Candidature;

public interface CandidatureRepository extends JpaRepository<Candidature, Integer> {

	boolean existsByCandidatIdCandidatAndOffreIdOffre(Integer idCandidat, Integer idOffre);

	List<Candidature> findByCandidatIdCandidatOrderByDateCandidatureDesc(Integer idCandidat);

	List<Candidature> findByOffreIdOffreOrderByDateCandidatureDesc(Integer idOffre);

	List<Candidature> findByOffreIdOffreAndStatutCandidatureLibelleOrderByDateCandidatureDesc(Integer idOffre,
			String libelle);

	long countByOffreIdOffre(Integer idOffre);

	/** Nombre de candidatures par offre : lignes [idOffre, nombre]. */
	@Query("select c.offre.idOffre, count(c) from Candidature c group by c.offre.idOffre")
	List<Object[]> compterParOffre();

}
