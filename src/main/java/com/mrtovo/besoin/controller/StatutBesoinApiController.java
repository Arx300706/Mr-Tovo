package com.mrtovo.besoin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mrtovo.commun.controller.ApiCrudController;
import com.mrtovo.commun.service.JournalAuditService;
import com.mrtovo.besoin.entity.StatutBesoin;
import com.mrtovo.besoin.repository.StatutBesoinRepository;

@RestController
@RequestMapping("/api/statuts-besoin")
public class StatutBesoinApiController extends ApiCrudController<StatutBesoin> {

	public StatutBesoinApiController(StatutBesoinRepository repository, JournalAuditService journalAuditService) {
		super(repository, journalAuditService, "StatutBesoin", StatutBesoin::getIdStatutBesoin, StatutBesoin::setIdStatutBesoin);
	}

}
