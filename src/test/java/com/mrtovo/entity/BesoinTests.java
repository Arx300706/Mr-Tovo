package com.mrtovo.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BesoinTests {

    @Test
    void depenseAnnuelle() {
        // 2 développeurs juniors à 2 000 000 Ar par mois : 2 000 000 x 12 x 2
        Besoin b = new Besoin();
        b.setNombre(2);
        b.setSalaireMensuel(new BigDecimal("2000000"));
        assertThat(b.calculerDepenseAnnuelle()).isEqualByComparingTo("48000000");
    }

    @Test
    void pasDeDepenseSansSalaire() {
        Besoin b = new Besoin();
        b.setNombre(3);
        assertThat(b.calculerDepenseAnnuelle()).isNull();
    }
}
