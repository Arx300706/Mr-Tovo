package com.mrtovo.recrutement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mrtovo.recrutement.entity.Candidat;

public interface CandidatRepository extends JpaRepository<Candidat, Integer> {

	boolean existsByEmailIgnoreCase(String email);

	Optional<Candidat> findByEmailIgnoreCase(String email);

}
