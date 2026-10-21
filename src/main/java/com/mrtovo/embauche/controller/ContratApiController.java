package com.mrtovo.embauche.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mrtovo.commun.controller.ApiCrudController;
import com.mrtovo.commun.service.JournalAuditService;
import com.mrtovo.embauche.entity.Contrat;
import com.mrtovo.embauche.repository.ContratRepository;

@RestController
@RequestMapping("/api/contrats")
public class ContratApiController extends ApiCrudController<Contrat> {

	public ContratApiController(ContratRepository repository, JournalAuditService journalAuditService) {
		super(repository, journalAuditService, "Contrat", Contrat::getIdContrat, Contrat::setIdContrat);
	}

}
