package com.mrtovo.commun.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mrtovo.commun.entity.Compte;
import com.mrtovo.commun.entity.Role;

public interface CompteRepository extends JpaRepository<Compte, Integer> {

	Optional<Compte> findByIdentifiantIgnoreCase(String identifiant);

	boolean existsByIdentifiantIgnoreCase(String identifiant);

	List<Compte> findAllByOrderByRoleAscIdentifiantAsc();

	List<Compte> findByRoleAndActifTrue(Role role);

}
