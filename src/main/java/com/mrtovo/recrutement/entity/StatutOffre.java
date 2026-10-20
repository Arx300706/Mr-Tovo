package com.mrtovo.recrutement.entity;

import com.mrtovo.commun.Libelles;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Statut d'une offre (section 3) : brouillon → publiée → clôturée → pourvue ; publiée → annulée.
 */
@Entity
@Table(name = "statut_offre")
public class StatutOffre {

	public static final String BROUILLON = "brouillon";
	public static final String PUBLIEE = "publiée";
	public static final String CLOTUREE = "clôturée";
	public static final String POURVUE = "pourvue";
	public static final String ANNULEE = "annulée";

	@Id
	@Column(name = "id_statut_offre")
	private Integer idStatutOffre;

	@Column(nullable = false, unique = true, length = 50)
	private String libelle;

	public Integer getIdStatutOffre() {
		return idStatutOffre;
	}

	public String getLibelle() {
		return libelle;
	}

	/** Libellé sans accent ni espace, pour les classes CSS : {@code statut-publiee}. */
	public String getCode() {
		return Libelles.code(libelle);
	}

	public boolean est(String libelleStatut) {
		return libelleStatut.equals(libelle);
	}

}
