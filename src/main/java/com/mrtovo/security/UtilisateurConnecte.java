package com.mrtovo.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Personne connectée : soit un employé (identifiant = matricule, id = id_employe),
 * soit un candidat (identifiant = e-mail, id = id_compte).
 */
@Getter
public class UtilisateurConnecte implements UserDetails {

    public enum Type { EMPLOYE, CANDIDAT }

    private final Type type;
    private final Integer id;
    private final String identifiant;
    private final String motDePasse;
    private final String nomComplet;
    private final Integer idDepartement;
    private final String libelleDepartement;
    private final String libellePoste;
    private final Set<String> roles;

    public UtilisateurConnecte(Type type, Integer id, String identifiant, String motDePasse, String nomComplet,
                               Integer idDepartement, String libelleDepartement, String libellePoste,
                               Set<String> roles) {
        this.type = type;
        this.id = id;
        this.identifiant = identifiant;
        this.motDePasse = motDePasse;
        this.nomComplet = nomComplet;
        this.idDepartement = idDepartement;
        this.libelleDepartement = libelleDepartement;
        this.libellePoste = libellePoste;
        this.roles = Set.copyOf(roles);
    }

    public boolean estCandidat() {
        return type == Type.CANDIDAT;
    }

    public boolean estEmploye() {
        return type == Type.EMPLOYE;
    }

    public boolean aRole(String role) {
        return roles.contains(role);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();
    }

    @Override
    public String getPassword() {
        return motDePasse;
    }

    @Override
    public String getUsername() {
        return identifiant;
    }

    /** Libellés lisibles des rôles, pour l'en-tête. */
    public List<String> getRolesAffiches() {
        return roles.stream().filter(r -> !r.equals(Roles.EMPLOYE)).sorted().toList();
    }
}
