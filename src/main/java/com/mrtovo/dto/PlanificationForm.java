package com.mrtovo.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/** Sélection d'une candidature par la RH : dates du test technique et de l'entretien. */
@Data
public class PlanificationForm {

    @NotNull(message = "La date du test technique est obligatoire")
    @Future(message = "La date du test doit être dans le futur")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dateTestTechnique;

    @NotNull(message = "La date de l'entretien est obligatoire")
    @Future(message = "La date de l'entretien doit être dans le futur")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dateEntretien;

    private String commentaire;
}
