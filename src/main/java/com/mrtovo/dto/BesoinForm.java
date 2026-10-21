package com.mrtovo.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Formulaire d'expression de besoin (manager). */
@Data
public class BesoinForm {

    @NotNull(message = "Choisissez le type de besoin")
    private Integer idTypeBesoin;

    /** Utilisé seulement par le directeur (un manager crée toujours pour son département). */
    private Integer idDepartement;

    @NotNull(message = "Choisissez le service")
    private Integer idService;

    /** Poste existant visé (obligatoire pour une recherche de personnel). */
    private Integer idPoste;

    @NotBlank(message = "L'intitulé est obligatoire")
    @Size(max = 150)
    private String intitule;

    @NotNull(message = "Le nombre est obligatoire")
    @Min(value = 1, message = "Au moins une personne")
    private Integer nombre;

    private String sexe;

    @Min(value = 16, message = "Âge minimum : 16 ans")
    private Integer ageMin;

    @Max(value = 70, message = "Âge maximum : 70 ans")
    private Integer ageMax;

    @NotNull(message = "Choisissez le type de contrat")
    private Integer idTypeContrat;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @FutureOrPresent(message = "La date de commencement ne peut pas être passée")
    private LocalDate dateCommencement;

    @PositiveOrZero(message = "Le budget doit être positif")
    private BigDecimal budget;

    @NotBlank(message = "Le motif est obligatoire")
    private String motif;
}
