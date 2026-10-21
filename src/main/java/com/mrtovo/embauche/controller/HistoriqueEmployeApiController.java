package com.mrtovo.embauche.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mrtovo.commun.controller.ApiCrudController;
import com.mrtovo.commun.service.JournalAuditService;
import com.mrtovo.embauche.entity.HistoriqueEmploye;
import com.mrtovo.embauche.repository.HistoriqueEmployeRepository;

@RestController
@RequestMapping("/api/historiques-employe")
public class HistoriqueEmployeApiController extends ApiCrudController<HistoriqueEmploye> {

	public HistoriqueEmployeApiController(HistoriqueEmployeRepository repository, JournalAuditService journalAuditService) {
		super(repository, journalAuditService, "HistoriqueEmploye", HistoriqueEmploye::getIdHistoriqueEmploye, HistoriqueEmploye::setIdHistoriqueEmploye);
	}

}
