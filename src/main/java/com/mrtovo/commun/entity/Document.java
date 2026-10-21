package com.mrtovo.commun.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mrtovo.embauche.entity.Contrat;
import com.mrtovo.embauche.entity.Employe;
import com.mrtovo.embauche.entity.OnboardingTache;
import com.mrtovo.recrutement.entity.Candidat;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Document de la GED (BK0-05) : rattaché à un candidat, un employé, un contrat ou une tâche d'onboarding.
 */
@Entity
@Table(name = "document")
public class Document {

	/** Nature du document. */
	public enum Type {
		CV("CV"),
		CONTRAT_SIGNE("Contrat signé"),
		PIECE_IDENTITE("Pièce d'identité"),
		JUSTIFICATIF_DOMICILE("Justificatif de domicile"),
		DIPLOME("Diplôme"),
		AUTRE("Autre");

		private final String libelle;

		Type(String libelle) {
			this.libelle = libelle;
		}

		public String getLibelle() {
			return libelle;
		}
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_document")
	private Integer idDocument;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private Type type;

	@Column(name = "nom_fichier", length = 255)
	private String nomFichier;

	@Column(columnDefinition = "LONGBLOB")
	private byte[] fichier;

	@Column(name = "date_document", nullable = false)
	private LocalDateTime dateDocument;

	@ManyToOne
	@JoinColumn(name = "id_candidat")
	private Candidat candidat;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_employe")
	private Employe employe;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_contrat")
	private Contrat contrat;

	@ManyToOne
	@JoinColumn(name = "id_onboarding_tache")
	private OnboardingTache onboardingTache;

	@PrePersist
	void avantCreation() {
		if (dateDocument == null) {
			dateDocument = LocalDateTime.now();
		}
	}

	/** Numéro seul en JSON : la relation est paresseuse (évite les boucles employé, contrat, département). */
	public Integer getIdEmploye() {
		return employe == null ? null : employe.getIdEmploye();
	}

	/** Numéro seul en JSON : la relation est paresseuse (évite les boucles employé, contrat, département). */
	public Integer getIdContrat() {
		return contrat == null ? null : contrat.getIdContrat();
	}

	public Integer getIdDocument() {
		return idDocument;
	}

	public void setIdDocument(Integer idDocument) {
		this.idDocument = idDocument;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getNomFichier() {
		return nomFichier;
	}

	public void setNomFichier(String nomFichier) {
		this.nomFichier = nomFichier;
	}

	public byte[] getFichier() {
		return fichier;
	}

	public void setFichier(byte[] fichier) {
		this.fichier = fichier;
	}

	public LocalDateTime getDateDocument() {
		return dateDocument;
	}

	public void setDateDocument(LocalDateTime dateDocument) {
		this.dateDocument = dateDocument;
	}

	public Candidat getCandidat() {
		return candidat;
	}

	public void setCandidat(Candidat candidat) {
		this.candidat = candidat;
	}

	public Employe getEmploye() {
		return employe;
	}

	public void setEmploye(Employe employe) {
		this.employe = employe;
	}

	public Contrat getContrat() {
		return contrat;
	}

	public void setContrat(Contrat contrat) {
		this.contrat = contrat;
	}

	public OnboardingTache getOnboardingTache() {
		return onboardingTache;
	}

	public void setOnboardingTache(OnboardingTache onboardingTache) {
		this.onboardingTache = onboardingTache;
	}

}
