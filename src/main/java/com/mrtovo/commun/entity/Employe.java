package com.mrtovo.commun.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "employe")
public class Employe {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_employe")
	private Integer idEmploye;

	@Column(nullable = false)
	private String nom;

	@Column(nullable = false)
	private String prenom;

	@Column(nullable = false, unique = true, length = 30)
	private String matricule;

	@Column(name = "mot_de_passe", nullable = false)
	private String motDePasse;

	public Integer getIdEmploye() {
		return idEmploye;
	}

	public String getNom() {
		return nom;
	}

	public String getPrenom() {
		return prenom;
	}

	public String getMatricule() {
		return matricule;
	}

	public String getMotDePasse() {
		return motDePasse;
	}

	public void setMotDePasse(String motDePasse) {
		this.motDePasse = motDePasse;
	}
}