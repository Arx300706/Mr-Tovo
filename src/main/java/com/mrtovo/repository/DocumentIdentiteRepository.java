package com.mrtovo.repository;

import com.mrtovo.entity.DocumentIdentite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentIdentiteRepository extends JpaRepository<DocumentIdentite, Integer> {
    Optional<DocumentIdentite> findByCandidatureId(Integer idCandidature);
    Optional<DocumentIdentite> findByEmployeId(Integer idEmploye);
}
