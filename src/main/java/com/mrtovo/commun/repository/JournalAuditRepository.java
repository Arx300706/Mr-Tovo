package com.mrtovo.commun.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mrtovo.commun.entity.JournalAudit;

public interface JournalAuditRepository extends JpaRepository<JournalAudit, Integer> {

	Page<JournalAudit> findAllByOrderByDateActionDescIdJournalAuditDesc(Pageable pageable);

}
