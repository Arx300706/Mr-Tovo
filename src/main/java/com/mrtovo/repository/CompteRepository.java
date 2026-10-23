package com.mrtovo.repository;

import com.mrtovo.entity.Compte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompteRepository extends JpaRepository<Compte, Integer> {

    Optional<Compte> findByEmailIgnoreCase(String email);
}
