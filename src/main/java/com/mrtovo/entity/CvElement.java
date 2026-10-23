package com.mrtovo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Une ligne répétable d'une rubrique du CV en ligne. */
@Entity
@Table(name = "cv_element")
@Getter
@Setter
public class CvElement {

    public static final String EXPERIENCE = "EXPERIENCE";
    public static final String FORMATION = "FORMATION";
    public static final String COMPETENCE = "COMPETENCE";
    public static final String QUALITE = "QUALITE";
    public static final String LANGUE = "LANGUE";
    public static final String PROJET = "PROJET";
    public static final String CERTIFICATION = "CERTIFICATION";
    public static final String INTERET = "INTERET";
    public static final String LIEN = "LIEN";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cv_element")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_candidature")
    private Candidature candidature;

    /** Permet aussi de gérer le CV d'une personne déjà employée. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employe")
    private Employe employe;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(nullable = false)
    private String titre;

    private String sousTitre;
    private String dateDebut;
    private String dateFin;

    @Column(columnDefinition = "text")
    private String description;

    private String niveau;
    private String url;
    private Integer ordre;
}
