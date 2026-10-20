package com.mrtovo.embauche.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mrtovo.embauche.entity.TypeContrat;

public interface TypeContratRepository extends JpaRepository<TypeContrat, Integer> {

	List<TypeContrat> findAllByOrderByIdTypeContratAsc();

	Optional<TypeContrat> findByLibelle(String libelle);

}
