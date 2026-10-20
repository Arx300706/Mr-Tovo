package com.mrtovo.recrutement.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mrtovo.commun.securite.UtilisateurConnecte;
import com.mrtovo.recrutement.dto.OffreFormulaire;
import com.mrtovo.recrutement.entity.Offre;
import com.mrtovo.recrutement.service.CandidatureService;
import com.mrtovo.recrutement.service.OffreService;
import com.mrtovo.recrutement.service.OffreWorkflow;

import jakarta.validation.Valid;

/**
 * Offres côté RH : liste, détail, brouillon, publier, clôturer, annuler,
 * et la liste des candidatures d'une offre (RH et managers).
 */
@Controller
@RequestMapping("/offres")
public class OffreController {

	private final OffreService offreService;
	private final CandidatureService candidatureService;

	public OffreController(OffreService offreService, CandidatureService candidatureService) {
		this.offreService = offreService;
		this.candidatureService = candidatureService;
	}

	@GetMapping
	public String liste(@RequestParam(required = false) String statut, Model model) {
		model.addAttribute("title", "Offres");
		model.addAttribute("offres", offreService.toutes(statut));
		model.addAttribute("nombresCandidatures", offreService.nombresCandidatures());
		model.addAttribute("statuts", offreService.statuts());
		model.addAttribute("statut", statut);
		return "offres/liste";
	}

	@GetMapping("/nouveau")
	public String nouveau(Model model) {
		model.addAttribute("formulaire", new OffreFormulaire());
		return pageFormulaire(null, model);
	}

	@PostMapping("/nouveau")
	public String creer(@Valid @ModelAttribute("formulaire") OffreFormulaire formulaire, BindingResult resultat,
			Model model, RedirectAttributes redirectAttributes) {
		if (resultat.hasErrors()) {
			return pageFormulaire(null, model);
		}
		Offre offre = offreService.creerBrouillon(formulaire, null);
		redirectAttributes.addFlashAttribute("succes", "Brouillon enregistré.");
		return "redirect:/offres/" + offre.getIdOffre();
	}

	@GetMapping("/{id}")
	public String detail(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte utilisateur,
			Model model) {
		Offre offre = offreService.trouver(id);
		model.addAttribute("title", offre.getTitre());
		model.addAttribute("offre", offre);
		model.addAttribute("actionsPossibles", offreService.actionsPossibles(offre, utilisateur));
		model.addAttribute("nombreCandidatures", offreService.nombreCandidatures(id));
		return "offres/detail";
	}

	@GetMapping("/{id}/modifier")
	public String modifier(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte utilisateur,
			Model model, RedirectAttributes redirectAttributes) {
		Offre offre = offreService.trouver(id);
		if (!offreService.actionsPossibles(offre, utilisateur).contains(OffreWorkflow.MODIFIER)) {
			redirectAttributes.addFlashAttribute("erreur", "Seule une offre en brouillon peut être modifiée");
			return "redirect:/offres/" + id;
		}
		model.addAttribute("formulaire", OffreFormulaire.depuis(offre));
		return pageFormulaire(offre, model);
	}

	@PostMapping("/{id}/modifier")
	public String enregistrer(@PathVariable Integer id, @Valid @ModelAttribute("formulaire") OffreFormulaire formulaire,
			BindingResult resultat, @AuthenticationPrincipal UtilisateurConnecte utilisateur, Model model,
			RedirectAttributes redirectAttributes) {
		if (resultat.hasErrors()) {
			return pageFormulaire(offreService.trouver(id), model);
		}
		try {
			offreService.modifierBrouillon(id, formulaire, utilisateur);
			redirectAttributes.addFlashAttribute("succes", "Offre enregistrée.");
		}
		catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("erreur", e.getMessage());
		}
		return "redirect:/offres/" + id;
	}

	@PostMapping("/{id}/publier")
	public String publier(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte utilisateur,
			RedirectAttributes redirectAttributes) {
		return changerStatut(id, OffreWorkflow.PUBLIER, "Offre publiée : elle est visible sur la page carrières.",
			utilisateur, redirectAttributes);
	}

	@PostMapping("/{id}/cloturer")
	public String cloturer(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte utilisateur,
			RedirectAttributes redirectAttributes) {
		return changerStatut(id, OffreWorkflow.CLOTURER, "Offre clôturée.", utilisateur, redirectAttributes);
	}

	@PostMapping("/{id}/annuler")
	public String annuler(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte utilisateur,
			RedirectAttributes redirectAttributes) {
		return changerStatut(id, OffreWorkflow.ANNULER, "Offre annulée.", utilisateur, redirectAttributes);
	}

	@GetMapping("/{id}/candidatures")
	public String candidatures(@PathVariable Integer id, @RequestParam(required = false) String statut, Model model) {
		Offre offre = offreService.trouver(id);
		model.addAttribute("title", "Candidatures : " + offre.getTitre());
		model.addAttribute("offre", offre);
		model.addAttribute("candidatures", candidatureService.deLOffre(id, statut));
		model.addAttribute("statuts", candidatureService.statuts());
		model.addAttribute("statut", statut);
		return "candidatures/liste";
	}

	private String changerStatut(Integer id, String action, String messageSucces, UtilisateurConnecte utilisateur,
			RedirectAttributes redirectAttributes) {
		try {
			offreService.changerStatut(id, action, utilisateur);
			redirectAttributes.addFlashAttribute("succes", messageSucces);
		}
		catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("erreur", e.getMessage());
		}
		return "redirect:/offres/" + id;
	}

	private String pageFormulaire(Offre offre, Model model) {
		model.addAttribute("title", offre == null ? "Nouvelle offre" : "Modifier l'offre");
		model.addAttribute("offre", offre);
		model.addAttribute("typesContrat", offreService.typesContrat());
		return "offres/formulaire";
	}

}
