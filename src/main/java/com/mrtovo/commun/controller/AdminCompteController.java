package com.mrtovo.commun.controller;

import java.util.Arrays;
import java.util.List;

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

import com.mrtovo.commun.dto.CompteFormulaire;
import com.mrtovo.commun.entity.Role;
import com.mrtovo.commun.securite.UtilisateurConnecte;
import com.mrtovo.commun.service.CompteService;
import com.mrtovo.commun.service.JournalAuditService;

import jakarta.validation.Valid;

/**
 * Administration (BK0-11) : comptes et journal d'audit. Réservé au rôle ADMIN (SecurityConfig).
 */
@Controller
@RequestMapping("/admin")
public class AdminCompteController {

	private static final int NOMBRE_LIGNES_AUDIT = 200;
	private static final String REDIRECTION_COMPTES = "redirect:/admin/comptes";

	private final CompteService compteService;
	private final JournalAuditService journalAuditService;

	public AdminCompteController(CompteService compteService, JournalAuditService journalAuditService) {
		this.compteService = compteService;
		this.journalAuditService = journalAuditService;
	}

	@GetMapping("/comptes")
	public String comptes(Model model) {
		if (!model.containsAttribute("formulaire")) {
			model.addAttribute("formulaire", new CompteFormulaire());
		}
		return pageComptes(model);
	}

	@PostMapping("/comptes")
	public String creer(@Valid @ModelAttribute("formulaire") CompteFormulaire formulaire, BindingResult resultat,
			Model model, RedirectAttributes redirectAttributes) {
		if (resultat.hasErrors()) {
			return pageComptes(model);
		}
		try {
			compteService.creerCompteEmploye(formulaire);
		}
		catch (IllegalArgumentException e) {
			model.addAttribute("erreur", e.getMessage());
			return pageComptes(model);
		}
		redirectAttributes.addFlashAttribute("succes", "Compte " + formulaire.getIdentifiant().trim() + " créé.");
		return REDIRECTION_COMPTES;
	}

	@PostMapping("/comptes/{id}/activer")
	public String activer(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte admin,
			RedirectAttributes redirectAttributes) {
		return executer(redirectAttributes, "Compte activé.", () -> compteService.changerActif(id, true, admin.getIdCompte()));
	}

	@PostMapping("/comptes/{id}/desactiver")
	public String desactiver(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte admin,
			RedirectAttributes redirectAttributes) {
		return executer(redirectAttributes, "Compte désactivé.", () -> compteService.changerActif(id, false, admin.getIdCompte()));
	}

	@PostMapping("/comptes/{id}/role")
	public String changerRole(@PathVariable Integer id, @RequestParam Role role,
			@AuthenticationPrincipal UtilisateurConnecte admin, RedirectAttributes redirectAttributes) {
		return executer(redirectAttributes, "Rôle modifié.", () -> compteService.changerRole(id, role, admin.getIdCompte()));
	}

	@PostMapping("/comptes/{id}/mot-de-passe")
	public String reinitialiserMotDePasse(@PathVariable Integer id, @RequestParam String motDePasse,
			RedirectAttributes redirectAttributes) {
		return executer(redirectAttributes, "Mot de passe réinitialisé.", () -> compteService.reinitialiserMotDePasse(id, motDePasse));
	}

	@GetMapping("/audit")
	public String audit(Model model) {
		model.addAttribute("title", "Journal d'audit");
		model.addAttribute("journaux", journalAuditService.derniers(NOMBRE_LIGNES_AUDIT));
		return "admin/audit";
	}

	private String pageComptes(Model model) {
		model.addAttribute("title", "Comptes");
		model.addAttribute("comptes", compteService.tous());
		model.addAttribute("roles", rolesEmploye());
		return "admin/comptes";
	}

	private String executer(RedirectAttributes redirectAttributes, String messageSucces, Runnable action) {
		try {
			action.run();
			redirectAttributes.addFlashAttribute("succes", messageSucces);
		}
		catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("erreur", e.getMessage());
		}
		return REDIRECTION_COMPTES;
	}

	private static List<Role> rolesEmploye() {
		return Arrays.stream(Role.values()).filter(r -> !r.estCandidat()).toList();
	}

}
