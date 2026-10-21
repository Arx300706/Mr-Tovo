package com.mrtovo.commun.securite;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.mrtovo.commun.entity.Compte;
import com.mrtovo.commun.entity.Employe;
import com.mrtovo.commun.entity.Role;

/**
 * Utilisateur connecté, disponible dans toutes les pages sous le nom {@code utilisateur}
 * et dans les contrôleurs via {@code @AuthenticationPrincipal UtilisateurConnecte utilisateur}.
 */
public class UtilisateurConnecte implements UserDetails {

	private final Integer idCompte;
	private final String identifiant;
	private final String motDePasse;
	private final Role role;
	private final Integer idEmploye;
	private final Integer idCandidat;
	private final String nomAffiche;
	private final boolean actif;

	public UtilisateurConnecte(Compte compte, String nomAffiche) {
		this.idCompte = compte.getIdCompte();
		this.identifiant = compte.getIdentifiant();
		this.motDePasse = compte.getMotDePasse();
		this.role = compte.getRole();
		this.idEmploye = compte.getIdEmploye();
		this.idCandidat = compte.getIdCandidat();
		this.nomAffiche = nomAffiche;
		this.actif = compte.isActif();
	}

	public UtilisateurConnecte(Employe employe) {
		this.idCompte = null;
		this.identifiant = employe.getMatricule();
		this.motDePasse = employe.getMotDePasse();
		this.role = Role.EMPLOYE;
		this.idEmploye = employe.getIdEmploye();
		this.idCandidat = null;
		this.nomAffiche = employe.getPrenom() + " " + employe.getNom();
		this.actif = true;
	}

	public Integer getIdCompte() {
		return idCompte;
	}

	public String getIdentifiant() {
		return identifiant;
	}

	public Role getRole() {
		return role;
	}

	public Integer getIdEmploye() {
		return idEmploye;
	}

	public Integer getIdCandidat() {
		return idCandidat;
	}

	public String getNomAffiche() {
		return nomAffiche;
	}

	public boolean estCandidat() {
		return role.estCandidat();
	}

	public boolean aLeRole(Role... roles) {
		for (Role r : roles) {
			if (r == role) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(role.autorite()));
	}

	@Override
	public String getPassword() {
		return motDePasse;
	}

	@Override
	public String getUsername() {
		return identifiant;
	}

	@Override
	public boolean isEnabled() {
		return actif;
	}

}
