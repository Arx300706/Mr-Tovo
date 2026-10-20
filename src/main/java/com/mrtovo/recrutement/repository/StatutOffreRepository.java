package com.mrtovo.recrutement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mrtovo.recrutement.entity.StatutOffre;

public interface StatutOffreRepository extends JpaRepository<StatutOffre, Integer> {

	Optional<StatutOffre> findByLibelle(String libelle);

	List<StatutOffre> findAllByOrderByIdStatutOffreAsc();

}
