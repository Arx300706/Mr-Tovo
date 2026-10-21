package com.mrtovo.commun.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Une ligne par action importante (qui, quand, quoi). On n'efface jamais une ligne.
 * BK0-08 branchera aussi le moteur de statuts dessus.
 */
@Entity
@Table(name = "journal_audit")
public class JournalAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_journal_audit")
	private Integer idJournalAudit;

	@Column(name = "date_action", nullable = false, updatable = false)
	private LocalDateTime dateAction;

	/** Identifiant du compte auteur, ou « systeme » pour les tâches automatiques. */
	@Column(nullable = false, length = 150, updatable = false)
	private String auteur;

	@Column(nullable = false, length = 50, updatable = false)
	private String action;

	@Column(nullable = false, length = 50, updatable = false)
	private String objet;

	@Column(name = "id_objet", updatable = false)
	private Integer idObjet;

	@Column(length = 500, updatable = false)
	private String detail;

	protected JournalAudit() {
	}

	public JournalAudit(String auteur, String action, String objet, Integer idObjet, String detail) {
		this.auteur = auteur;
		this.action = action;
		this.objet = objet;
		this.idObjet = idObjet;
		this.detail = detail;
	}

	@PrePersist
	void avantCreation() {
		if (dateAction == null) {
			dateAction = LocalDateTime.now();
		}
	}

	public Integer getIdJournalAudit() {
		return idJournalAudit;
	}

	public LocalDateTime getDateAction() {
		return dateAction;
	}

	public String getAuteur() {
		return auteur;
	}

	public String getAction() {
		return action;
	}

	public String getObjet() {
		return objet;
	}

	public Integer getIdObjet() {
		return idObjet;
	}

	public String getDetail() {
		return detail;
	}

}
