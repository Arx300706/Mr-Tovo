package com.mrtovo.commun.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.mrtovo.commun.securite.UtilisateurConnecte;

/**
 * Met l'utilisateur connecté à disposition de toutes les pages sous le nom {@code utilisateur}
 * (null si personne n'est connecté). Exemple : {@code ${utilisateur?.nomAffiche}}, {@code ${utilisateur?.role}}.
 */
@ControllerAdvice
public class UtilisateurConnecteAdvice {

	@ModelAttribute("utilisateur")
	public UtilisateurConnecte utilisateur(@AuthenticationPrincipal UtilisateurConnecte utilisateur) {
		return utilisateur;
	}

}
