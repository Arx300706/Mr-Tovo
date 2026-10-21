package com.mrtovo.besoin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Type de besoin (creation poste, recherche personnel, recherche stagiaire), table de référence.
 */
@Entity
@Table(name = "type_besoin")
public class TypeBesoin {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_type_besoin")
	private Integer idTypeBesoin;

	@Column(nullable = false, unique = true, length = 50)
	private String libelle;

	public Integer getIdTypeBesoin() {
		return idTypeBesoin;
	}

	public void setIdTypeBesoin(Integer idTypeBesoin) {
		this.idTypeBesoin = idTypeBesoin;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

}
