package com.mrtovo.embauche.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mrtovo.besoin.entity.Poste;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Historique des postes et contrats d'un employé : une ligne par changement.
 */
@Entity
@Table(name = "historique_employe")
public class HistoriqueEmploye {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_historique_employe")
	private Integer idHistoriqueEmploye;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_employe", nullable = false)
	private Employe employe;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_poste", nullable = false)
	private Poste poste;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_contrat")
	private Contrat contrat;

	@Column(nullable = false)
	private LocalDateTime date;

	@PrePersist
	void avantCreation() {
		if (date == null) {
			date = LocalDateTime.now();
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

	public Integer getIdHistoriqueEmploye() {
		return idHistoriqueEmploye;
	}

	public void setIdHistoriqueEmploye(Integer idHistoriqueEmploye) {
		this.idHistoriqueEmploye = idHistoriqueEmploye;
	}

	public Employe getEmploye() {
		return employe;
	}

	public void setEmploye(Employe employe) {
		this.employe = employe;
	}

	public Poste getPoste() {
		return poste;
	}

	public void setPoste(Poste poste) {
		this.poste = poste;
	}

	public Contrat getContrat() {
		return contrat;
	}

	public void setContrat(Contrat contrat) {
		this.contrat = contrat;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

}
