package com.mrtovo.recrutement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mrtovo.recrutement.entity.EvaluationCandidat;

public interface EvaluationCandidatRepository extends JpaRepository<EvaluationCandidat, Integer> {

	List<EvaluationCandidat> findByEntretienCandidatureIdCandidatureOrderByDateEvaluationAsc(Integer idCandidature);

	/** Note moyenne (sur 20) d'une candidature, null si aucune évaluation. */
	@Query("select avg(e.note) from EvaluationCandidat e where e.entretien.candidature.idCandidature = :idCandidature")
	Double noteMoyenne(Integer idCandidature);

}
