package com.mrtovo.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private Integer idVille;
    private Integer idCommune;
    private Integer idFokontany;

    @Size(max = 100, message = "Le lot ne doit pas dépasser 100 caractères")
    private String lot;
    private String motDePasse;
    private String confirmation;

    // --- Candidature ---
    @NotBlank(message = "Le diplôme est obligatoire")
    @Size(max = 150, message = "Ne doit pas dépasser 150 caractères")
    private String diplome;

    @NotNull(message = "Indiquez vos années d'expérience")
    @Min(value = 0, message = "Valeur invalide")
    private Integer anneesExperience;

    @NotBlank(message = "La lettre de motivation est obligatoire")
    private String lettreMotivation;

    // --- CV en ligne ---
    @Size(max = 150, message = "Ne doit pas dépasser 150 caractères")
    private String metierRecherche;

    @Size(max = 1200, message = "Ne doit pas dépasser 1 200 caractères")
    private String presentation;

    private String formations;
    private String competences;
    private String qualites;
    private String langues;
    private String projets;
    private String certifications;
    private String centresInteret;

    @Size(max = 255, message = "Lien trop long")
    private String linkedin;

    @Size(max = 255, message = "Lien trop long")
    private String github;

    @Size(max = 255, message = "Lien trop long")
    private String portfolio;

    /** Photo JPEG ou PNG facultative. */
    private MultipartFile photo;

    /** CIN, passeport ou permis de conduire (image ou PDF facultatif). */
    private String typePieceIdentite;
    private MultipartFile pieceIdentite;

    /** Rubriques répétables du CV en ligne, ajoutées avec les boutons « + ». */
    private List<CvElementForm> experiences = new ArrayList<>();
    private List<CvElementForm> formationsMultiples = new ArrayList<>();
    private List<CvElementForm> competencesMultiples = new ArrayList<>();
    private List<CvElementForm> qualitesMultiples = new ArrayList<>();
    private List<CvElementForm> languesMultiples = new ArrayList<>();
    private List<CvElementForm> projetsMultiples = new ArrayList<>();
    private List<CvElementForm> certificationsMultiples = new ArrayList<>();
    private List<CvElementForm> interetsMultiples = new ArrayList<>();
    private List<CvElementForm> liensMultiples = new ArrayList<>();

    @Data
    public static class CvElementForm {
        private String titre;
        private String sousTitre;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate dateDebut;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate dateFin;
        private String description;
        private String niveau;
        private String url;
    }

    /** CV au format PDF (facultatif). */
    private MultipartFile cv;
}
