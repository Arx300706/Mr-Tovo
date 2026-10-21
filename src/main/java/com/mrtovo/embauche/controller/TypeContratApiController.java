package com.mrtovo.embauche.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mrtovo.commun.controller.ApiCrudController;
import com.mrtovo.commun.service.JournalAuditService;
import com.mrtovo.embauche.entity.TypeContrat;
import com.mrtovo.embauche.repository.TypeContratRepository;

@RestController
@RequestMapping("/api/types-contrat")
public class TypeContratApiController extends ApiCrudController<TypeContrat> {

	public TypeContratApiController(TypeContratRepository repository, JournalAuditService journalAuditService) {
		super(repository, journalAuditService, "TypeContrat", TypeContrat::getIdTypeContrat, TypeContrat::setIdTypeContrat);
	}

}
