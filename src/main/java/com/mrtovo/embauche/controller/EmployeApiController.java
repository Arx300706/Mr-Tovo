package com.mrtovo.embauche.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mrtovo.commun.controller.ApiCrudController;
import com.mrtovo.commun.service.JournalAuditService;
import com.mrtovo.embauche.entity.Employe;
import com.mrtovo.embauche.repository.EmployeRepository;

@RestController
@RequestMapping("/api/employes")
public class EmployeApiController extends ApiCrudController<Employe> {

	public EmployeApiController(EmployeRepository repository, JournalAuditService journalAuditService) {
		super(repository, journalAuditService, "Employe", Employe::getIdEmploye, Employe::setIdEmploye);
	}

}
