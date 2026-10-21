package com.mrtovo.embauche.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Statut d'un contrat (cree, signe, valide, annule, rompu), table de référence.
 */
@Entity
@Table(name = "statut_contrat")
public class StatutContrat {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_statut_contrat")
	private Integer idStatutContrat;

	@Column(nullable = false, unique = true, length = 50)
	private String libelle;

	public Integer getIdStatutContrat() {
		return idStatutContrat;
	}

	public void setIdStatutContrat(Integer idStatutContrat) {
		this.idStatutContrat = idStatutContrat;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

}
