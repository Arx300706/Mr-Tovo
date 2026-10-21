package com.mrtovo.besoin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mrtovo.commun.controller.ApiCrudController;
import com.mrtovo.commun.service.JournalAuditService;
import com.mrtovo.besoin.entity.Departement;
import com.mrtovo.besoin.repository.DepartementRepository;

@RestController
@RequestMapping("/api/departements")
public class DepartementApiController extends ApiCrudController<Departement> {

	public DepartementApiController(DepartementRepository repository, JournalAuditService journalAuditService) {
		super(repository, journalAuditService, "Departement", Departement::getIdDepartement, Departement::setIdDepartement);
	}

}
