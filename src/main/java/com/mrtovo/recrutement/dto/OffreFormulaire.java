package com.mrtovo.recrutement.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.mrtovo.recrutement.entity.Offre;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Formulaire de création / modification d'une offre en brouillon (/offres/nouveau, /offres/{id}/modifier).
 */
public class OffreFormulaire {

	@NotBlank(message = "Le titre est obligatoire")
	@Size(max = 150, message = "150 caractères maximum")
	private String titre;

	@Size(max = 10000, message = "10 000 caractères maximum")
	private String description;

	@Size(max = 10000, message = "10 000 caractères maximum")
	private String profil;

	private Integer idTypeContrat;

	@Size(max = 150, message = "150 caractères maximum")
	private String lieu;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate dateLimite;

	public static OffreFormulaire depuis(Offre offre) {
		OffreFormulaire formulaire = new OffreFormulaire();
		formulaire.titre = offre.getTitre();
		formulaire.description = offre.getDescription();
		formulaire.profil = offre.getProfil();
		formulaire.idTypeContrat = offre.getTypeContrat() == null ? null : offre.getTypeContrat().getIdTypeContrat();
		formulaire.lieu = offre.getLieu();
		formulaire.dateLimite = offre.getDateLimite();
		return formulaire;
	}

	public String getTitre() {
		return titre;
	}

	public void setTitre(String titre) {
		this.titre = titre;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProfil() {
		return profil;
	}

	public void setProfil(String profil) {
		this.profil = profil;
	}

	public Integer getIdTypeContrat() {
		return idTypeContrat;
	}

	public void setIdTypeContrat(Integer idTypeContrat) {
		this.idTypeContrat = idTypeContrat;
	}

	public String getLieu() {
		return lieu;
	}

	public void setLieu(String lieu) {
		this.lieu = lieu;
	}

	public LocalDate getDateLimite() {
		return dateLimite;
	}

	public void setDateLimite(LocalDate dateLimite) {
		this.dateLimite = dateLimite;
	}

}
