package com.mrtovo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/** Pièce d'identité protégée jointe au CV d'une candidature. */
@Entity
@Table(name = "document_identite")
@Getter
@Setter
public class DocumentIdentite {

    public static final String CIN = "CIN";
    public static final String PASSEPORT = "PASSEPORT";
    public static final String PERMIS = "PERMIS";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_document_identite")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_candidature", unique = true)
    private Candidature candidature;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employe", unique = true)
    private Employe employe;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false)
    private String nomFichier;

    @Column(nullable = false, length = 100)
    private String mimeType;

    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] fichier;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dateDepot;
}
