package com.mrtovo.recrutement.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Size;

/**
 * Formulaire « Postuler » d'une offre publique (/carrieres/{id}/postuler).
 * Le CV est vérifié par {@code CandidatureService} (format et taille).
 */
public class CandidatureFormulaire {

	@Size(max = 5000, message = "La lettre de motivation fait 5 000 caractères maximum")
	private String lettre;

	private MultipartFile cv;

	public String getLettre() {
		return lettre;
	}

	public void setLettre(String lettre) {
		this.lettre = lettre;
	}

	public MultipartFile getCv() {
		return cv;
	}

	public void setCv(MultipartFile cv) {
		this.cv = cv;
	}

}
