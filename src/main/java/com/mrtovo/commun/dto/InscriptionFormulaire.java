package com.mrtovo.commun.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Formulaire de création de compte candidat (/inscription).
 */
public class InscriptionFormulaire {

	@NotBlank(message = "Le nom est obligatoire")
	@Size(max = 100)
	private String nom;

	@NotBlank(message = "Le prénom est obligatoire")
	@Size(max = 100)
	private String prenom;

	@NotBlank(message = "L'e-mail est obligatoire")
	@Email(message = "L'e-mail n'est pas valide")
	@Size(max = 150)
	private String email;

	@Size(max = 30)
	private String telephone;

	@NotBlank(message = "Le mot de passe est obligatoire")
	@Size(min = 8, max = 100, message = "Le mot de passe doit contenir au moins 8 caractères")
	private String motDePasse;

	@NotBlank(message = "Merci de confirmer le mot de passe")
	private String confirmationMotDePasse;

	@AssertTrue(message = "Les deux mots de passe ne sont pas identiques")
	public boolean isMotsDePasseIdentiques() {
		return motDePasse == null || motDePasse.equals(confirmationMotDePasse);
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getPrenom() {
		return prenom;
	}

	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getMotDePasse() {
		return motDePasse;
	}

	public void setMotDePasse(String motDePasse) {
		this.motDePasse = motDePasse;
	}

	public String getConfirmationMotDePasse() {
		return confirmationMotDePasse;
	}

	public void setConfirmationMotDePasse(String confirmationMotDePasse) {
		this.confirmationMotDePasse = confirmationMotDePasse;
	}

}
