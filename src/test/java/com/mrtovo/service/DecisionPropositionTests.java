package com.mrtovo.service;

import com.mrtovo.entity.reference.StatutProposition;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DecisionPropositionTests {

    @Test
    void toutOuiDonneValide() {
        assertThat(DecisionProposition.decider(List.of(true, true, true, true))).isEqualTo(StatutProposition.VALIDE);
    }

    @Test
    void toutNonDonneRefuse() {
        assertThat(DecisionProposition.decider(List.of(false, false, false))).isEqualTo(StatutProposition.REFUSE);
    }

    @Test
    void melangeDonneEnAttente() {
        assertThat(DecisionProposition.decider(List.of(false, true, true))).isEqualTo(StatutProposition.EN_ATTENTE);
    }

    @Test
    void reponseManquanteRefusee() {
        assertThatThrownBy(() -> DecisionProposition.decider(Arrays.asList(true, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
