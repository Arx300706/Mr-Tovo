package com.mrtovo.commun.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mrtovo.commun.dto.CompteFormulaire;
import com.mrtovo.commun.entity.Compte;
import com.mrtovo.commun.entity.Role;
import com.mrtovo.commun.repository.CompteRepository;

/**
 * Gestion des comptes par l'admin. Chaque action est tracée dans le journal d'audit.
 * Les erreurs métier sont des {@link IllegalArgumentException} avec un message à afficher.
 */
@Service
public class CompteService {

	private static final String OBJET = "Compte";

	private final CompteRepository compteRepository;
	private final PasswordEncoder passwordEncoder;
	private final JournalAuditService journalAuditService;
	private final EmailSimuleService emailSimuleService;

	public CompteService(CompteRepository compteRepository, PasswordEncoder passwordEncoder,
			JournalAuditService journalAuditService, EmailSimuleService emailSimuleService) {
		this.compteRepository = compteRepository;
		this.passwordEncoder = passwordEncoder;
		this.journalAuditService = journalAuditService;
		this.emailSimuleService = emailSimuleService;
	}

	@Transactional(readOnly = true)
	public List<Compte> tous() {
		return compteRepository.findAllByOrderByRoleAscIdentifiantAsc();
	}

	@Transactional
	public Compte creerCompteEmploye(CompteFormulaire formulaire) {
		String identifiant = formulaire.getIdentifiant().trim();
		if (formulaire.getRole() == Role.CANDIDAT) {
			throw new IllegalArgumentException("Un compte candidat se crée depuis la page d'inscription");
		}
		if (compteRepository.existsByIdentifiantIgnoreCase(identifiant)) {
			throw new IllegalArgumentException("Un compte existe déjà avec l'identifiant " + identifiant);
		}
		Compte compte = new Compte();
		compte.setIdentifiant(identifiant);
		compte.setRole(formulaire.getRole());
		compte.setIdEmploye(formulaire.getIdEmploye());
		compte.setMotDePasse(passwordEncoder.encode(formulaire.getMotDePasse()));
		compte = compteRepository.save(compte);

		journalAuditService.enregistrer("CREER", OBJET, compte.getIdCompte(), identifiant + " (" + compte.getRole() + ")");
		emailSimuleService.envoyer(identifiant, "Votre compte Mr Tovo est créé",
			"Connectez-vous avec votre matricule " + identifiant + ". Votre mot de passe initial vous est remis par l'administrateur.");
		return compte;
	}

	@Transactional
	public Compte changerActif(Integer idCompte, boolean actif, Integer idCompteAdmin) {
		Compte compte = trouver(idCompte);
		if (!actif && compte.getIdCompte().equals(idCompteAdmin)) {
			throw new IllegalArgumentException("Vous ne pouvez pas désactiver votre propre compte");
		}
		compte.setActif(actif);
		journalAuditService.enregistrer(actif ? "ACTIVER" : "DESACTIVER", OBJET, idCompte, compte.getIdentifiant());
		emailSimuleService.envoyer(compte.getIdentifiant(), "Votre compte Mr Tovo",
			actif ? "Votre compte a été réactivé." : "Votre compte a été désactivé. Contactez les RH si besoin.");
		return compte;
	}

	@Transactional
	public Compte changerRole(Integer idCompte, Role role, Integer idCompteAdmin) {
		Compte compte = trouver(idCompte);
		if (compte.getIdCompte().equals(idCompteAdmin)) {
			throw new IllegalArgumentException("Vous ne pouvez pas changer votre propre rôle");
		}
		if (compte.getRole().estCandidat() != role.estCandidat()) {
			throw new IllegalArgumentException("Un compte candidat ne peut pas devenir un compte employé, et inversement");
		}
		Role ancien = compte.getRole();
		compte.setRole(role);
		journalAuditService.enregistrer("CHANGER_ROLE", OBJET, idCompte, compte.getIdentifiant() + " : " + ancien + " → " + role);
		return compte;
	}

	@Transactional
	public Compte reinitialiserMotDePasse(Integer idCompte, String motDePasse) {
		if (motDePasse == null || motDePasse.length() < 8) {
			throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères");
		}
		Compte compte = trouver(idCompte);
		compte.setMotDePasse(passwordEncoder.encode(motDePasse));
		journalAuditService.enregistrer("REINITIALISER_MDP", OBJET, idCompte, compte.getIdentifiant());
		emailSimuleService.envoyer(compte.getIdentifiant(), "Mot de passe réinitialisé",
			"Votre mot de passe Mr Tovo a été réinitialisé par l'administrateur.");
		return compte;
	}

	private Compte trouver(Integer idCompte) {
		return compteRepository.findById(idCompte)
			.orElseThrow(() -> new IllegalArgumentException("Compte introuvable"));
	}

}
