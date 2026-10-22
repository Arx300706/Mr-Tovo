package com.mrtovo.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Saisie d'une offre d'emploi par la RH à partir d'un besoin validé par la Finance. */
@Data
public class OffreForm {

    @NotBlank(message = "L'intitulé est obligatoire")
    @Size(max = 150, message = "Ne doit pas dépasser 150 caractères")
    private String intitule;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    private String profil;

    @NotNull(message = "La date limite de candidature est obligatoire")
    @FutureOrPresent(message = "La date limite ne peut pas être passée")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFin;
}
