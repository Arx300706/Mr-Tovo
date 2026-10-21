package com.mrtovo.recrutement.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.mrtovo.commun.entity.Role;
import com.mrtovo.commun.securite.UtilisateurConnecte;
import com.mrtovo.recrutement.entity.Offre;
import com.mrtovo.recrutement.entity.StatutOffre;

/**
 * Transitions autorisées d'une offre (section 3). Tout ce qui n'est pas listé est interdit.
 * À rebrancher sur le moteur de statuts commun (BK0-03) quand il sera livré.
 */
@Component
public class OffreWorkflow {

	public static final String MODIFIER = "MODIFIER";
	public static final String PUBLIER = "PUBLIER";
	public static final String CLOTURER = "CLOTURER";
	public static final String ANNULER = "ANNULER";

	/** Action, statut de départ, statut d'arrivée (null = l'action ne change pas le statut), rôles autorisés. */
	private record Transition(String action, String de, String vers, Set<Role> roles) {
	}

	private static final List<Transition> TRANSITIONS = List.of(
		new Transition(MODIFIER, StatutOffre.BROUILLON, null, Set.of(Role.RH)),
		new Transition(PUBLIER, StatutOffre.BROUILLON, StatutOffre.PUBLIEE, Set.of(Role.RH)),
		new Transition(CLOTURER, StatutOffre.PUBLIEE, StatutOffre.CLOTUREE, Set.of(Role.RH)),
		new Transition(ANNULER, StatutOffre.PUBLIEE, StatutOffre.ANNULEE, Set.of(Role.RH)));

	/** Codes des actions possibles pour cet utilisateur sur cette offre ({@code actionsPossibles}). */
	public List<String> actionsPossibles(Offre offre, UtilisateurConnecte utilisateur) {
		return TRANSITIONS.stream()
			.filter(t -> offre.getStatutOffre().est(t.de()) && autorise(t, utilisateur))
			.map(Transition::action)
			.toList();
	}

	/**
	 * Vérifie l'action et renvoie le libellé du statut d'arrivée (null si le statut ne change pas).
	 * @throws IllegalArgumentException avec un message à afficher si l'action est interdite
	 */
	public String verifier(Offre offre, String action, UtilisateurConnecte utilisateur, LocalDate aujourdhui) {
		Transition transition = TRANSITIONS.stream()
			.filter(t -> t.action().equals(action) && offre.getStatutOffre().est(t.de()))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException(
				"Action impossible : l'offre est « " + offre.getStatutOffre().getLibelle() + " »"));
		if (!autorise(transition, utilisateur)) {
			throw new IllegalArgumentException("Vous n'avez pas le droit de faire cette action");
		}
		if (PUBLIER.equals(action)) {
			verifierPublication(offre, aujourdhui);
		}
		return transition.vers();
	}

	private static void verifierPublication(Offre offre, LocalDate aujourdhui) {
		if (estVide(offre.getDescription()) || offre.getTypeContrat() == null) {
			throw new IllegalArgumentException("Pour publier, il faut une description et un type de contrat");
		}
		if (offre.getDateLimite() != null && offre.getDateLimite().isBefore(aujourdhui)) {
			throw new IllegalArgumentException("La date limite est déjà passée");
		}
	}

	private static boolean autorise(Transition transition, UtilisateurConnecte utilisateur) {
		return utilisateur != null && transition.roles().contains(utilisateur.getRole());
	}

	private static boolean estVide(String texte) {
		return texte == null || texte.isBlank();
	}

}
