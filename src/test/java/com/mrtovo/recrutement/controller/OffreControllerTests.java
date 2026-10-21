package com.mrtovo.recrutement.controller;

import static com.mrtovo.recrutement.Fabrique.offre;
import static com.mrtovo.recrutement.Fabrique.utilisateur;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mrtovo.commun.config.SecurityConfig;
import com.mrtovo.commun.entity.Role;
import com.mrtovo.commun.securite.UtilisateurConnecte;
import com.mrtovo.commun.service.CompteDetailsService;
import com.mrtovo.recrutement.entity.StatutOffre;
import com.mrtovo.recrutement.service.CandidatureService;
import com.mrtovo.recrutement.service.OffreService;
import com.mrtovo.recrutement.service.OffreWorkflow;

@WebMvcTest(OffreController.class)
@Import(SecurityConfig.class)
class OffreControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CompteDetailsService compteDetailsService;

	@MockitoBean
	private OffreService offreService;

	@MockitoBean
	private CandidatureService candidatureService;

	private final UtilisateurConnecte rh = utilisateur(Role.RH, null);

	@Test
	void reserveAuxRh() throws Exception {
		mockMvc.perform(get("/offres").with(user("m").roles("MANAGER"))).andExpect(status().isForbidden());
		mockMvc.perform(get("/offres").with(user("c").roles("CANDIDAT"))).andExpect(status().isForbidden());
	}

	@Test
	void listeEtDetail() throws Exception {
		when(offreService.toutes(any())).thenReturn(List.of(offre(1, StatutOffre.PUBLIEE, null)));
		when(offreService.nombresCandidatures()).thenReturn(Map.of(1, 3L));
		when(offreService.trouver(1)).thenReturn(offre(1, StatutOffre.BROUILLON, null));
		when(offreService.actionsPossibles(any(), any())).thenReturn(List.of(OffreWorkflow.MODIFIER, OffreWorkflow.PUBLIER));

		mockMvc.perform(get("/offres").with(user(rh)))
			.andExpect(status().isOk()).andExpect(view().name("offres/liste"))
			.andExpect(model().attributeExists("offres", "nombresCandidatures", "statuts"));
		mockMvc.perform(get("/offres/1").with(user(rh)))
			.andExpect(status().isOk()).andExpect(view().name("offres/detail"))
			.andExpect(model().attribute("actionsPossibles", List.of("MODIFIER", "PUBLIER")));
	}

	@Test
	void publierEtErreurMetier() throws Exception {
		mockMvc.perform(post("/offres/1/publier").with(user(rh)).with(csrf()))
			.andExpect(redirectedUrl("/offres/1")).andExpect(flash().attributeExists("succes"));
		verify(offreService).changerStatut(eq(1), eq(OffreWorkflow.PUBLIER), any());

		when(offreService.changerStatut(eq(1), eq(OffreWorkflow.CLOTURER), any()))
			.thenThrow(new IllegalArgumentException("Action impossible : l'offre est « brouillon »"));
		mockMvc.perform(post("/offres/1/cloturer").with(user(rh)).with(csrf()))
			.andExpect(redirectedUrl("/offres/1"))
			.andExpect(flash().attribute("erreur", "Action impossible : l'offre est « brouillon »"));
	}

	@Test
	void modifierUneOffrePublieeRedirige() throws Exception {
		when(offreService.trouver(1)).thenReturn(offre(1, StatutOffre.PUBLIEE, null));
		when(offreService.actionsPossibles(any(), any())).thenReturn(List.of(OffreWorkflow.CLOTURER));

		mockMvc.perform(get("/offres/1/modifier").with(user(rh)))
			.andExpect(redirectedUrl("/offres/1")).andExpect(flash().attributeExists("erreur"));
	}

	@Test
	void nouveauBrouillonValide() throws Exception {
		mockMvc.perform(post("/offres/nouveau").with(user(rh)).with(csrf()).param("titre", ""))
			.andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("formulaire", "titre"));
	}

	@Test
	void managerVoitLesCandidaturesDUneOffre() throws Exception {
		when(offreService.trouver(1)).thenReturn(offre(1, StatutOffre.PUBLIEE, null));

		mockMvc.perform(get("/offres/1/candidatures").with(user(utilisateur(Role.MANAGER, null))))
			.andExpect(status().isOk()).andExpect(view().name("candidatures/liste"));
	}

}
