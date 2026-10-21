package com.mrtovo.embauche.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mrtovo.embauche.entity.Employe;

public interface EmployeRepository extends JpaRepository<Employe, Integer> {

}
