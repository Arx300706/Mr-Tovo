package com.mrtovo.recrutement.controller;

import static com.mrtovo.recrutement.Fabrique.offre;
import static com.mrtovo.recrutement.Fabrique.utilisateur;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.mrtovo.commun.config.SecurityConfig;
import com.mrtovo.commun.entity.Role;
import com.mrtovo.commun.securite.UtilisateurConnecte;
import com.mrtovo.commun.service.CompteDetailsService;
import com.mrtovo.recrutement.entity.StatutOffre;
import com.mrtovo.recrutement.service.CandidatureService;
import com.mrtovo.recrutement.service.OffreService;

@WebMvcTest({ PublicController.class, EspaceCandidatController.class })
@Import(SecurityConfig.class)
class PublicControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CompteDetailsService compteDetailsService;

	@MockitoBean
	private OffreService offreService;

	@MockitoBean
	private CandidatureService candidatureService;

	private final UtilisateurConnecte candidat = utilisateur(Role.CANDIDAT, 5);

	@Test
	void pagesPubliquesSansConnexion() throws Exception {
		when(offreService.visibles(any(), any())).thenReturn(List.of(offre(1, StatutOffre.PUBLIEE, null)));
		when(offreService.trouverVisible(1)).thenReturn(offre(1, StatutOffre.PUBLIEE, LocalDate.now().plusDays(3)));

		mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("public/accueil"));
		mockMvc.perform(get("/carrieres").param("q", "java"))
			.andExpect(status().isOk()).andExpect(view().name("public/offres")).andExpect(model().attributeExists("offres", "typesContrat"));
		mockMvc.perform(get("/carrieres/1"))
			.andExpect(status().isOk()).andExpect(view().name("public/offre-detail"));
		mockMvc.perform(get("/carrieres/merci")).andExpect(status().isOk()).andExpect(view().name("public/confirmation"));
	}

	@Test
	void offreInvisibleEn404() throws Exception {
		when(offreService.trouverVisible(9)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

		mockMvc.perform(get("/carrieres/9")).andExpect(status().isNotFound());
	}

	@Test
	void candidaterDemandeUnCompteCandidat() throws Exception {
		mockMvc.perform(get("/carrieres/1/candidater")).andExpect(redirectedUrl("/connexion"));
		mockMvc.perform(get("/carrieres/1/candidater").with(user("rh").roles("RH"))).andExpect(status().isForbidden());
		mockMvc.perform(get("/carrieres/1/candidater").with(user(candidat))).andExpect(redirectedUrl("/carrieres/1#postuler"));
		mockMvc.perform(get("/espace-candidat")).andExpect(redirectedUrl("/connexion"));
	}

	@Test
	void postulerAvecCv() throws Exception {
		when(offreService.trouverVisible(1)).thenReturn(offre(1, StatutOffre.PUBLIEE, null));

		mockMvc.perform(multipart("/carrieres/1/postuler").file(new MockMultipartFile("cv", "cv.pdf", "application/pdf", new byte[10]))
				.param("lettre", "Bonjour").with(user(candidat)).with(csrf()))
			.andExpect(redirectedUrl("/carrieres/merci"));
		verify(candidatureService).postuler(eq(1), any(), any());
	}

	@Test
	void postulerSansCvRaffichLeFormulaire() throws Exception {
		when(offreService.trouverVisible(1)).thenReturn(offre(1, StatutOffre.PUBLIEE, null));

		mockMvc.perform(multipart("/carrieres/1/postuler").param("lettre", "Bonjour").with(user(candidat)).with(csrf()))
			.andExpect(status().isOk())
			.andExpect(view().name("public/offre-detail"))
			.andExpect(model().attributeHasFieldErrors("formulaire", "cv"));
		verify(candidatureService, never()).postuler(any(), any(), any());
	}

	@Test
	void espaceCandidat() throws Exception {
		mockMvc.perform(get("/espace-candidat").with(user(candidat)))
			.andExpect(status().isOk())
			.andExpect(view().name("espace-candidat/candidatures"))
			.andExpect(model().attributeExists("candidatures"));
	}

}
