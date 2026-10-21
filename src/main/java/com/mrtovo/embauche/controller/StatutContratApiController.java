package com.mrtovo.embauche.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mrtovo.commun.controller.ApiCrudController;
import com.mrtovo.commun.service.JournalAuditService;
import com.mrtovo.embauche.entity.StatutContrat;
import com.mrtovo.embauche.repository.StatutContratRepository;

@RestController
@RequestMapping("/api/statuts-contrat")
public class StatutContratApiController extends ApiCrudController<StatutContrat> {

	public StatutContratApiController(StatutContratRepository repository, JournalAuditService journalAuditService) {
		super(repository, journalAuditService, "StatutContrat", StatutContrat::getIdStatutContrat, StatutContrat::setIdStatutContrat);
	}

}
