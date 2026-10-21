package com.mrtovo.besoin.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Poste de l'organigramme, rattaché à un département et éventuellement à un service.
 */
@Entity
@Table(name = "poste")
public class Poste {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_poste")
	private Integer idPoste;

	@Column(nullable = false, length = 100)
	private String libelle;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_departement", nullable = false)
	private Departement departement;

	@ManyToOne
	@JoinColumn(name = "id_service")
	private Service service;

	@Column(name = "est_manager", nullable = false)
	private boolean estManager;

	@Column(name = "est_directeur", nullable = false)
	private boolean estDirecteur;

	@Column(name = "est_occupe", nullable = false)
	private boolean estOccupe;

	@Column(name = "est_actif", nullable = false)
	private boolean estActif = true;

	@Column(name = "date_modification", nullable = false)
	private LocalDateTime dateModification;

	@PrePersist
	@PreUpdate
	void avantEnregistrement() {
		dateModification = LocalDateTime.now();
	}

	public Integer getIdPoste() {
		return idPoste;
	}

	public void setIdPoste(Integer idPoste) {
		this.idPoste = idPoste;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public Departement getDepartement() {
		return departement;
	}

	public void setDepartement(Departement departement) {
		this.departement = departement;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public boolean isEstManager() {
		return estManager;
	}

	public void setEstManager(boolean estManager) {
		this.estManager = estManager;
	}

	public boolean isEstDirecteur() {
		return estDirecteur;
	}

	public void setEstDirecteur(boolean estDirecteur) {
		this.estDirecteur = estDirecteur;
	}

	public boolean isEstOccupe() {
		return estOccupe;
	}

	public void setEstOccupe(boolean estOccupe) {
		this.estOccupe = estOccupe;
	}

	public boolean isEstActif() {
		return estActif;
	}

	public void setEstActif(boolean estActif) {
		this.estActif = estActif;
	}

	public LocalDateTime getDateModification() {
		return dateModification;
	}

	public void setDateModification(LocalDateTime dateModification) {
		this.dateModification = dateModification;
	}

}
