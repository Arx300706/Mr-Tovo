package com.mrtovo.commun.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mrtovo.commun.dto.InscriptionFormulaire;
import com.mrtovo.commun.entity.Compte;
import com.mrtovo.commun.entity.Role;
import com.mrtovo.commun.repository.CompteRepository;
import com.mrtovo.recrutement.entity.Candidat;
import com.mrtovo.recrutement.repository.CandidatRepository;

/**
 * Création d'un compte candidat : un Candidat + son Compte (identifiant = e-mail).
 */
@Service
public class InscriptionService {

	private final CompteRepository compteRepository;
	private final CandidatRepository candidatRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailSimuleService emailSimuleService;

	public InscriptionService(CompteRepository compteRepository, CandidatRepository candidatRepository,
			PasswordEncoder passwordEncoder, EmailSimuleService emailSimuleService) {
		this.compteRepository = compteRepository;
		this.candidatRepository = candidatRepository;
		this.passwordEncoder = passwordEncoder;
		this.emailSimuleService = emailSimuleService;
	}

	public boolean emailDejaUtilise(String email) {
		return compteRepository.existsByIdentifiantIgnoreCase(email)
			|| candidatRepository.existsByEmailIgnoreCase(email);
	}

	@Transactional
	public Compte inscrireCandidat(InscriptionFormulaire formulaire) {
		String email = formulaire.getEmail().trim().toLowerCase();

		Candidat candidat = new Candidat();
		candidat.setNom(formulaire.getNom().trim());
		candidat.setPrenom(formulaire.getPrenom().trim());
		candidat.setEmail(email);
		candidat.setTelephone(formulaire.getTelephone());
		candidat = candidatRepository.save(candidat);

		Compte compte = new Compte();
		compte.setIdentifiant(email);
		compte.setMotDePasse(passwordEncoder.encode(formulaire.getMotDePasse()));
		compte.setRole(Role.CANDIDAT);
		compte.setIdCandidat(candidat.getIdCandidat());
		compte = compteRepository.save(compte);

		emailSimuleService.envoyer(email, "Bienvenue sur Mr Tovo",
			"Bonjour " + candidat.getPrenom() + ", votre compte candidat est créé. Votre identifiant : " + email + ".");
		return compte;
	}

}
