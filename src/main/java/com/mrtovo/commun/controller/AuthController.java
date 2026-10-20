package com.mrtovo.commun.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mrtovo.commun.dto.InscriptionFormulaire;
import com.mrtovo.commun.entity.Compte;
import com.mrtovo.commun.securite.UtilisateurConnecte;
import com.mrtovo.commun.service.CompteDetailsService;
import com.mrtovo.commun.service.InscriptionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * Connexion à deux entrées (employé / candidat) et création de compte candidat.
 * Le POST /connexion et /deconnexion sont traités par Spring Security (voir SecurityConfig).
 * Après l'inscription, le candidat est connecté directement et revient sur l'offre qu'il voulait (s'il y en a une).
 */
@Controller
public class AuthController {

	private final InscriptionService inscriptionService;
	private final CompteDetailsService compteDetailsService;
	private final RequestCache requestCache = new HttpSessionRequestCache();
	private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

	public AuthController(InscriptionService inscriptionService, CompteDetailsService compteDetailsService) {
		this.inscriptionService = inscriptionService;
		this.compteDetailsService = compteDetailsService;
	}

	@GetMapping("/connexion")
	public String connexion(@RequestParam(name = "espace", required = false) String espace,
			Authentication authentication, HttpServletRequest request, HttpServletResponse response, Model model) {
		if (estConnecte(authentication)) {
			return "redirect:/";
		}
		if (espace == null) {
			espace = pageCandidatDemandee(request, response) ? CompteDetailsService.ESPACE_CANDIDAT
				: CompteDetailsService.ESPACE_EMPLOYE;
		}
		model.addAttribute("title", "Connexion");
		model.addAttribute("espace", CompteDetailsService.ESPACE_CANDIDAT.equals(espace)
			? CompteDetailsService.ESPACE_CANDIDAT
			: CompteDetailsService.ESPACE_EMPLOYE);
		return "auth/connexion";
	}

	/** @param offre offre sur laquelle revenir après l'inscription (bouton « Créer un compte » d'une offre) */
	@GetMapping("/inscription")
	public String inscription(@RequestParam(required = false) Integer offre, Authentication authentication,
			Model model) {
		if (estConnecte(authentication)) {
			return "redirect:/";
		}
		model.addAttribute("title", "Créer un compte candidat");
		model.addAttribute("formulaire", new InscriptionFormulaire());
		model.addAttribute("offre", offre);
		return "auth/inscription";
	}

	@PostMapping("/inscription")
	public String inscrire(@Valid @ModelAttribute("formulaire") InscriptionFormulaire formulaire,
			BindingResult resultat, @RequestParam(required = false) Integer offre, HttpServletRequest request,
			HttpServletResponse response, Model model, RedirectAttributes redirectAttributes) {
		if (!resultat.hasFieldErrors("email") && inscriptionService.emailDejaUtilise(formulaire.getEmail().trim())) {
			resultat.rejectValue("email", "email.utilise", "Un compte existe déjà avec cet e-mail");
		}
		if (resultat.hasErrors()) {
			model.addAttribute("title", "Créer un compte candidat");
			model.addAttribute("offre", offre);
			return "auth/inscription";
		}
		Compte compte = inscriptionService.inscrireCandidat(formulaire);
		connecter(compte.getIdentifiant(), request, response);
		redirectAttributes.addFlashAttribute("succes", "Votre compte est créé, vous êtes connecté.");
		return "redirect:" + pageApresInscription(offre, request, response);
	}

	/** Ouvre la session du candidat qui vient de s'inscrire (comme après une connexion). */
	private void connecter(String identifiant, HttpServletRequest request, HttpServletResponse response) {
		UtilisateurConnecte utilisateur = compteDetailsService.loadUserByUsername(identifiant);
		Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(utilisateur, null,
			utilisateur.getAuthorities());
		request.getSession();
		request.changeSessionId();
		SecurityContext contexte = SecurityContextHolder.createEmptyContext();
		contexte.setAuthentication(authentication);
		SecurityContextHolder.setContext(contexte);
		securityContextRepository.saveContext(contexte, request, response);
	}

	private String pageApresInscription(Integer offre, HttpServletRequest request, HttpServletResponse response) {
		if (offre != null) {
			return "/carrieres/" + offre + "/candidater";
		}
		SavedRequest pageDemandee = requestCache.getRequest(request, response);
		if (pageDemandee != null) {
			requestCache.removeRequest(request, response);
			return pageDemandee.getRedirectUrl();
		}
		return "/espace-candidat";
	}

	/** Vrai si l'utilisateur a été envoyé ici depuis une page candidat (« Candidater », espace candidat). */
	private boolean pageCandidatDemandee(HttpServletRequest request, HttpServletResponse response) {
		SavedRequest pageDemandee = requestCache.getRequest(request, response);
		if (pageDemandee == null) {
			return false;
		}
		String url = pageDemandee.getRedirectUrl();
		return url.contains("/carrieres/") || url.contains("/espace-candidat");
	}

	private static boolean estConnecte(Authentication authentication) {
		return authentication != null && authentication.isAuthenticated()
			&& !(authentication instanceof AnonymousAuthenticationToken);
	}

}
