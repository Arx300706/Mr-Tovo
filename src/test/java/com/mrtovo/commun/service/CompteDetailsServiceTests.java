package com.mrtovo.commun.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.mrtovo.commun.entity.Compte;
import com.mrtovo.commun.entity.Employe;
import com.mrtovo.commun.entity.Role;
import com.mrtovo.commun.repository.CompteRepository;
import com.mrtovo.commun.repository.EmployeRepository;
import com.mrtovo.recrutement.repository.CandidatRepository;

class CompteDetailsServiceTests {

	private final CompteRepository compteRepository = mock(CompteRepository.class);
	private final EmployeRepository employeRepository = mock(EmployeRepository.class);
	private final CompteDetailsService service =
		new CompteDetailsService(compteRepository, employeRepository, mock(CandidatRepository.class));

	@AfterEach
	void nettoyer() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	void unCandidatNePeutPasPasserParLEntreeEmploye() {
		compte("c@x.mg", Role.CANDIDAT);
		espace("employe");

		assertThatThrownBy(() -> service.loadUserByUsername("c@x.mg")).isInstanceOf(UsernameNotFoundException.class);
	}

	@Test
	void unEmployeNePeutPasPasserParLEntreeCandidat() {
		compte("EMP001", Role.EMPLOYE);
		espace("candidat");

		assertThatThrownBy(() -> service.loadUserByUsername("EMP001")).isInstanceOf(UsernameNotFoundException.class);
	}

	@Test
	void chargeLeCompteParLaBonneEntree() {
		Employe employe = mock(Employe.class);
		when(employe.getMatricule()).thenReturn("MAT-0001");
		when(employe.getMotDePasse()).thenReturn("$2a$10$hash");
		when(employe.getPrenom()).thenReturn("Mr");
		when(employe.getNom()).thenReturn("Tovo");
		when(employeRepository.findByMatriculeIgnoreCase("MAT-0001")).thenReturn(Optional.of(employe));
		espace("employe");

		assertThat(service.loadUserByUsername("MAT-0001").getAuthorities())
			.extracting(Object::toString).containsExactly("ROLE_EMPLOYE");
	}

	private void compte(String identifiant, Role role) {
		Compte compte = new Compte();
		compte.setIdentifiant(identifiant);
		compte.setMotDePasse("x");
		compte.setRole(role);
		when(compteRepository.findByIdentifiantIgnoreCase(identifiant)).thenReturn(Optional.of(compte));
	}

	private static void espace(String espace) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter(CompteDetailsService.PARAM_ESPACE, espace);
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
	}

}
