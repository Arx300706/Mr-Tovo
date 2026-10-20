package com.mrtovo.recrutement.entity;

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
 * Candidature d'un candidat à une offre (une seule par candidat et par offre).
 */
@Entity
@Table(name = "candidature")
public class Candidature {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_candidature")
	private Integer idCandidature;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_candidat", nullable = false)
	private Candidat candidat;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_offre", nullable = false)
	private Offre offre;

	@Column(name = "date_candidature", nullable = false, updatable = false)
	private LocalDateTime dateCandidature;

	@Column(columnDefinition = "TEXT")
	private String lettre;

	/** Document CV dans la GED (BK0-05) ; vide tant que la GED n'est pas prête. */
	@Column(name = "id_document_cv")
	private Integer idDocumentCv;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_statut_candidature", nullable = false)
	private StatutCandidature statutCandidature;

	@Column(name = "motif_rejet", length = 500)
	private String motifRejet;

	@Column(name = "date_modification", nullable = false)
	private LocalDateTime dateModification;

	@PrePersist
	void avantCreation() {
		if (dateCandidature == null) {
			dateCandidature = LocalDateTime.now();
		}
		dateModification = LocalDateTime.now();
	}

	@PreUpdate
	void avantModification() {
		dateModification = LocalDateTime.now();
	}

	public Integer getIdCandidature() {
		return idCandidature;
	}

	public Candidat getCandidat() {
		return candidat;
	}

	public void setCandidat(Candidat candidat) {
		this.candidat = candidat;
	}

	public Offre getOffre() {
		return offre;
	}

	public void setOffre(Offre offre) {
		this.offre = offre;
	}

	public LocalDateTime getDateCandidature() {
		return dateCandidature;
	}

	public void setDateCandidature(LocalDateTime dateCandidature) {
		this.dateCandidature = dateCandidature;
	}

	public String getLettre() {
		return lettre;
	}

	public void setLettre(String lettre) {
		this.lettre = lettre;
	}

	public Integer getIdDocumentCv() {
		return idDocumentCv;
	}

	public void setIdDocumentCv(Integer idDocumentCv) {
		this.idDocumentCv = idDocumentCv;
	}

	public StatutCandidature getStatutCandidature() {
		return statutCandidature;
	}

	public void setStatutCandidature(StatutCandidature statutCandidature) {
		this.statutCandidature = statutCandidature;
	}

	public String getMotifRejet() {
		return motifRejet;
	}

	public void setMotifRejet(String motifRejet) {
		this.motifRejet = motifRejet;
	}

	public LocalDateTime getDateModification() {
		return dateModification;
	}

}
