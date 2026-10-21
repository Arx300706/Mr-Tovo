package com.mrtovo.embauche.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Parcours d'intégration d'un nouvel employé (BK4) ; ses tâches sont dans {@link OnboardingTache}.
 */
@Entity
@Table(name = "onboarding")
public class Onboarding {

	/** Avancement du parcours. */
	public enum Statut {
		EN_COURS("En cours"),
		TERMINE("Terminé"),
		ABANDONNE("Abandonné");

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
	@Column(name = "id_onboarding")
	private Integer idOnboarding;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_employe", nullable = false)
	private Employe employe;

	@Column(name = "date_debut")
	private LocalDate dateDebut;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Statut statut = Statut.EN_COURS;

	@Column(nullable = false)
	private int progression;

	/** Numéro seul en JSON : la relation est paresseuse (évite les boucles employé, contrat, département). */
	public Integer getIdEmploye() {
		return employe == null ? null : employe.getIdEmploye();
	}

	public Integer getIdOnboarding() {
		return idOnboarding;
	}

	public void setIdOnboarding(Integer idOnboarding) {
		this.idOnboarding = idOnboarding;
	}

	public Employe getEmploye() {
		return employe;
	}

	public void setEmploye(Employe employe) {
		this.employe = employe;
	}

	public LocalDate getDateDebut() {
		return dateDebut;
	}

	public void setDateDebut(LocalDate dateDebut) {
		this.dateDebut = dateDebut;
	}

	public Statut getStatut() {
		return statut;
	}

	public void setStatut(Statut statut) {
		this.statut = statut;
	}

	public int getProgression() {
		return progression;
	}

	public void setProgression(int progression) {
		this.progression = progression;
	}

}
