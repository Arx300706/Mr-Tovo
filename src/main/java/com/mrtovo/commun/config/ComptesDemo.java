package com.mrtovo.commun.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mrtovo.commun.entity.Compte;
import com.mrtovo.commun.entity.Role;
import com.mrtovo.commun.repository.CompteRepository;
import com.mrtovo.recrutement.entity.Candidat;
import com.mrtovo.recrutement.repository.CandidatRepository;

/**
 * Comptes de démo (un par rôle, même mot de passe), créés au démarrage s'ils n'existent pas.
 * Activé par {@code mrtovo.demo.comptes=true} : à désactiver en production.
 */
@Component
@ConditionalOnProperty(name = "mrtovo.demo.comptes", havingValue = "true")
public class ComptesDemo implements ApplicationRunner {

	public static final String MOT_DE_PASSE = "demo1234";

	private final CompteRepository compteRepository;
	private final CandidatRepository candidatRepository;
	private final PasswordEncoder passwordEncoder;

	public ComptesDemo(CompteRepository compteRepository, CandidatRepository candidatRepository,
			PasswordEncoder passwordEncoder) {
		this.compteRepository = compteRepository;
		this.candidatRepository = candidatRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		creer("admin", Role.ADMIN, null);
		creer("rh", Role.RH, null);
		creer("finance", Role.FINANCE, null);
		creer("manager1", Role.MANAGER, null);
		creer("manager2", Role.MANAGER, null);
		creer("EMP001", Role.EMPLOYE, null);

		if (!compteRepository.existsByIdentifiantIgnoreCase("candidat1@mrtovo.local")) {
			Candidat candidat = new Candidat();
			candidat.setNom("Rakoto");
			candidat.setPrenom("Candidat");
			candidat.setEmail("candidat1@mrtovo.local");
			creer(candidat.getEmail(), Role.CANDIDAT, candidatRepository.save(candidat).getIdCandidat());
		}
	}

	private void creer(String identifiant, Role role, Integer idCandidat) {
		if (compteRepository.existsByIdentifiantIgnoreCase(identifiant)) {
			return;
		}
		Compte compte = new Compte();
		compte.setIdentifiant(identifiant);
		compte.setMotDePasse(passwordEncoder.encode(MOT_DE_PASSE));
		compte.setRole(role);
		compte.setIdCandidat(idCandidat);
		compteRepository.save(compte);
	}

}
