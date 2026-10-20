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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.mrtovo.commun.config.SecurityConfig;
import com.mrtovo.commun.dto.CompteFormulaire;
import com.mrtovo.commun.entity.Compte;
import com.mrtovo.commun.entity.Role;
import com.mrtovo.commun.securite.UtilisateurConnecte;
import com.mrtovo.commun.service.CompteDetailsService;
import com.mrtovo.commun.service.CompteService;
import com.mrtovo.commun.service.JournalAuditService;

@WebMvcTest(AdminCompteController.class)
@Import(SecurityConfig.class)
class AdminCompteControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CompteDetailsService compteDetailsService;

	@MockitoBean
	private CompteService compteService;

	@MockitoBean
	private JournalAuditService journalAuditService;

	@Test
	void reserveALAdmin() throws Exception {
		mockMvc.perform(get("/admin/comptes").with(user("rh").roles("RH"))).andExpect(status().isForbidden());
		mockMvc.perform(get("/admin/audit").with(user("m").roles("MANAGER"))).andExpect(status().isForbidden());
	}

	@Test
	void pagesAdmin() throws Exception {
		when(compteService.tous()).thenReturn(List.of(compte(2, "rh", Role.RH)));
		when(journalAuditService.derniers(200)).thenReturn(List.of());

		mockMvc.perform(get("/admin/comptes").with(user(admin())))
			.andExpect(status().isOk())
			.andExpect(view().name("admin/comptes"))
			.andExpect(model().attributeExists("comptes", "roles", "formulaire"));
		mockMvc.perform(get("/admin/audit").with(user(admin())))
			.andExpect(status().isOk())
			.andExpect(view().name("admin/audit"));
	}

	@Test
	void creationCompte() throws Exception {
		mockMvc.perform(post("/admin/comptes").with(user(admin())).with(csrf())
				.param("identifiant", "EMP002").param("role", "RH").param("motDePasse", "motdepasse2"))
			.andExpect(redirectedUrl("/admin/comptes"))
			.andExpect(flash().attributeExists("succes"));
		verify(compteService).creerCompteEmploye(any(CompteFormulaire.class));
	}

	@Test
	void creationCompteInvalide() throws Exception {
		mockMvc.perform(post("/admin/comptes").with(user(admin())).with(csrf())
				.param("identifiant", "").param("motDePasse", "court"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("formulaire", "identifiant", "role", "motDePasse"));
		verify(compteService, never()).creerCompteEmploye(any());
	}

	@Test
	void erreurMetierAfficheeEnFlash() throws Exception {
		when(compteService.changerActif(eq(1), eq(false), eq(1)))
			.thenThrow(new IllegalArgumentException("Vous ne pouvez pas désactiver votre propre compte"));

		mockMvc.perform(post("/admin/comptes/1/desactiver").with(user(admin())).with(csrf()))
			.andExpect(redirectedUrl("/admin/comptes"))
			.andExpect(flash().attribute("erreur", "Vous ne pouvez pas désactiver votre propre compte"));
	}

	private static UtilisateurConnecte admin() {
		return new UtilisateurConnecte(compte(1, "admin", Role.ADMIN), "admin");
	}

	private static Compte compte(Integer id, String identifiant, Role role) {
		Compte compte = new Compte();
		ReflectionTestUtils.setField(compte, "idCompte", id);
		compte.setIdentifiant(identifiant);
		compte.setMotDePasse("x");
		compte.setRole(role);
		return compte;
	}

}
