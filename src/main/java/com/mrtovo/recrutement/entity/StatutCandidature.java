package com.mrtovo.recrutement.entity;

import com.mrtovo.commun.Libelles;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Statut d'une candidature (section 3) : reçue → présélectionnée → en entretien → retenue → embauchée ;
 * avant « embauchée » : → rejetée (motif obligatoire) ou → retirée (par le candidat).
 */
@Entity
@Table(name = "statut_candidature")
public class StatutCandidature {

	public static final String RECUE = "reçue";
	public static final String PRESELECTIONNEE = "présélectionnée";
	public static final String EN_ENTRETIEN = "en entretien";
	public static final String RETENUE = "retenue";
	public static final String REJETEE = "rejetée";
	public static final String RETIREE = "retirée";
	public static final String EMBAUCHEE = "embauchée";

	@Id
	@Column(name = "id_statut_candidature")
	private Integer idStatutCandidature;

	@Column(nullable = false, unique = true, length = 50)
	private String libelle;

	public Integer getIdStatutCandidature() {
		return idStatutCandidature;
	}

	public String getLibelle() {
		return libelle;
	}

	/** Libellé sans accent ni espace, pour les classes CSS : {@code statut-en-entretien}. */
	public String getCode() {
		return Libelles.code(libelle);
	}

	public boolean est(String libelleStatut) {
		return libelleStatut.equals(libelle);
	}

}
