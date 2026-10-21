package com.mrtovo.embauche.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.mrtovo.recrutement.entity.Candidature;
import com.mrtovo.recrutement.entity.Offre;

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
 * Proposition d'embauche faite au candidat retenu sur une offre (salaire, contrat, dates).
 */
@Entity
@Table(name = "proposition")
public class Proposition {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_proposition")
	private Integer idProposition;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_offre", nullable = false)
	private Offre offre;

	@ManyToOne
	@JoinColumn(name = "id_candidature")
	private Candidature candidature;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal salaire;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_type_contrat", nullable = false)
	private TypeContrat typeContrat;

	@Column(name = "date_commencement", nullable = false)
	private LocalDate dateCommencement;

	@Column(name = "date_fin")
	private LocalDate dateFin;

	@Column(name = "date_limite_validation")
	private LocalDate dateLimiteValidation;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_statut_proposition", nullable = false)
	private StatutProposition statutProposition;

	@Column(name = "date_modification", nullable = false)
	private LocalDateTime dateModification;

	@PrePersist
	@PreUpdate
	void avantEnregistrement() {
		dateModification = LocalDateTime.now();
	}

	public Integer getIdProposition() {
		return idProposition;
	}

	public void setIdProposition(Integer idProposition) {
		this.idProposition = idProposition;
	}

	public Offre getOffre() {
		return offre;
	}

	public void setOffre(Offre offre) {
		this.offre = offre;
	}

	public Candidature getCandidature() {
		return candidature;
	}

	public void setCandidature(Candidature candidature) {
		this.candidature = candidature;
	}

	public BigDecimal getSalaire() {
		return salaire;
	}

	public void setSalaire(BigDecimal salaire) {
		this.salaire = salaire;
	}

	public TypeContrat getTypeContrat() {
		return typeContrat;
	}

	public void setTypeContrat(TypeContrat typeContrat) {
		this.typeContrat = typeContrat;
	}

	public LocalDate getDateCommencement() {
		return dateCommencement;
	}

	public void setDateCommencement(LocalDate dateCommencement) {
		this.dateCommencement = dateCommencement;
	}

	public LocalDate getDateFin() {
		return dateFin;
	}

	public void setDateFin(LocalDate dateFin) {
		this.dateFin = dateFin;
	}

	public LocalDate getDateLimiteValidation() {
		return dateLimiteValidation;
	}

	public void setDateLimiteValidation(LocalDate dateLimiteValidation) {
		this.dateLimiteValidation = dateLimiteValidation;
	}

	public StatutProposition getStatutProposition() {
		return statutProposition;
	}

	public void setStatutProposition(StatutProposition statutProposition) {
		this.statutProposition = statutProposition;
	}

	public LocalDateTime getDateModification() {
		return dateModification;
	}

	public void setDateModification(LocalDateTime dateModification) {
		this.dateModification = dateModification;
	}

}
