package com.mrtovo.commun.entity;

/**
 * Rôles de l'application (section 4 du TODO).
 * Spring Security les voit sous la forme ROLE_xxx.
 */
public enum Role {
	CANDIDAT("Candidat"),
	EMPLOYE("Employé"),
	MANAGER("Manager"),
	RH("Ressources humaines"),
	FINANCE("Finance"),
	ADMIN("Administrateur");

	private final String libelle;

	Role(String libelle) {
		this.libelle = libelle;
	}

	public String getLibelle() {
		return libelle;
	}

	public String autorite() {
		return "ROLE_" + name();
	}

	public boolean estCandidat() {
		return this == CANDIDAT;
	}
}
