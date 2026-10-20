package com.mrtovo.recrutement.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.mrtovo.embauche.entity.TypeContrat;

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
 * Offre d'emploi (section 2.4). Elle vient d'un besoin validé (BK1) ;
 * Id_Besoin, Id_Poste et Id_Departement restent de simples numéros tant que les entités de BK1 n'existent pas.
 */
@Entity
@Table(name = "offre")
public class Offre {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_offre")
	private Integer idOffre;

	@Column(name = "id_besoin")
	private Integer idBesoin;

	@Column(name = "id_poste")
	private Integer idPoste;

	@Column(name = "id_departement")
	private Integer idDepartement;

	@Column(nullable = false, length = 150)
	private String titre;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(columnDefinition = "TEXT")
	private String profil;

	@ManyToOne
	@JoinColumn(name = "id_type_contrat")
	private TypeContrat typeContrat;

	@Column(length = 150)
	private String lieu;

	@Column(name = "date_publication")
	private LocalDate datePublication;

	@Column(name = "date_limite")
	private LocalDate dateLimite;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_statut_offre", nullable = false)
	private StatutOffre statutOffre;

	@Column(name = "date_creation", nullable = false, updatable = false)
	private LocalDateTime dateCreation;

	@Column(name = "date_modification", nullable = false)
	private LocalDateTime dateModification;

	@PrePersist
	void avantCreation() {
		dateCreation = LocalDateTime.now();
		dateModification = dateCreation;
	}

	@PreUpdate
	void avantModification() {
		dateModification = LocalDateTime.now();
	}

	/** Visible au public : publiée et date limite non dépassée. */
	public boolean estVisiblePubliquement(LocalDate aujourdhui) {
		return statutOffre != null && statutOffre.est(StatutOffre.PUBLIEE)
			&& (dateLimite == null || !dateLimite.isBefore(aujourdhui));
	}

	public boolean estBrouillon() {
		return statutOffre != null && statutOffre.est(StatutOffre.BROUILLON);
	}

	public Integer getIdOffre() {
		return idOffre;
	}

	public Integer getIdBesoin() {
		return idBesoin;
	}

	public void setIdBesoin(Integer idBesoin) {
		this.idBesoin = idBesoin;
	}

	public Integer getIdPoste() {
		return idPoste;
	}

	public void setIdPoste(Integer idPoste) {
		this.idPoste = idPoste;
	}

	public Integer getIdDepartement() {
		return idDepartement;
	}

	public void setIdDepartement(Integer idDepartement) {
		this.idDepartement = idDepartement;
	}

	public String getTitre() {
		return titre;
	}

	public void setTitre(String titre) {
		this.titre = titre;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProfil() {
		return profil;
	}

	public void setProfil(String profil) {
		this.profil = profil;
	}

	public TypeContrat getTypeContrat() {
		return typeContrat;
	}

	public void setTypeContrat(TypeContrat typeContrat) {
		this.typeContrat = typeContrat;
	}

	public String getLieu() {
		return lieu;
	}

	public void setLieu(String lieu) {
		this.lieu = lieu;
	}

	public LocalDate getDatePublication() {
		return datePublication;
	}

	public void setDatePublication(LocalDate datePublication) {
		this.datePublication = datePublication;
	}

	public LocalDate getDateLimite() {
		return dateLimite;
	}

	public void setDateLimite(LocalDate dateLimite) {
		this.dateLimite = dateLimite;
	}

	public StatutOffre getStatutOffre() {
		return statutOffre;
	}

	public void setStatutOffre(StatutOffre statutOffre) {
		this.statutOffre = statutOffre;
	}

	public LocalDateTime getDateCreation() {
		return dateCreation;
	}

	public LocalDateTime getDateModification() {
		return dateModification;
	}

}
