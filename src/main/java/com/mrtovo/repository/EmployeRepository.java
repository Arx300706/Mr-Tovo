package com.mrtovo.repository;

import com.mrtovo.entity.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EmployeRepository extends JpaRepository<Employe, Integer> {

    @Query("select e from Employe e left join fetch e.posteActuel p left join fetch p.departement where e.matricule = :matricule")
    Optional<Employe> findByMatricule(String matricule);

    boolean existsByMatricule(String matricule);
}
