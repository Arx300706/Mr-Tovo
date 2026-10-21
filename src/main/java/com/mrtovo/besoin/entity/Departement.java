package com.mrtovo.besoin.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mrtovo.embauche.entity.Employe;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Département de l'entreprise, avec son manager (un employé).
 */
@Entity
@Table(name = "departement")
public class Departement {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_departement")
	private Integer idDepartement;

	@Column(nullable = false, unique = true, length = 100)
	private String libelle;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_manager")
	private Employe manager;

	/** Numéro seul en JSON : la relation est paresseuse (évite les boucles employé, contrat, département). */
	public Integer getIdManager() {
		return manager == null ? null : manager.getIdEmploye();
	}

	public Integer getIdDepartement() {
		return idDepartement;
	}

	public void setIdDepartement(Integer idDepartement) {
		this.idDepartement = idDepartement;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public Employe getManager() {
		return manager;
	}

	public void setManager(Employe manager) {
		this.manager = manager;
	}

}
