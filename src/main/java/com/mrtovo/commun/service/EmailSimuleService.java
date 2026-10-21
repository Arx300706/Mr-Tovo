package com.mrtovo.commun.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mrtovo.commun.entity.Compte;
import com.mrtovo.commun.entity.Role;
import com.mrtovo.commun.repository.CompteRepository;

/**
 * Envoi d'e-mails <b>simulé</b> : le message est écrit dans les logs (logger « EMAIL »).
 * Ne jamais y mettre de mot de passe ni de donnée sensible.
 */
@Service
public class EmailSimuleService {

	private static final Logger LOG = LoggerFactory.getLogger("EMAIL");

	private final CompteRepository compteRepository;

	public EmailSimuleService(CompteRepository compteRepository) {
		this.compteRepository = compteRepository;
	}

	public void envoyer(String destinataire, String sujet, String corps) {
		LOG.info("""

			===== E-MAIL SIMULÉ =====
			À      : {}
			Sujet  : {}
			{}
			=========================""", destinataire, sujet, corps);
	}

	/** Envoie le même message à tous les comptes actifs d'un rôle (ex. toute l'équipe RH). */
	public void envoyerAuRole(Role role, String sujet, String corps) {
		for (Compte compte : compteRepository.findByRoleAndActifTrue(role)) {
			envoyer(compte.getIdentifiant(), sujet, corps);
		}
	}

}
