package com.mrtovo.commun.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Compte de connexion : relié à un employé (identifiant = matricule)
 * <b>ou</b> à un candidat (identifiant = e-mail).
 */
@Entity
@Table(name = "compte")
public class Compte {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_compte")
	private Integer idCompte;

	@Column(nullable = false, unique = true, length = 150)
	private String identifiant;

	@Column(name = "mot_de_passe", nullable = false)
	private String motDePasse;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Role role;

	@Column(name = "id_employe")
	private Integer idEmploye;

	@Column(name = "id_candidat")
	private Integer idCandidat;

	@Column(nullable = false)
	private boolean actif = true;

	@Column(name = "date_creation", nullable = false, updatable = false)
	private LocalDateTime dateCreation;

	@PrePersist
	void avantCreation() {
		if (dateCreation == null) {
			dateCreation = LocalDateTime.now();
		}
	}

	public Integer getIdCompte() {
		return idCompte;
	}

	public String getIdentifiant() {
		return identifiant;
	}

	public void setIdentifiant(String identifiant) {
		this.identifiant = identifiant;
	}

	public String getMotDePasse() {
		return motDePasse;
	}

	public void setMotDePasse(String motDePasse) {
		this.motDePasse = motDePasse;
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

	public Integer getIdCandidat() {
		return idCandidat;
	}

	public void setIdCandidat(Integer idCandidat) {
		this.idCandidat = idCandidat;
	}

	public boolean isActif() {
		return actif;
	}

	public void setActif(boolean actif) {
		this.actif = actif;
	}

	public LocalDateTime getDateCreation() {
		return dateCreation;
	}

}
