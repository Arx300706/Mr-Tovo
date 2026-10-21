package com.mrtovo.commun.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mrtovo.commun.entity.Employe;

public interface EmployeRepository extends JpaRepository<Employe, Integer> {

	Optional<Employe> findByMatriculeIgnoreCase(String matricule);
}