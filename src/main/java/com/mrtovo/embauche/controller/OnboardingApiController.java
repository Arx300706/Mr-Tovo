package com.mrtovo.embauche.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mrtovo.commun.controller.ApiCrudController;
import com.mrtovo.commun.service.JournalAuditService;
import com.mrtovo.embauche.entity.Onboarding;
import com.mrtovo.embauche.repository.OnboardingRepository;

@RestController
@RequestMapping("/api/onboardings")
public class OnboardingApiController extends ApiCrudController<Onboarding> {

	public OnboardingApiController(OnboardingRepository repository, JournalAuditService journalAuditService) {
		super(repository, journalAuditService, "Onboarding", Onboarding::getIdOnboarding, Onboarding::setIdOnboarding);
	}

}
