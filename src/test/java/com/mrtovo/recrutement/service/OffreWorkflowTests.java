package com.mrtovo.recrutement.service;

import static com.mrtovo.recrutement.Fabrique.offre;
import static com.mrtovo.recrutement.Fabrique.utilisateur;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.mrtovo.commun.entity.Role;
import com.mrtovo.recrutement.entity.Offre;
import com.mrtovo.recrutement.entity.StatutOffre;

class OffreWorkflowTests {

	private static final LocalDate AUJOURDHUI = LocalDate.of(2026, 10, 20);

	private final OffreWorkflow workflow = new OffreWorkflow();

	@Test
	void actionsPossiblesSelonLeStatut() {
		assertThat(workflow.actionsPossibles(offre(1, StatutOffre.BROUILLON, null), utilisateur(Role.RH, null)))
			.containsExactly(OffreWorkflow.MODIFIER, OffreWorkflow.PUBLIER);
		assertThat(workflow.actionsPossibles(offre(1, StatutOffre.PUBLIEE, null), utilisateur(Role.RH, null)))
			.containsExactly(OffreWorkflow.CLOTURER, OffreWorkflow.ANNULER);
		assertThat(workflow.actionsPossibles(offre(1, StatutOffre.CLOTUREE, null), utilisateur(Role.RH, null)))
			.isEmpty();
	}

	@Test
	void seulsLesRhAgissentSurLesOffres() {
		Offre brouillon = offre(1, StatutOffre.BROUILLON, null);

		assertThat(workflow.actionsPossibles(brouillon, utilisateur(Role.MANAGER, null))).isEmpty();
		assertThatThrownBy(() -> workflow.verifier(brouillon, OffreWorkflow.PUBLIER, utilisateur(Role.MANAGER, null), AUJOURDHUI))
			.hasMessageContaining("droit");
	}

	@Test
	void transitionNonPrevueRefusee() {
		Offre annulee = offre(1, StatutOffre.ANNULEE, null);

		assertThatThrownBy(() -> workflow.verifier(annulee, OffreWorkflow.PUBLIER, utilisateur(Role.RH, null), AUJOURDHUI))
			.hasMessageContaining("annulée");
		// Section 3 : seule une offre publiée peut être annulée
		assertThatThrownBy(() -> workflow.verifier(offre(1, StatutOffre.BROUILLON, null), OffreWorkflow.ANNULER,
			utilisateur(Role.RH, null), AUJOURDHUI)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void publicationDemandeUneOffreComplete() {
		Offre sansDescription = offre(1, StatutOffre.BROUILLON, null);
		sansDescription.setDescription(" ");
		Offre expiree = offre(2, StatutOffre.BROUILLON, AUJOURDHUI.minusDays(1));

		assertThatThrownBy(() -> workflow.verifier(sansDescription, OffreWorkflow.PUBLIER, utilisateur(Role.RH, null), AUJOURDHUI))
			.hasMessageContaining("description");
		assertThatThrownBy(() -> workflow.verifier(expiree, OffreWorkflow.PUBLIER, utilisateur(Role.RH, null), AUJOURDHUI))
			.hasMessageContaining("date limite");
		assertThat(workflow.verifier(offre(3, StatutOffre.BROUILLON, AUJOURDHUI), OffreWorkflow.PUBLIER,
			utilisateur(Role.RH, null), AUJOURDHUI)).isEqualTo(StatutOffre.PUBLIEE);
	}

	@Test
	void offreExpireeInvisibleAuPublic() {
		assertThat(offre(1, StatutOffre.PUBLIEE, AUJOURDHUI).estVisiblePubliquement(AUJOURDHUI)).isTrue();
		assertThat(offre(1, StatutOffre.PUBLIEE, null).estVisiblePubliquement(AUJOURDHUI)).isTrue();
		assertThat(offre(1, StatutOffre.PUBLIEE, AUJOURDHUI.minusDays(1)).estVisiblePubliquement(AUJOURDHUI)).isFalse();
		assertThat(offre(1, StatutOffre.BROUILLON, null).estVisiblePubliquement(AUJOURDHUI)).isFalse();
	}

}
