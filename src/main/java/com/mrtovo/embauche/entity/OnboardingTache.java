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
 * Tâche d'un parcours d'onboarding (badge, matériel, formation…) confiée à un responsable.
 */
@Entity
@Table(name = "onboarding_tache")
public class OnboardingTache {

	/** Nature de la tâche. */
	public enum Categorie {
		CONTRAT_SIGNE("Contrat signé"),
		BADGE("Badge"),
		COMPTE_INFORMATIQUE("Compte informatique"),
		MATERIEL("Matériel"),
		FORMATION_ACCUEIL("Formation d'accueil"),
		INSCRIPTION_PAI("Inscription PAI");

		private final String libelle;

		Categorie(String libelle) {
			this.libelle = libelle;
		}

		public String getLibelle() {
			return libelle;
		}
	}

	/** Avancement de la tâche. */
	public enum Statut {
		A_FAIRE("À faire"),
		FAIT("Fait"),
		NON_APPLICABLE("Non applicable");

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
	@Column(name = "id_onboarding_tache")
	private Integer idOnboardingTache;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_onboarding", nullable = false)
	private Onboarding onboarding;

	@Column(nullable = false, length = 150)
	private String libelle;

	@Enumerated(EnumType.STRING)
	@Column(length = 30)
	private Categorie categorie;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_responsable")
	private Employe responsable;

	private LocalDate echeance;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Statut statut = Statut.A_FAIRE;

	/** Numéro seul en JSON : la relation est paresseuse (évite les boucles employé, contrat, département). */
	public Integer getIdResponsable() {
		return responsable == null ? null : responsable.getIdEmploye();
	}

	public Integer getIdOnboardingTache() {
		return idOnboardingTache;
	}

	public void setIdOnboardingTache(Integer idOnboardingTache) {
		this.idOnboardingTache = idOnboardingTache;
	}

	public Onboarding getOnboarding() {
		return onboarding;
	}

	public void setOnboarding(Onboarding onboarding) {
		this.onboarding = onboarding;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public Categorie getCategorie() {
		return categorie;
	}

	public void setCategorie(Categorie categorie) {
		this.categorie = categorie;
	}

	public Employe getResponsable() {
		return responsable;
	}

	public void setResponsable(Employe responsable) {
		this.responsable = responsable;
	}

	public LocalDate getEcheance() {
		return echeance;
	}

	public void setEcheance(LocalDate echeance) {
		this.echeance = echeance;
	}

	public Statut getStatut() {
		return statut;
	}

	public void setStatut(Statut statut) {
		this.statut = statut;
	}

}
