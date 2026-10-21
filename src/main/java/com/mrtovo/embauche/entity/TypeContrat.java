package com.mrtovo.embauche.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Type de contrat (CE, CDD, CDI), table de référence.
 * Créée par BK2 pour les offres ; modifiable par l'API REST {@code /api/types-contrat}.
 */
@Entity
@Table(name = "type_contrat")
public class TypeContrat {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_type_contrat")
	private Integer idTypeContrat;

	@Column(nullable = false, unique = true, length = 50)
	private String libelle;

	public Integer getIdTypeContrat() {
		return idTypeContrat;
	}

	public void setIdTypeContrat(Integer idTypeContrat) {
		this.idTypeContrat = idTypeContrat;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

}
