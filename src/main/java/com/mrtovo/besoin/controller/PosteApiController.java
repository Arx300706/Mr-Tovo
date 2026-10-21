package com.mrtovo.besoin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mrtovo.commun.controller.ApiCrudController;
import com.mrtovo.commun.service.JournalAuditService;
import com.mrtovo.besoin.entity.Poste;
import com.mrtovo.besoin.repository.PosteRepository;

@RestController
@RequestMapping("/api/postes")
public class PosteApiController extends ApiCrudController<Poste> {

	public PosteApiController(PosteRepository repository, JournalAuditService journalAuditService) {
		super(repository, journalAuditService, "Poste", Poste::getIdPoste, Poste::setIdPoste);
	}

}
