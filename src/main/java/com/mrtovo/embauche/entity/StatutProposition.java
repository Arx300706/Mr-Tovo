package com.mrtovo.embauche.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Statut d'une proposition d'embauche (valide, refuse, en attente), table de référence.
 */
@Entity
@Table(name = "statut_proposition")
public class StatutProposition {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_statut_proposition")
	private Integer idStatutProposition;

	@Column(nullable = false, unique = true, length = 50)
	private String libelle;

	public Integer getIdStatutProposition() {
		return idStatutProposition;
	}

	public void setIdStatutProposition(Integer idStatutProposition) {
		this.idStatutProposition = idStatutProposition;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

}
