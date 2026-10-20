package com.mrtovo.commun.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.mrtovo.commun.config.SecurityConfig;
import com.mrtovo.commun.dto.InscriptionFormulaire;
import com.mrtovo.commun.entity.Compte;
import com.mrtovo.commun.entity.Role;
import com.mrtovo.commun.securite.UtilisateurConnecte;
import com.mrtovo.commun.service.CompteDetailsService;
import com.mrtovo.commun.service.InscriptionService;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@MockitoBean
	private CompteDetailsService compteDetailsService;

	@MockitoBean
	private InscriptionService inscriptionService;

	@Test
	void pagesPubliquesAccessiblesSansConnexion() throws Exception {
		mockMvc.perform(get("/connexion"))
			.andExpect(status().isOk())
			.andExpect(view().name("auth/connexion"))
			.andExpect(model().attribute("espace", "employe"));
		mockMvc.perform(get("/connexion").param("espace", "candidat"))
			.andExpect(model().attribute("espace", "candidat"));
		mockMvc.perform(get("/inscription"))
			.andExpect(status().isOk())
			.andExpect(view().name("auth/inscription"));
		mockMvc.perform(get("/css/style.css")).andExpect(status().isOk());
	}

	@Test
	void pageProtegeeRedirigeVersConnexion() throws Exception {
		mockMvc.perform(get("/besoins"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/connexion"));
	}

	@Test
	void droitsParRole() throws Exception {
		mockMvc.perform(get("/besoins").with(user("c").roles("CANDIDAT"))).andExpect(status().isForbidden());
		mockMvc.perform(get("/admin/comptes").with(user("rh").roles("RH"))).andExpect(status().isForbidden());
		mockMvc.perform(get("/offres").with(user("m").roles("MANAGER"))).andExpect(status().isForbidden());
		mockMvc.perform(get("/espace-candidat").with(user("rh").roles("RH"))).andExpect(status().isForbidden());
		// Autorisé : pas de contrôleur encore, donc 404 et pas 403
		mockMvc.perform(get("/offres/1/candidatures").with(user("m").roles("MANAGER"))).andExpect(status().isNotFound());
		mockMvc.perform(get("/espace-candidat").with(user("c").roles("CANDIDAT"))).andExpect(status().isNotFound());
	}

	@Test
	void connexionEmployeRedirigeVersTableauDeBord() throws Exception {
		when(compteDetailsService.loadUserByUsername("rh")).thenReturn(utilisateur("rh", Role.RH));

		mockMvc.perform(post("/connexion").with(csrf())
				.param("identifiant", "rh").param("motDePasse", "demo1234").param("espace", "employe"))
			.andExpect(redirectedUrl("/tableau-de-bord"));
	}

	@Test
	void connexionCandidatRedirigeVersSonEspace() throws Exception {
		when(compteDetailsService.loadUserByUsername("c@x.mg")).thenReturn(utilisateur("c@x.mg", Role.CANDIDAT));

		mockMvc.perform(post("/connexion").with(csrf())
				.param("identifiant", "c@x.mg").param("motDePasse", "demo1234").param("espace", "candidat"))
			.andExpect(redirectedUrl("/espace-candidat"));
	}

	@Test
	void connexionRateeRevientSurLeBonOnglet() throws Exception {
		when(compteDetailsService.loadUserByUsername(any())).thenThrow(new UsernameNotFoundException("x"));

		mockMvc.perform(post("/connexion").with(csrf())
				.param("identifiant", "inconnu").param("motDePasse", "x").param("espace", "candidat"))
			.andExpect(redirectedUrl("/connexion?erreur&espace=candidat"));
	}

	@Test
	void inscriptionValideCreeLeCompte() throws Exception {
		mockMvc.perform(post("/inscription").with(csrf())
				.param("nom", "Rabe").param("prenom", "Soa").param("email", "soa@x.mg")
				.param("motDePasse", "motdepasse1").param("confirmationMotDePasse", "motdepasse1"))
			.andExpect(redirectedUrl("/connexion?espace=candidat"));

		verify(inscriptionService).inscrireCandidat(any(InscriptionFormulaire.class));
	}

	@Test
	void inscriptionRefuseeSiMotsDePasseDifferentsOuEmailUtilise() throws Exception {
		mockMvc.perform(post("/inscription").with(csrf())
				.param("nom", "Rabe").param("prenom", "Soa").param("email", "soa@x.mg")
				.param("motDePasse", "motdepasse1").param("confirmationMotDePasse", "autre"))
			.andExpect(status().isOk())
			.andExpect(view().name("auth/inscription"))
			.andExpect(model().attributeHasFieldErrors("formulaire", "motsDePasseIdentiques"));

		when(inscriptionService.emailDejaUtilise(eq("pris@x.mg"))).thenReturn(true);
		mockMvc.perform(post("/inscription").with(csrf())
				.param("nom", "Rabe").param("prenom", "Soa").param("email", "pris@x.mg")
				.param("motDePasse", "motdepasse1").param("confirmationMotDePasse", "motdepasse1"))
			.andExpect(model().attributeHasFieldErrors("formulaire", "email"));

		verify(inscriptionService, never()).inscrireCandidat(any());
	}

	private UtilisateurConnecte utilisateur(String identifiant, Role role) {
		Compte compte = new Compte();
		ReflectionTestUtils.setField(compte, "idCompte", 1);
		compte.setIdentifiant(identifiant);
		compte.setMotDePasse(passwordEncoder.encode("demo1234"));
		compte.setRole(role);
		return new UtilisateurConnecte(compte, identifiant);
	}

}
