package com.mrtovo.besoin.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mrtovo.embauche.entity.Employe;
import com.mrtovo.embauche.entity.TypeContrat;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Besoin en personnel exprimé par un manager (section 2.3), validé ensuite par les RH et la finance.
 */
@Entity
@Table(name = "besoin")
public class Besoin {

	/** Sexe souhaité ; vide = indifférent. */
	public enum Sexe {
		H, F
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_besoin")
	private Integer idBesoin;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_manager", nullable = false)
	private Employe manager;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_type_besoin", nullable = false)
	private TypeBesoin typeBesoin;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_statut_besoin", nullable = false)
	private StatutBesoin statutBesoin;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_departement", nullable = false)
	private Departement departement;

	@ManyToOne
	@JoinColumn(name = "id_service")
	private Service service;

	@ManyToOne
	@JoinColumn(name = "id_poste")
	private Poste poste;

	@Column(nullable = false, length = 150)
	private String intitule;

	@Column(nullable = false)
	private int nombre = 1;

	@Enumerated(EnumType.STRING)
	@Column(length = 1)
	private Sexe sexe;

	@Column(name = "age_min")
	private Integer ageMin;

	@Column(name = "age_max")
	private Integer ageMax;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_type_contrat", nullable = false)
	private TypeContrat typeContrat;

	@Column(name = "date_commencement")
	private LocalDate dateCommencement;

	@Column(precision = 15, scale = 2)
	private BigDecimal budget;

	@Column(columnDefinition = "TEXT")
	private String motif;

	@Column(name = "date_modification", nullable = false)
	private LocalDateTime dateModification;

	@PrePersist
	@PreUpdate
	void avantEnregistrement() {
		dateModification = LocalDateTime.now();
	}

	/** Numéro seul en JSON : la relation est paresseuse (évite les boucles employé, contrat, département). */
	public Integer getIdManager() {
		return manager == null ? null : manager.getIdEmploye();
	}

	public Integer getIdBesoin() {
		return idBesoin;
	}

	public void setIdBesoin(Integer idBesoin) {
		this.idBesoin = idBesoin;
	}

	public Employe getManager() {
		return manager;
	}

	public void setManager(Employe manager) {
		this.manager = manager;
	}

	public TypeBesoin getTypeBesoin() {
		return typeBesoin;
	}

	public void setTypeBesoin(TypeBesoin typeBesoin) {
		this.typeBesoin = typeBesoin;
	}

	public StatutBesoin getStatutBesoin() {
		return statutBesoin;
	}

	public void setStatutBesoin(StatutBesoin statutBesoin) {
		this.statutBesoin = statutBesoin;
	}

	public Departement getDepartement() {
		return departement;
	}

	public void setDepartement(Departement departement) {
		this.departement = departement;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public Poste getPoste() {
		return poste;
	}

	public void setPoste(Poste poste) {
		this.poste = poste;
	}

	public String getIntitule() {
		return intitule;
	}

	public void setIntitule(String intitule) {
		this.intitule = intitule;
	}

	public int getNombre() {
		return nombre;
	}

	public void setNombre(int nombre) {
		this.nombre = nombre;
	}

	public Sexe getSexe() {
		return sexe;
	}

	public void setSexe(Sexe sexe) {
		this.sexe = sexe;
	}

	public Integer getAgeMin() {
		return ageMin;
	}

	public void setAgeMin(Integer ageMin) {
		this.ageMin = ageMin;
	}

	public Integer getAgeMax() {
		return ageMax;
	}

	public void setAgeMax(Integer ageMax) {
		this.ageMax = ageMax;
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

	public BigDecimal getBudget() {
		return budget;
	}

	public void setBudget(BigDecimal budget) {
		this.budget = budget;
	}

	public String getMotif() {
		return motif;
	}

	public void setMotif(String motif) {
		this.motif = motif;
	}

	public LocalDateTime getDateModification() {
		return dateModification;
	}

	public void setDateModification(LocalDateTime dateModification) {
		this.dateModification = dateModification;
	}

}
