package com.mrtovo.recrutement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mrtovo.recrutement.entity.StatutCandidature;

public interface StatutCandidatureRepository extends JpaRepository<StatutCandidature, Integer> {

	Optional<StatutCandidature> findByLibelle(String libelle);

	List<StatutCandidature> findAllByOrderByIdStatutCandidatureAsc();

}
