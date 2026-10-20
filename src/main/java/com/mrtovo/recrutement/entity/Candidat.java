package com.mrtovo.recrutement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Candidat (section 2.4) : distinct de l'employé, il peut avoir plusieurs candidatures.
 * Version minimale créée par BK0-04 pour l'inscription ; BK2 la complète (CV…).
 */
@Entity
@Table(name = "candidat")
public class Candidat {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_candidat")
	private Integer idCandidat;

	@Column(nullable = false, length = 100)
	private String nom;

	@Column(nullable = false, length = 100)
	private String prenom;

	@Column(nullable = false, unique = true, length = 150)
	private String email;

	@Column(length = 30)
	private String telephone;

	public Integer getIdCandidat() {
		return idCandidat;
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

}
