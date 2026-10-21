package com.mrtovo.repository;

import com.mrtovo.entity.MessageProposition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessagePropositionRepository extends JpaRepository<MessageProposition, Integer> {

    List<MessageProposition> findByPropositionIdOrderByDateEnvoiAsc(Integer idProposition);
}
