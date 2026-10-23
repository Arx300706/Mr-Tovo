package com.mrtovo.repository;

import com.mrtovo.entity.HistoriqueEmploye;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoriqueEmployeRepository extends JpaRepository<HistoriqueEmploye, Integer> {

    List<HistoriqueEmploye> findByEmployeIdOrderByDateDesc(Integer idEmploye);
}
