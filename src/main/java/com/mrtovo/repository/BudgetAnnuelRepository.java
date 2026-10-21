package com.mrtovo.repository;

import com.mrtovo.entity.BudgetAnnuel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BudgetAnnuelRepository extends JpaRepository<BudgetAnnuel, Integer> {

    Optional<BudgetAnnuel> findByAnnee(int annee);
}
