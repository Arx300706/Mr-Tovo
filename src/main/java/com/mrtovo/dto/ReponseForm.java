package com.mrtovo.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Réponse du candidat : Oui / Non pour chaque champ, valeur souhaitée si Non. */
@Data
public class ReponseForm {

    private Boolean accepteSalaire;
    private BigDecimal salaireSouhaite;

    private Boolean accepteTypeContrat;
    private Integer idTypeContratSouhaite;

    private Boolean accepteDateCommencement;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateCommencementSouhaite;

    private Boolean accepteDateFin;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFinSouhaite;

    private String commentaire;
}
