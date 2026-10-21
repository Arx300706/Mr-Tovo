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
import jakarta.persistence.Table;

/**
 * Entretien d'une candidature, avec son évaluateur (compte d'un manager ou des RH).
 */
@Entity
@Table(name = "entretien")
public class Entretien {

	public enum Type {
		TELEPHONIQUE("Téléphonique"),
		PHYSIQUE("Sur place"),
		VISIO("Visioconférence");

		private final String libelle;

		Type(String libelle) {
			this.libelle = libelle;
		}

		public String getLibelle() {
			return libelle;
		}
	}

	public enum Statut {
		PLANIFIE("Planifié"),
		REALISE("Réalisé"),
		ANNULE("Annulé");

		private final String libelle;

		Statut(String libelle) {
			this.libelle = libelle;
		}

		public String getLibelle() {
			return libelle;
		}
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_entretien")
	private Integer idEntretien;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_candidature", nullable = false)
	private Candidature candidature;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Type type;

	@Column(name = "date_entretien", nullable = false)
	private LocalDateTime dateEntretien;

	@Column(length = 200)
	private String lieu;

	@ManyToOne
	@JoinColumn(name = "id_evaluateur")
	private Compte evaluateur;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Statut statut = Statut.PLANIFIE;

	public Integer getIdEntretien() {
		return idEntretien;
	}

	public Candidature getCandidature() {
		return candidature;
	}

	public void setCandidature(Candidature candidature) {
		this.candidature = candidature;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public LocalDateTime getDateEntretien() {
		return dateEntretien;
	}

	public void setDateEntretien(LocalDateTime dateEntretien) {
		this.dateEntretien = dateEntretien;
	}

	public String getLieu() {
		return lieu;
	}

	public void setLieu(String lieu) {
		this.lieu = lieu;
	}

	public Compte getEvaluateur() {
		return evaluateur;
	}

	public void setEvaluateur(Compte evaluateur) {
		this.evaluateur = evaluateur;
	}

	public Statut getStatut() {
		return statut;
	}

	public void setStatut(Statut statut) {
		this.statut = statut;
	}

}
