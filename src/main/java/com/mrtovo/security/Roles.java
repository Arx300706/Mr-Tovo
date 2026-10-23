package com.mrtovo.security;

/** Rôles Spring Security (sans le préfixe ROLE_). */
public final class Roles {

    public static final String CANDIDAT = "CANDIDAT";
    public static final String EMPLOYE = "EMPLOYE";
    public static final String MANAGER = "MANAGER";
    public static final String RH = "RH";
    public static final String FINANCE = "FINANCE";
    public static final String DIRECTEUR = "DIRECTEUR";

    private Roles() {
    }
}
