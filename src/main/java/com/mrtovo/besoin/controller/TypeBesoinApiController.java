package com.mrtovo.besoin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mrtovo.commun.controller.ApiCrudController;
import com.mrtovo.commun.service.JournalAuditService;
import com.mrtovo.besoin.entity.TypeBesoin;
import com.mrtovo.besoin.repository.TypeBesoinRepository;

@RestController
@RequestMapping("/api/types-besoin")
public class TypeBesoinApiController extends ApiCrudController<TypeBesoin> {

	public TypeBesoinApiController(TypeBesoinRepository repository, JournalAuditService journalAuditService) {
		super(repository, journalAuditService, "TypeBesoin", TypeBesoin::getIdTypeBesoin, TypeBesoin::setIdTypeBesoin);
	}

}
