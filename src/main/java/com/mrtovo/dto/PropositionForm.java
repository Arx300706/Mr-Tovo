package com.mrtovo.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Proposition d'emploi saisie (ou révisée) par la RH. */
@Data
public class PropositionForm {

    /** Poste vacant existant ; laissé vide si on crée un nouveau poste. */
    private Integer idPoste;

    /** Libellé du nouveau poste à créer quand idPoste est vide. */
    private String libelleNouveauPoste;

    @NotNull(message = "Le salaire est obligatoire")
    @Positive(message = "Le salaire doit être positif")
    private BigDecimal salaire;

    @NotNull(message = "Le type de contrat est obligatoire")
    private Integer idTypeContrat;

    @NotNull(message = "La date de commencement est obligatoire")
    @FutureOrPresent(message = "La date de commencement ne peut pas être passée")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateCommencement;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFin;

    @NotNull(message = "La date limite de réponse est obligatoire")
    @FutureOrPresent(message = "La date limite ne peut pas être passée")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateLimiteValidation;
}
