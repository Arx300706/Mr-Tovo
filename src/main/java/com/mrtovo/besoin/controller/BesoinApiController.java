package com.mrtovo.besoin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mrtovo.commun.controller.ApiCrudController;
import com.mrtovo.commun.service.JournalAuditService;
import com.mrtovo.besoin.entity.Besoin;
import com.mrtovo.besoin.repository.BesoinRepository;

@RestController
@RequestMapping("/api/besoins")
public class BesoinApiController extends ApiCrudController<Besoin> {

	public BesoinApiController(BesoinRepository repository, JournalAuditService journalAuditService) {
		super(repository, journalAuditService, "Besoin", Besoin::getIdBesoin, Besoin::setIdBesoin);
	}

}
