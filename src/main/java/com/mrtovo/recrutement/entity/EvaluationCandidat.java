package com.mrtovo.recrutement.entity;

import java.time.LocalDateTime;

import com.mrtovo.commun.entity.Compte;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Évaluation d'un entretien par un évaluateur (une seule par évaluateur et par entretien), note sur 20.
 */
@Entity
@Table(name = "evaluation_candidat")
public class EvaluationCandidat {

	public enum Avis {
		FAVORABLE("Favorable"),
		RESERVE("Réservé"),
		DEFAVORABLE("Défavorable");

		private final String libelle;

		Avis(String libelle) {
			this.libelle = libelle;
		}

		public String getLibelle() {
			return libelle;
		}
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_evaluation")
	private Integer idEvaluation;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_entretien", nullable = false)
	private Entretien entretien;

	@Column(nullable = false)
	private Integer note;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Avis avis;

	@Column(columnDefinition = "TEXT")
	private String commentaire;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_evaluateur", nullable = false)
	private Compte evaluateur;

	@Column(name = "date_evaluation", nullable = false, updatable = false)
	private LocalDateTime dateEvaluation;

	@PrePersist
	void avantCreation() {
		if (dateEvaluation == null) {
			dateEvaluation = LocalDateTime.now();
		}
	}

	public Integer getIdEvaluation() {
		return idEvaluation;
	}

	public Entretien getEntretien() {
		return entretien;
	}

	public void setEntretien(Entretien entretien) {
		this.entretien = entretien;
	}

	public Integer getNote() {
		return note;
	}

	public void setNote(Integer note) {
		this.note = note;
	}

	public Avis getAvis() {
		return avis;
	}

	public void setAvis(Avis avis) {
		this.avis = avis;
	}

	public String getCommentaire() {
		return commentaire;
	}

	public void setCommentaire(String commentaire) {
		this.commentaire = commentaire;
	}

	public Compte getEvaluateur() {
		return evaluateur;
	}

	public void setEvaluateur(Compte evaluateur) {
		this.evaluateur = evaluateur;
	}

	public LocalDateTime getDateEvaluation() {
		return dateEvaluation;
	}

}
