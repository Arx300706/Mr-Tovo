package com.mrtovo.besoin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mrtovo.commun.controller.ApiCrudController;
import com.mrtovo.commun.service.JournalAuditService;
import com.mrtovo.besoin.entity.Service;
import com.mrtovo.besoin.repository.ServiceRepository;

@RestController
@RequestMapping("/api/services")
public class ServiceApiController extends ApiCrudController<Service> {

	public ServiceApiController(ServiceRepository repository, JournalAuditService journalAuditService) {
		super(repository, journalAuditService, "Service", Service::getIdService, Service::setIdService);
	}

}
