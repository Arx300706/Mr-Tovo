package com.mrtovo.service;

import com.mrtovo.entity.Besoin;
import com.mrtovo.entity.Candidature;
import com.mrtovo.entity.OffreEmploi;
import com.mrtovo.entity.reference.TypeContrat;
import com.mrtovo.exception.RegleMetierException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PropositionDatesTests {

    private static final LocalDate COMMENCEMENT = LocalDate.of(2027, 3, 1);

    private static TypeContrat type(String libelle) {
        TypeContrat t = new TypeContrat();
        t.setLibelle(libelle);
        return t;
    }

    @Test
    void cdiSansDateDeFinAccepte() {
        assertThatCode(() -> PropositionService.verifierDates(type(TypeContrat.CDI), COMMENCEMENT, null, LocalDate.of(2027, 2, 20)))
                .doesNotThrowAnyException();
    }

    @Test
    void cddSansDateDeFinRefuse() {
        assertThatThrownBy(() -> PropositionService.verifierDates(type(TypeContrat.CDD), COMMENCEMENT, null, LocalDate.of(2027, 2, 20)))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("date de fin");
    }

    @Test
    void ceAvecDateDeFinAvantOuLeMemeJourQueLeCommencementRefuse() {
        assertThatThrownBy(() -> PropositionService.verifierDates(type(TypeContrat.CE), COMMENCEMENT, COMMENCEMENT, LocalDate.of(2027, 2, 20)))
                .isInstanceOf(RegleMetierException.class);
        assertThatThrownBy(() -> PropositionService.verifierDates(type(TypeContrat.CE), COMMENCEMENT, COMMENCEMENT.minusDays(1), LocalDate.of(2027, 2, 20)))
                .isInstanceOf(RegleMetierException.class);
    }

    @Test
    void cddAvecDateDeFinApresLeCommencementAccepte() {
        assertThatCode(() -> PropositionService.verifierDates(type(TypeContrat.CDD), COMMENCEMENT, COMMENCEMENT.plusDays(1), LocalDate.of(2027, 2, 20)))
                .doesNotThrowAnyException();
    }

    @Test
    void limiteDeReponseApresLeCommencementRefusee() {
        assertThatThrownBy(() -> PropositionService.verifierDates(type(TypeContrat.CDI), COMMENCEMENT, null, COMMENCEMENT.plusDays(1)))
                .isInstanceOf(RegleMetierException.class)
                .hasMessageContaining("date limite de réponse");
    }

    @Test
    void limiteLeJourDuCommencementAccepteeEtLaVeille() {
        assertThatCode(() -> PropositionService.verifierDates(type(TypeContrat.CDI), COMMENCEMENT, null, COMMENCEMENT))
                .doesNotThrowAnyException();
        assertThatCode(() -> PropositionService.verifierDates(type(TypeContrat.CDI), COMMENCEMENT, null, COMMENCEMENT.minusDays(1)))
                .doesNotThrowAnyException();
    }

    @Test
    void datesPreRemplieesCddUnAnMoinsUnJour() {
        LocalDate debut = LocalDate.now().plusMonths(2);
        Candidature c = candidatureAvecContrat(TypeContrat.CDD, debut);

        var form = new PropositionService(null, null, null, null, null, null, null, null, null).formulairePour(c);

        assertThat(form.getDateCommencement()).isEqualTo(debut);
        assertThat(form.getDateFin()).isEqualTo(debut.plusYears(1).minusDays(1));
        assertThat(form.getDateLimiteValidation()).isEqualTo(LocalDate.now().plusDays(7));
    }

    @Test
    void datesPreRemplieesCeSixMoisMoinsUnJour() {
        LocalDate debut = LocalDate.now().plusMonths(2);
        Candidature c = candidatureAvecContrat(TypeContrat.CE, debut);

        var form = new PropositionService(null, null, null, null, null, null, null, null, null).formulairePour(c);

        assertThat(form.getDateFin()).isEqualTo(debut.plusMonths(6).minusDays(1));
    }

    @Test
    void cdiPreRemplideSansDateDeFin() {
        Candidature c = candidatureAvecContrat(TypeContrat.CDI, null);

        var form = new PropositionService(null, null, null, null, null, null, null, null, null).formulairePour(c);

        assertThat(form.getDateFin()).isNull();
        assertThat(form.getDateCommencement()).isAfter(LocalDate.now());
    }

    /** Candidature réaliste : offre → besoin (type de contrat et date de commencement). */
    private static Candidature candidatureAvecContrat(String libelleContrat, LocalDate dateCommencement) {
        TypeContrat contrat = type(libelleContrat);
        Besoin besoin = new Besoin();
        besoin.setTypeContrat(contrat);
        besoin.setDateCommencement(dateCommencement);
        OffreEmploi offre = new OffreEmploi();
        offre.setBesoin(besoin);
        Candidature c = new Candidature();
        c.setOffre(offre);
        return c;
    }
}
