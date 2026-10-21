package com.mrtovo.embauche.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Contrat d'un employé ; un renouvellement ou un avenant pointe vers le contrat précédent.
 */
@Entity
@Table(name = "contrat")
public class Contrat {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_contrat")
	private Integer idContrat;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_employe", nullable = false)
	private Employe employe;

	@ManyToOne
	@JoinColumn(name = "id_proposition")
	private Proposition proposition;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_statut_contrat", nullable = false)
	private StatutContrat statutContrat;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_type_contrat", nullable = false)
	private TypeContrat typeContrat;

	@Column(name = "date_debut")
	private LocalDate dateDebut;

	@Column(name = "date_fin")
	private LocalDate dateFin;

	@Column(precision = 15, scale = 2)
	private BigDecimal salaire;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_contrat_precedent")
	private Contrat contratPrecedent;

	/** Numéro seul en JSON : la relation est paresseuse (évite les boucles employé, contrat, département). */
	public Integer getIdEmploye() {
		return employe == null ? null : employe.getIdEmploye();
	}

	/** Numéro seul en JSON : la relation est paresseuse (évite les boucles employé, contrat, département). */
	public Integer getIdContratPrecedent() {
		return contratPrecedent == null ? null : contratPrecedent.getIdContrat();
	}

	public Integer getIdContrat() {
		return idContrat;
	}

	public void setIdContrat(Integer idContrat) {
		this.idContrat = idContrat;
	}

	public Employe getEmploye() {
		return employe;
	}

	public void setEmploye(Employe employe) {
		this.employe = employe;
	}

	public Proposition getProposition() {
		return proposition;
	}

	public void setProposition(Proposition proposition) {
		this.proposition = proposition;
	}

	public StatutContrat getStatutContrat() {
		return statutContrat;
	}

	public void setStatutContrat(StatutContrat statutContrat) {
		this.statutContrat = statutContrat;
	}

	public TypeContrat getTypeContrat() {
		return typeContrat;
	}

	public void setTypeContrat(TypeContrat typeContrat) {
		this.typeContrat = typeContrat;
	}

	public LocalDate getDateDebut() {
		return dateDebut;
	}

	public void setDateDebut(LocalDate dateDebut) {
		this.dateDebut = dateDebut;
	}

	public LocalDate getDateFin() {
		return dateFin;
	}

	public void setDateFin(LocalDate dateFin) {
		this.dateFin = dateFin;
	}

	public BigDecimal getSalaire() {
		return salaire;
	}

	public void setSalaire(BigDecimal salaire) {
		this.salaire = salaire;
	}

	public Contrat getContratPrecedent() {
		return contratPrecedent;
	}

	public void setContratPrecedent(Contrat contratPrecedent) {
		this.contratPrecedent = contratPrecedent;
	}

}
