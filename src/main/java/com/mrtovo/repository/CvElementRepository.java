package com.mrtovo.repository;

import com.mrtovo.entity.CvElement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CvElementRepository extends JpaRepository<CvElement, Integer> {
    List<CvElement> findByCandidatureIdOrderByTypeAscOrdreAsc(Integer idCandidature);
    List<CvElement> findByEmployeIdOrderByTypeAscOrdreAsc(Integer idEmploye);
}
