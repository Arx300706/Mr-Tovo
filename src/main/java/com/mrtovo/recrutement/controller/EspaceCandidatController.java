package com.mrtovo.recrutement.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mrtovo.commun.securite.UtilisateurConnecte;
import com.mrtovo.recrutement.service.CandidatureService;

/**
 * Espace candidat (rôle CANDIDAT) : la liste de ses candidatures et leur statut.
 */
@Controller
public class EspaceCandidatController {

	private final CandidatureService candidatureService;

	public EspaceCandidatController(CandidatureService candidatureService) {
		this.candidatureService = candidatureService;
	}

	@GetMapping("/espace-candidat")
	public String candidatures(@AuthenticationPrincipal UtilisateurConnecte utilisateur, Model model) {
		model.addAttribute("title", "Mes candidatures");
		model.addAttribute("candidatures", candidatureService.mesCandidatures(utilisateur));
		return "espace-candidat/candidatures";
	}

}
