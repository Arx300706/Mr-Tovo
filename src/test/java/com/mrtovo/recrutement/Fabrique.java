package com.mrtovo.recrutement;

import java.time.LocalDate;

import org.springframework.test.util.ReflectionTestUtils;

import com.mrtovo.commun.entity.Compte;
import com.mrtovo.commun.entity.Role;
import com.mrtovo.commun.securite.UtilisateurConnecte;
import com.mrtovo.embauche.entity.TypeContrat;
import com.mrtovo.recrutement.entity.Offre;
import com.mrtovo.recrutement.entity.StatutCandidature;
import com.mrtovo.recrutement.entity.StatutOffre;

/** Objets de test du bloc 2 (les entités de référence n'ont pas de setters). */
public final class Fabrique {

	private Fabrique() {
	}

	public static StatutOffre statutOffre(String libelle) {
		StatutOffre statut = new StatutOffre();
		ReflectionTestUtils.setField(statut, "libelle", libelle);
		return statut;
	}

	public static StatutCandidature statutCandidature(String libelle) {
		StatutCandidature statut = new StatutCandidature();
		ReflectionTestUtils.setField(statut, "libelle", libelle);
		return statut;
	}

	public static TypeContrat typeContrat(String libelle) {
		TypeContrat type = new TypeContrat();
		ReflectionTestUtils.setField(type, "idTypeContrat", 1);
		ReflectionTestUtils.setField(type, "libelle", libelle);
		return type;
	}

	public static Offre offre(Integer id, String statut, LocalDate dateLimite) {
		Offre offre = new Offre();
		ReflectionTestUtils.setField(offre, "idOffre", id);
		offre.setTitre("Développeur Java");
		offre.setDescription("Description");
		offre.setTypeContrat(typeContrat("CDI"));
		offre.setStatutOffre(statutOffre(statut));
		offre.setDateLimite(dateLimite);
		return offre;
	}

	public static UtilisateurConnecte utilisateur(Role role, Integer idCandidat) {
		Compte compte = new Compte();
		ReflectionTestUtils.setField(compte, "idCompte", 1);
		compte.setIdentifiant(role == Role.CANDIDAT ? "c@x.mg" : role.name().toLowerCase());
		compte.setMotDePasse("x");
		compte.setRole(role);
		compte.setIdCandidat(idCandidat);
		return new UtilisateurConnecte(compte, compte.getIdentifiant());
	}

}
