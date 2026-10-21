package com.mrtovo.besoin.entity;

import java.time.LocalDateTime;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Étape du circuit de validation d'un besoin (envoi RH, envoi finance…) et sa décision.
 */
@Entity
@Table(name = "validation")
public class Validation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_validation")
	private Integer idValidation;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_besoin", nullable = false)
	private Besoin besoin;

	@Column(nullable = false, length = 100)
	private String etape;

	@Column(length = 50)
	private String decision;

	@Column(length = 1000)
	private String commentaire;

	@Column(name = "date_validation", nullable = false)
	private LocalDateTime dateValidation;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_employe")
	private Employe validateur;

	@PrePersist
	void avantCreation() {
		if (dateValidation == null) {
			dateValidation = LocalDateTime.now();
		}
	}

	/** Numéro seul en JSON : la relation est paresseuse (évite les boucles employé, contrat, département). */
	public Integer getIdValidateur() {
		return validateur == null ? null : validateur.getIdEmploye();
	}

	public Integer getIdValidation() {
		return idValidation;
	}

	public void setIdValidation(Integer idValidation) {
		this.idValidation = idValidation;
	}

	public Besoin getBesoin() {
		return besoin;
	}

	public void setBesoin(Besoin besoin) {
		this.besoin = besoin;
	}

	public String getEtape() {
		return etape;
	}

	public void setEtape(String etape) {
		this.etape = etape;
	}

	public String getDecision() {
		return decision;
	}

	public void setDecision(String decision) {
		this.decision = decision;
	}

	public String getCommentaire() {
		return commentaire;
	}

	public void setCommentaire(String commentaire) {
		this.commentaire = commentaire;
	}

	public LocalDateTime getDateValidation() {
		return dateValidation;
	}

	public void setDateValidation(LocalDateTime dateValidation) {
		this.dateValidation = dateValidation;
	}

	public Employe getValidateur() {
		return validateur;
	}

	public void setValidateur(Employe validateur) {
		this.validateur = validateur;
	}

}
