package com.mrtovo.besoin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mrtovo.commun.controller.ApiCrudController;
import com.mrtovo.commun.service.JournalAuditService;
import com.mrtovo.besoin.entity.Validation;
import com.mrtovo.besoin.repository.ValidationRepository;

@RestController
@RequestMapping("/api/validations")
public class ValidationApiController extends ApiCrudController<Validation> {

	public ValidationApiController(ValidationRepository repository, JournalAuditService journalAuditService) {
		super(repository, journalAuditService, "Validation", Validation::getIdValidation, Validation::setIdValidation);
	}

}
