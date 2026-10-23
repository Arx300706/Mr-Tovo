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
