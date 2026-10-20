package com.mrtovo.commun.dto;

import com.mrtovo.commun.entity.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Formulaire de création d'un compte employé par l'admin (/admin/comptes).
 */
public class CompteFormulaire {

	@NotBlank(message = "Le matricule est obligatoire")
	@Size(max = 150)
	private String identifiant;

	@NotNull(message = "Le rôle est obligatoire")
	private Role role;

	private Integer idEmploye;

	@NotBlank(message = "Le mot de passe initial est obligatoire")
	@Size(min = 8, max = 100, message = "Le mot de passe doit contenir au moins 8 caractères")
	private String motDePasse;

	public String getIdentifiant() {
		return identifiant;
	}

	public void setIdentifiant(String identifiant) {
		this.identifiant = identifiant;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Integer getIdEmploye() {
		return idEmploye;
	}

	public void setIdEmploye(Integer idEmploye) {
		this.idEmploye = idEmploye;
	}

	public String getMotDePasse() {
		return motDePasse;
	}

	public void setMotDePasse(String motDePasse) {
		this.motDePasse = motDePasse;
	}

}
