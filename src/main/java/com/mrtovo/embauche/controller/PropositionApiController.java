package com.mrtovo.embauche.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mrtovo.commun.controller.ApiCrudController;
import com.mrtovo.commun.service.JournalAuditService;
import com.mrtovo.embauche.entity.Proposition;
import com.mrtovo.embauche.repository.PropositionRepository;

@RestController
@RequestMapping("/api/propositions")
public class PropositionApiController extends ApiCrudController<Proposition> {

	public PropositionApiController(PropositionRepository repository, JournalAuditService journalAuditService) {
		super(repository, journalAuditService, "Proposition", Proposition::getIdProposition, Proposition::setIdProposition);
	}

}
