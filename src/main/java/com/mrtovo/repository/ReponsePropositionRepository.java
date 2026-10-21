package com.mrtovo.repository;

import com.mrtovo.entity.ReponseProposition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReponsePropositionRepository extends JpaRepository<ReponseProposition, Integer> {

    List<ReponseProposition> findByPropositionIdOrderByDateReponseDesc(Integer idProposition);

    Optional<ReponseProposition> findFirstByPropositionIdOrderByDateReponseDesc(Integer idProposition);
}
