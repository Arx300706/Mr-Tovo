package com.mrtovo.commun.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.mrtovo.commun.dto.CompteFormulaire;
import com.mrtovo.commun.entity.Compte;
import com.mrtovo.commun.entity.Role;
import com.mrtovo.commun.repository.CompteRepository;

class CompteServiceTests {

	private final CompteRepository compteRepository = mock(CompteRepository.class);
	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	private final CompteService service = new CompteService(compteRepository, passwordEncoder,
		mock(JournalAuditService.class), mock(EmailSimuleService.class));

	@Test
	void creeUnCompteEmployeAvecMotDePasseChiffre() {
		when(compteRepository.save(any(Compte.class))).thenAnswer(i -> i.getArgument(0));

		Compte compte = service.creerCompteEmploye(formulaire(" EMP002 ", Role.RH));

		assertThat(compte.getIdentifiant()).isEqualTo("EMP002");
		assertThat(compte.getMotDePasse()).isNotEqualTo("motdepasse2");
		assertThat(passwordEncoder.matches("motdepasse2", compte.getMotDePasse())).isTrue();
	}

	@Test
	void refuseLesDoublonsEtLesComptesCandidat() {
		when(compteRepository.existsByIdentifiantIgnoreCase("rh")).thenReturn(true);

		assertThatThrownBy(() -> service.creerCompteEmploye(formulaire("rh", Role.RH)))
			.hasMessageContaining("existe déjà");
		assertThatThrownBy(() -> service.creerCompteEmploye(formulaire("x", Role.CANDIDAT)))
			.hasMessageContaining("inscription");
	}

	@Test
	void lAdminNePeutPasSeDesactiverNiChangerSonRole() {
		compte(1, Role.ADMIN);

		assertThatThrownBy(() -> service.changerActif(1, false, 1)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> service.changerRole(1, Role.RH, 1)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void unCandidatNePeutPasDevenirEmploye() {
		compte(5, Role.CANDIDAT);

		assertThatThrownBy(() -> service.changerRole(5, Role.RH, 1)).hasMessageContaining("candidat");
	}

	@Test
	void desactiveUnCompte() {
		Compte compte = compte(5, Role.EMPLOYE);

		service.changerActif(5, false, 1);

		assertThat(compte.isActif()).isFalse();
	}

	private Compte compte(Integer id, Role role) {
		Compte compte = new Compte();
		ReflectionTestUtils.setField(compte, "idCompte", id);
		compte.setIdentifiant("c" + id);
		compte.setRole(role);
		when(compteRepository.findById(id)).thenReturn(Optional.of(compte));
		return compte;
	}

	private static CompteFormulaire formulaire(String identifiant, Role role) {
		CompteFormulaire formulaire = new CompteFormulaire();
		formulaire.setIdentifiant(identifiant);
		formulaire.setRole(role);
		formulaire.setMotDePasse("motdepasse2");
		return formulaire;
	}

}
