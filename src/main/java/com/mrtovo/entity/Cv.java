package com.mrtovo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/** CV déposé avec une candidature, et employé RH qui l'a traité (et quand). */
@Entity
@Table(name = "cv")
@Getter
@Setter
public class Cv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cv")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_candidature")
    private Candidature candidature;

    @Column(nullable = false)
    private String nomFichier;

    /** Contenu du fichier PDF. */
    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] fichier;

    @CreationTimestamp
    @Column(name = "date_depot", updatable = false)
    private LocalDateTime dateDepot;

    /** Employé RH qui a traité le CV (première action RH sur la candidature). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employe")
    private Employe employe;

    private LocalDateTime dateTraitement;
}
