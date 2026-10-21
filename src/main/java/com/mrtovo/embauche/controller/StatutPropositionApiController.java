package com.mrtovo.embauche.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mrtovo.commun.controller.ApiCrudController;
import com.mrtovo.commun.service.JournalAuditService;
import com.mrtovo.embauche.entity.StatutProposition;
import com.mrtovo.embauche.repository.StatutPropositionRepository;

@RestController
@RequestMapping("/api/statuts-proposition")
public class StatutPropositionApiController extends ApiCrudController<StatutProposition> {

	public StatutPropositionApiController(StatutPropositionRepository repository, JournalAuditService journalAuditService) {
		super(repository, journalAuditService, "StatutProposition", StatutProposition::getIdStatutProposition, StatutProposition::setIdStatutProposition);
	}

}
