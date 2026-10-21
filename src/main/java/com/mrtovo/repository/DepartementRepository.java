package com.mrtovo.repository;

import com.mrtovo.entity.Departement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartementRepository extends JpaRepository<Departement, Integer> {

    List<Departement> findAllByOrderByLibelleAsc();

    Optional<Departement> findByCode(String code);
}
