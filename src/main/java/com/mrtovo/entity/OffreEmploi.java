package com.mrtovo.entity;

import com.mrtovo.entity.reference.StatutOffre;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "offre_emploi")
@Getter
@Setter
public class OffreEmploi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_offre")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_besoin")
    private Besoin besoin;

    private String intitule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_departement")
    private Departement departement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_service")
    private ServiceEntreprise service;

    @Column(columnDefinition = "text")
    private String description;

    @Column(columnDefinition = "text")
    private String profil;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dateCreation;

    private LocalDate dateFin;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(insertable = false, updatable = false)
    private LocalDateTime dateModification;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_statut_offre")
    private StatutOffre statut;

    /** Une offre publiée accepte des candidatures jusqu'à sa date de fin incluse. */
    public boolean estOuverte() {
        return StatutOffre.PUBLIE.equals(statut.getLibelle())
                && (dateFin == null || !dateFin.isBefore(LocalDate.now()));
    }
}
