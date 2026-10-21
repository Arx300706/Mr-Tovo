package com.mrtovo.entity;

import com.mrtovo.entity.reference.TypeContrat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Réponse du candidat à une proposition : Oui / Non pour chaque champ, avec la valeur souhaitée si Non. */
@Entity
@Table(name = "reponse_proposition")
@Getter
@Setter
public class ReponseProposition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reponse_proposition")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proposition")
    private Proposition proposition;

    private boolean accepteSalaire;

    private BigDecimal salaireSouhaite;

    private boolean accepteTypeContrat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_type_contrat_souhaite")
    private TypeContrat typeContratSouhaite;

    private boolean accepteDateCommencement;

    private LocalDate dateCommencementSouhaite;

    /** Null quand la proposition n'a pas de date de fin (CDI). */
    private Boolean accepteDateFin;

    private LocalDate dateFinSouhaite;

    @Column(columnDefinition = "text")
    private String commentaire;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dateReponse;
}
