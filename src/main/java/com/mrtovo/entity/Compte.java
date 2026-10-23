package com.mrtovo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/** Compte d'un candidat (employé, externe ou ancienne relation), identifiant = e-mail. */
@Entity
@Table(name = "compte")
@Getter
@Setter
public class Compte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compte")
    private Integer id;

    private String email;

    private String motDePasse;

    private String nom;

    private String prenom;

    private String telephone;

    private LocalDate dateNaissance;

    private String sexe;

    private String adresse;

    private String ville;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ville")
    private Ville villeResidence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_commune")
    private Commune commune;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fokontany")
    private Fokontany fokontany;

    private String lot;

    public String getAdresseStructuree() {
        StringBuilder resultat = new StringBuilder();
        if (lot != null && !lot.isBlank()) resultat.append(lot);
        if (fokontany != null) ajouter(resultat, fokontany.getLibelle());
        if (commune != null) ajouter(resultat, commune.getLibelle());
        if (villeResidence != null) ajouter(resultat, villeResidence.getLibelle());
        return resultat.isEmpty() ? null : resultat.toString();
    }

    private static void ajouter(StringBuilder resultat, String valeur) {
        if (!resultat.isEmpty()) resultat.append(", ");
        resultat.append(valeur);
    }

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dateCreation;

    /** Âge en années (null si la date de naissance n'est pas connue). */
    public Integer getAge() {
        return dateNaissance == null ? null : Period.between(dateNaissance, LocalDate.now()).getYears();
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }
}
