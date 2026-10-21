package com.mrtovo.recrutement.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.mrtovo.recrutement.entity.Candidature;
import com.mrtovo.recrutement.service.CandidatureService;

/**
 * Fiche candidature (RH et managers), en lecture.
 * Les actions (présélectionner, rejeter, retenir) arrivent avec BK2-05 via {@code actionsPossibles}.
 */
@Controller
public class CandidatureController {

	private final CandidatureService candidatureService;

	public CandidatureController(CandidatureService candidatureService) {
		this.candidatureService = candidatureService;
	}

	@GetMapping("/candidatures/{id}")
	public String detail(@PathVariable Integer id, Model model) {
		Candidature candidature = candidatureService.trouver(id);
		model.addAttribute("title", "Candidature de " + candidature.getCandidat().getNomComplet());
		model.addAttribute("candidature", candidature);
		model.addAttribute("entretiens", candidatureService.entretiens(id));
		model.addAttribute("evaluations", candidatureService.evaluations(id));
		model.addAttribute("noteMoyenne", candidatureService.noteMoyenne(id));
		// TODO BK2-05 : workflow de la candidature
		model.addAttribute("actionsPossibles", List.of());
		return "candidatures/detail";
	}

}
