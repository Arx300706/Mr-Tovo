package com.mrtovo.entity;

import com.mrtovo.entity.reference.StatutContrat;
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

@Entity
@Table(name = "contrat")
@Getter
@Setter
public class Contrat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrat")
    private Integer id;

    /** Renseigné quand la RH valide le contrat (création ou mise à jour de l'employé). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employe")
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_candidature")
    private Candidature candidature;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proposition")
    private Proposition proposition;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_statut_contrat")
    private StatutContrat statut;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_type_contrat")
    private TypeContrat typeContrat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_poste")
    private Poste poste;

    private BigDecimal salaire;

    private LocalDate dateDebut;

    private LocalDate dateFin;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateSignatureCandidat;

    private LocalDateTime dateSignatureRh;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(insertable = false, updatable = false)
    private LocalDateTime dateModification;
}
