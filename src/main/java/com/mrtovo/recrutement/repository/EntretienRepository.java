package com.mrtovo.recrutement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mrtovo.recrutement.entity.Entretien;

public interface EntretienRepository extends JpaRepository<Entretien, Integer> {

	List<Entretien> findByCandidatureIdCandidatureOrderByDateEntretienAsc(Integer idCandidature);

}
