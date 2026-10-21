package com.mrtovo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/** Journal de tous les changements de statut (traçabilité). */
@Entity
@Table(name = "historique_statut")
@Getter
@Setter
public class HistoriqueStatut {

    public static final String BESOIN = "BESOIN";
    public static final String OFFRE = "OFFRE";
    public static final String CANDIDATURE = "CANDIDATURE";
    public static final String PROPOSITION = "PROPOSITION";
    public static final String CONTRAT = "CONTRAT";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historique_statut")
    private Integer id;

    private String entite;

    private Integer idEntite;

    private String ancienStatut;

    private String nouveauStatut;

    private String auteur;

    @Column(columnDefinition = "text")
    private String commentaire;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dateChangement;
}
