package com.mrtovo.commun.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.mrtovo.commun.entity.Compte;
import com.mrtovo.commun.entity.Employe;
import com.mrtovo.commun.repository.CompteRepository;
import com.mrtovo.commun.repository.EmployeRepository;
import com.mrtovo.commun.securite.UtilisateurConnecte;
import com.mrtovo.recrutement.repository.CandidatRepository;

/**
 * Charge un compte pour Spring Security.
 * La page de connexion a deux entrées (champ caché {@code espace}) :
 * un candidat ne peut pas passer par l'entrée « employé », et inversement.
 */
@Service
public class CompteDetailsService implements UserDetailsService {

	public static final String PARAM_ESPACE = "espace";
	public static final String ESPACE_EMPLOYE = "employe";
	public static final String ESPACE_CANDIDAT = "candidat";

	private final CompteRepository compteRepository;
	private final EmployeRepository employeRepository;
	private final CandidatRepository candidatRepository;

	public CompteDetailsService(CompteRepository compteRepository, EmployeRepository employeRepository,
			CandidatRepository candidatRepository) {
		this.compteRepository = compteRepository;
		this.employeRepository = employeRepository;
		this.candidatRepository = candidatRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public UtilisateurConnecte loadUserByUsername(String identifiant) {
		String espace = espaceDemande();
		String identifiantNettoye = identifiant.trim();
		if (ESPACE_EMPLOYE.equals(espace)) {
			Employe employe = employeRepository.findByMatriculeIgnoreCase(identifiantNettoye)
				.orElseThrow(() -> new UsernameNotFoundException("Employé introuvable"));
			return new UtilisateurConnecte(employe);
		}
		if (!ESPACE_CANDIDAT.equals(espace)) {
			throw new UsernameNotFoundException("Espace de connexion invalide");
		}

		Compte compte = compteRepository.findByIdentifiantIgnoreCase(identifiantNettoye)
			.orElseThrow(() -> new UsernameNotFoundException("Compte introuvable"));
		if (!compte.getRole().estCandidat()) {
			throw new UsernameNotFoundException("Compte introuvable pour cet espace");
		}

		return new UtilisateurConnecte(compte, nomAffiche(compte));
	}

	private String nomAffiche(Compte compte) {
		if (compte.getIdCandidat() != null) {
			return candidatRepository.findById(compte.getIdCandidat())
				.map(c -> c.getPrenom() + " " + c.getNom())
				.orElse(compte.getIdentifiant());
		}
		// TODO BK3 : afficher prénom + nom de l'employé quand l'entité Employe existe
		return compte.getIdentifiant();
	}

	private static String espaceDemande() {
		if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributs) {
			return attributs.getRequest().getParameter(PARAM_ESPACE);
		}
		return null;
	}

}
