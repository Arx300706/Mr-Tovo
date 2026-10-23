package com.mrtovo.entity;

import com.mrtovo.entity.reference.StatutProposition;
import com.mrtovo.entity.reference.TypeContrat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Proposition d'emploi faite par la RH à un candidat retenu (candidature « succes »). */
@Entity
@Table(name = "proposition")
@Getter
@Setter
public class Proposition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proposition")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_offre")
    private OffreEmploi offre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_candidature")
    private Candidature candidature;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_poste")
    private Poste poste;

    private BigDecimal salaire;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_type_contrat")
    private TypeContrat typeContrat;

    private LocalDate dateCommencement;

    private LocalDate dateFin;

    private LocalDate dateLimiteValidation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_statut_proposition")
    private StatutProposition statut;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dateCreation;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(insertable = false, updatable = false)
    private LocalDateTime dateModification;

    public boolean estExpiree() {
        return dateLimiteValidation != null && dateLimiteValidation.isBefore(LocalDate.now());
    }
}
