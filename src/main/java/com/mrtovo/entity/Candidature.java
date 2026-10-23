package com.mrtovo.entity;

import com.mrtovo.entity.reference.StatutCandidature;
import com.mrtovo.entity.reference.TypeCandidature;
import com.mrtovo.entity.reference.TypeContrat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.LocalDateTime;

@Entity
@Table(name = "candidature")
@Getter
@Setter
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_candidature")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_compte")
    private Compte compte;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dateCandidature;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_departement")
    private Departement departement;

    /** Renseigné quand le candidat est (ou a été) employé de l'entreprise. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employe")
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_offre")
    private OffreEmploi offre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_statut_candidature")
    private StatutCandidature statut;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_type_candidature")
    private TypeCandidature typeCandidature;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_type_contrat")
    private TypeContrat typeContrat;

    private String diplome;

    private Integer anneesExperience;

    @Column(columnDefinition = "text")
    private String lettreMotivation;

    @Column(columnDefinition = "text")
    private String experienceProfessionnelle;

    private String metierRecherche;

    @Column(columnDefinition = "text")
    private String presentation;

    @Column(columnDefinition = "text")
    private String formations;

    @Column(columnDefinition = "text")
    private String competences;

    @Column(columnDefinition = "text")
    private String qualites;

    @Column(columnDefinition = "text")
    private String langues;

    @Column(columnDefinition = "text")
    private String projets;

    @Column(columnDefinition = "text")
    private String certifications;

    @Column(columnDefinition = "text")
    private String centresInteret;

    private String linkedin;
    private String github;
    private String portfolio;

    @Column(columnDefinition = "bytea")
    private byte[] photo;

    private String photoType;

    private LocalDateTime dateTestTechnique;

    private LocalDateTime dateEntretien;

    @Column(columnDefinition = "text")
    private String commentaireRh;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(insertable = false, updatable = false)
    private LocalDateTime dateModification;
}
