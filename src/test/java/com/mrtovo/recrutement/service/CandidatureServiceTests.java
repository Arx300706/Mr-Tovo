package com.mrtovo.recrutement.service;

import static com.mrtovo.recrutement.Fabrique.offre;
import static com.mrtovo.recrutement.Fabrique.statutCandidature;
import static com.mrtovo.recrutement.Fabrique.utilisateur;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.mrtovo.commun.entity.Role;
import com.mrtovo.commun.service.EmailSimuleService;
import com.mrtovo.commun.service.JournalAuditService;
import com.mrtovo.recrutement.dto.CandidatureFormulaire;
import com.mrtovo.recrutement.entity.Candidat;
import com.mrtovo.recrutement.entity.Candidature;
import com.mrtovo.recrutement.entity.StatutCandidature;
import com.mrtovo.recrutement.entity.StatutOffre;
import com.mrtovo.recrutement.repository.CandidatRepository;
import com.mrtovo.recrutement.repository.CandidatureRepository;
import com.mrtovo.recrutement.repository.EntretienRepository;
import com.mrtovo.recrutement.repository.EvaluationCandidatRepository;
import com.mrtovo.recrutement.repository.StatutCandidatureRepository;

class CandidatureServiceTests {

	private final CandidatureRepository candidatureRepository = mock(CandidatureRepository.class);
	private final CandidatRepository candidatRepository = mock(CandidatRepository.class);
	private final StatutCandidatureRepository statutRepository = mock(StatutCandidatureRepository.class);
	private final OffreService offreService = mock(OffreService.class);
	private final CandidatureService service = new CandidatureService(candidatureRepository, candidatRepository,
		statutRepository, mock(EntretienRepository.class), mock(EvaluationCandidatRepository.class), offreService,
		mock(JournalAuditService.class), mock(EmailSimuleService.class));

	@BeforeEach
	void preparer() {
		Candidat candidat = new Candidat();
		candidat.setNom("Rabe");
		candidat.setPrenom("Soa");
		candidat.setEmail("c@x.mg");
		when(candidatRepository.findById(5)).thenReturn(Optional.of(candidat));
		when(offreService.trouverVisible(1)).thenReturn(offre(1, StatutOffre.PUBLIEE, null));
		when(statutRepository.findByLibelle(StatutCandidature.RECUE))
			.thenReturn(Optional.of(statutCandidature(StatutCandidature.RECUE)));
		when(candidatureRepository.save(any(Candidature.class))).thenAnswer(i -> i.getArgument(0));
	}

	@Test
	void candidatureRecueAvecLeCandidatDuCompte() {
		Candidature candidature = service.postuler(1, utilisateur(Role.CANDIDAT, 5), formulaire(cv("cv.pdf", 1000)));

		assertThat(candidature.getCandidat().getEmail()).isEqualTo("c@x.mg");
		assertThat(candidature.getStatutCandidature().getLibelle()).isEqualTo(StatutCandidature.RECUE);
		assertThat(candidature.getOffre().getIdOffre()).isEqualTo(1);
	}

	@Test
	void doubleCandidatureRefusee() {
		when(candidatureRepository.existsByCandidatIdCandidatAndOffreIdOffre(5, 1)).thenReturn(true);

		assertThatThrownBy(() -> service.postuler(1, utilisateur(Role.CANDIDAT, 5), formulaire(cv("cv.pdf", 1000))))
			.hasMessageContaining("déjà postulé");
		verify(candidatureRepository, never()).save(any());
	}

	@Test
	void offreInvisibleRefusee() {
		when(offreService.trouverVisible(9)).thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));

		assertThatThrownBy(() -> service.postuler(9, utilisateur(Role.CANDIDAT, 5), formulaire(cv("cv.pdf", 1000))))
			.isInstanceOf(ResponseStatusException.class);
	}

	@Test
	void reserveAuxComptesCandidat() {
		assertThatThrownBy(() -> service.postuler(1, utilisateur(Role.RH, null), formulaire(cv("cv.pdf", 1000))))
			.isInstanceOf(ResponseStatusException.class);
	}

	@Test
	void controleDuCv() {
		assertThat(CandidatureService.erreurCv(null)).contains("obligatoire");
		assertThat(CandidatureService.erreurCv(cv("cv.pdf", 0))).contains("obligatoire");
		assertThat(CandidatureService.erreurCv(cv("cv.exe", 100))).contains("PDF ou Word");
		assertThat(CandidatureService.erreurCv(cv("cv", 100))).contains("PDF ou Word");
		assertThat(CandidatureService.erreurCv(cv("cv.pdf", CandidatureService.TAILLE_MAX_CV + 1))).contains("5 Mo");
		assertThat(CandidatureService.erreurCv(cv("Mon CV.DOCX", 100))).isNull();
		assertThat(CandidatureService.erreurCv(cv("cv.pdf", CandidatureService.TAILLE_MAX_CV))).isNull();
	}

	private static CandidatureFormulaire formulaire(MockMultipartFile cv) {
		CandidatureFormulaire formulaire = new CandidatureFormulaire();
		formulaire.setLettre("Bonjour");
		formulaire.setCv(cv);
		return formulaire;
	}

	private static MockMultipartFile cv(String nom, long taille) {
		return new MockMultipartFile("cv", nom, "application/octet-stream", new byte[(int) taille]);
	}

}
