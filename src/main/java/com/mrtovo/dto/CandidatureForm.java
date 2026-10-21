package com.mrtovo.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Candidature à une offre publiée. Les champs « compte » (identité, e-mail, mot de passe)
 * ne sont demandés que si le visiteur n'est pas déjà connecté en tant que candidat.
 */
@Data
public class CandidatureForm {

    @NotNull(message = "Indiquez si vous êtes employé, externe ou ancienne relation")
    private Integer idTypeCandidature;

    /** Matricule : obligatoire pour un employé, facultatif pour une ancienne relation. */
    private String matricule;

    // --- Compte ---
    private String nom;
    private String prenom;
    private String email;
    private String telephone;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Past(message = "Date de naissance invalide")
    private LocalDate dateNaissance;

    private String sexe;
    private String adresse;
    private String motDePasse;
    private String confirmation;

    // --- Candidature ---
    @NotBlank(message = "Le diplôme est obligatoire")
    @Size(max = 150)
    private String diplome;

    @NotNull(message = "Indiquez vos années d'expérience")
    @Min(value = 0, message = "Valeur invalide")
    private Integer anneesExperience;

    @NotBlank(message = "La lettre de motivation est obligatoire")
    private String lettreMotivation;
}
