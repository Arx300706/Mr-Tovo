package com.mrtovo.repository;

import com.mrtovo.entity.GrilleSalaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GrilleSalaireRepository extends JpaRepository<GrilleSalaire, Integer> {

    List<GrilleSalaire> findAllByOrderByPosteDepartementLibelleAscPosteLibelleAscAncienneteMinAsc();

    List<GrilleSalaire> findByPosteIdOrderByAncienneteMinAsc(Integer idPoste);
}
