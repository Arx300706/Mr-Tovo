package com.mrtovo.embauche.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mrtovo.commun.controller.ApiCrudController;
import com.mrtovo.commun.service.JournalAuditService;
import com.mrtovo.embauche.entity.OnboardingTache;
import com.mrtovo.embauche.repository.OnboardingTacheRepository;

@RestController
@RequestMapping("/api/onboarding-taches")
public class OnboardingTacheApiController extends ApiCrudController<OnboardingTache> {

	public OnboardingTacheApiController(OnboardingTacheRepository repository, JournalAuditService journalAuditService) {
		super(repository, journalAuditService, "OnboardingTache", OnboardingTache::getIdOnboardingTache, OnboardingTache::setIdOnboardingTache);
	}

}
