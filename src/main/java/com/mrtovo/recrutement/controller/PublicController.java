package com.mrtovo.recrutement.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mrtovo.commun.securite.UtilisateurConnecte;
import com.mrtovo.recrutement.dto.CandidatureFormulaire;
import com.mrtovo.recrutement.entity.Offre;
import com.mrtovo.recrutement.service.CandidatureService;
import com.mrtovo.recrutement.service.OffreService;

import jakarta.validation.Valid;

/**
 * Pages publiques (sans connexion) : accueil, offres publiées, détail et dépôt de candidature.
 * « Candidater » et « Postuler » demandent un compte candidat (SecurityConfig) : après connexion
 * ou inscription, le candidat revient sur le formulaire.
 */
@Controller
public class PublicController {

	private static final int NOMBRE_OFFRES_ACCUEIL = 6;

	private final OffreService offreService;
	private final CandidatureService candidatureService;

	public PublicController(OffreService offreService, CandidatureService candidatureService) {
		this.offreService = offreService;
		this.candidatureService = candidatureService;
	}

	@GetMapping("/")
	public String accueil(Model model) {
		model.addAttribute("title", "Accueil");
		model.addAttribute("offres", offreService.visibles(null, null).stream().limit(NOMBRE_OFFRES_ACCUEIL).toList());
		return "public/accueil";
	}

	@GetMapping("/carrieres")
	public String offres(@RequestParam(required = false) String q, @RequestParam(required = false) Integer typeContrat,
			Model model) {
		model.addAttribute("title", "Offres d'emploi");
		model.addAttribute("offres", offreService.visibles(q, typeContrat));
		model.addAttribute("typesContrat", offreService.typesContrat());
		model.addAttribute("q", q);
		model.addAttribute("typeContrat", typeContrat);
		return "public/offres";
	}

	@GetMapping("/carrieres/{id:\\d+}")
	public String detail(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte utilisateur,
			Model model) {
		Offre offre = offreService.trouverVisible(id);
		if (!model.containsAttribute("formulaire")) {
			model.addAttribute("formulaire", new CandidatureFormulaire());
		}
		return pageDetail(offre, utilisateur, model);
	}

	/** Bouton « Candidater » : passe par la connexion si besoin, puis revient sur le formulaire. */
	@GetMapping("/carrieres/{id:\\d+}/candidater")
	public String candidater(@PathVariable Integer id) {
		return "redirect:/carrieres/" + id + "#postuler";
	}

	@PostMapping("/carrieres/{id:\\d+}/postuler")
	public String postuler(@PathVariable Integer id, @Valid @ModelAttribute("formulaire") CandidatureFormulaire formulaire,
			BindingResult resultat, @AuthenticationPrincipal UtilisateurConnecte utilisateur, Model model,
			RedirectAttributes redirectAttributes) {
		Offre offre = offreService.trouverVisible(id);
		String erreurCv = CandidatureService.erreurCv(formulaire.getCv());
		if (erreurCv != null) {
			resultat.rejectValue("cv", "cv.invalide", erreurCv);
		}
		if (resultat.hasErrors()) {
			return pageDetail(offre, utilisateur, model);
		}
		try {
			candidatureService.postuler(id, utilisateur, formulaire);
		}
		catch (IllegalArgumentException e) {
			model.addAttribute("erreur", e.getMessage());
			return pageDetail(offre, utilisateur, model);
		}
		redirectAttributes.addFlashAttribute("offre", offre);
		return "redirect:/carrieres/merci";
	}

	@GetMapping("/carrieres/merci")
	public String merci(Model model) {
		model.addAttribute("title", "Candidature envoyée");
		return "public/confirmation";
	}

	private String pageDetail(Offre offre, UtilisateurConnecte utilisateur, Model model) {
		model.addAttribute("title", offre.getTitre());
		model.addAttribute("offre", offre);
		model.addAttribute("dejaPostule", candidatureService.aDejaPostule(offre.getIdOffre(), utilisateur));
		return "public/offre-detail";
	}

}
