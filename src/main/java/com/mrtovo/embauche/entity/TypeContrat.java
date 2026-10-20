package com.mrtovo.embauche.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Type de contrat (CE, CDD, CDI), table de référence.
 * Version minimale créée par BK2 pour les offres ; BK3 la complète si besoin.
 */
@Entity
@Table(name = "type_contrat")
public class TypeContrat {

	@Id
	@Column(name = "id_type_contrat")
	private Integer idTypeContrat;

	@Column(nullable = false, unique = true, length = 50)
	private String libelle;

	public Integer getIdTypeContrat() {
		return idTypeContrat;
	}

	public String getLibelle() {
		return libelle;
	}

}
