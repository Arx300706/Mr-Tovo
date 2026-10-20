package com.mrtovo.commun.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mrtovo.commun.dto.InscriptionFormulaire;
import com.mrtovo.commun.service.CompteDetailsService;
import com.mrtovo.commun.service.InscriptionService;

import jakarta.validation.Valid;

/**
 * Connexion à deux entrées (employé / candidat) et création de compte candidat.
 * Le POST /connexion et /deconnexion sont traités par Spring Security (voir SecurityConfig).
 */
@Controller
public class AuthController {

	private final InscriptionService inscriptionService;

	public AuthController(InscriptionService inscriptionService) {
		this.inscriptionService = inscriptionService;
	}

	@GetMapping("/connexion")
	public String connexion(@RequestParam(name = "espace", defaultValue = CompteDetailsService.ESPACE_EMPLOYE) String espace,
			Authentication authentication, Model model) {
		if (estConnecte(authentication)) {
			return "redirect:/";
		}
		model.addAttribute("title", "Connexion");
		model.addAttribute("espace", CompteDetailsService.ESPACE_CANDIDAT.equals(espace)
			? CompteDetailsService.ESPACE_CANDIDAT
			: CompteDetailsService.ESPACE_EMPLOYE);
		return "auth/connexion";
	}

	@GetMapping("/inscription")
	public String inscription(Authentication authentication, Model model) {
		if (estConnecte(authentication)) {
			return "redirect:/";
		}
		model.addAttribute("title", "Créer un compte candidat");
		model.addAttribute("formulaire", new InscriptionFormulaire());
		return "auth/inscription";
	}

	@PostMapping("/inscription")
	public String inscrire(@Valid @ModelAttribute("formulaire") InscriptionFormulaire formulaire,
			BindingResult resultat, Model model, RedirectAttributes redirectAttributes) {
		if (!resultat.hasFieldErrors("email") && inscriptionService.emailDejaUtilise(formulaire.getEmail().trim())) {
			resultat.rejectValue("email", "email.utilise", "Un compte existe déjà avec cet e-mail");
		}
		if (resultat.hasErrors()) {
			model.addAttribute("title", "Créer un compte candidat");
			return "auth/inscription";
		}
		inscriptionService.inscrireCandidat(formulaire);
		redirectAttributes.addFlashAttribute("succes", "Votre compte est créé, vous pouvez vous connecter.");
		return "redirect:/connexion?espace=" + CompteDetailsService.ESPACE_CANDIDAT;
	}

	private static boolean estConnecte(Authentication authentication) {
		return authentication != null && authentication.isAuthenticated()
			&& !(authentication instanceof AnonymousAuthenticationToken);
	}

}
