package com.mrtovo.recrutement.demo;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mrtovo.commun.config.ComptesDemo;
import com.mrtovo.commun.entity.Compte;
import com.mrtovo.commun.entity.Role;
import com.mrtovo.commun.repository.CompteRepository;
import com.mrtovo.embauche.repository.TypeContratRepository;
import com.mrtovo.recrutement.entity.Candidat;
import com.mrtovo.recrutement.entity.Candidature;
import com.mrtovo.recrutement.entity.Entretien;
import com.mrtovo.recrutement.entity.EvaluationCandidat;
import com.mrtovo.recrutement.entity.Offre;
import com.mrtovo.recrutement.entity.StatutCandidature;
import com.mrtovo.recrutement.entity.StatutOffre;
import com.mrtovo.recrutement.repository.CandidatRepository;
import com.mrtovo.recrutement.repository.CandidatureRepository;
import com.mrtovo.recrutement.repository.EntretienRepository;
import com.mrtovo.recrutement.repository.EvaluationCandidatRepository;
import com.mrtovo.recrutement.repository.OffreRepository;
import com.mrtovo.recrutement.repository.StatutCandidatureRepository;
import com.mrtovo.recrutement.repository.StatutOffreRepository;

/**
 * Données de démo du bloc 2 (BK2-02), créées au démarrage si aucune offre n'existe.
 * 2 offres publiées + 1 brouillon + 1 clôturée + 1 expirée, 10 candidats avec compte (mot de passe demo1234),
 * candidat1 a postulé à deux offres, candidatures à différents statuts, entretiens et évaluations.
 * Activé par {@code mrtovo.demo.recrutement=true}. Passe après {@link ComptesDemo} (évaluateurs rh et manager1).
 */
@Component
@Order(2)
@ConditionalOnProperty(name = "mrtovo.demo.recrutement", havingValue = "true")
public class DonneesDemoRecrutement implements ApplicationRunner {

	private static final Logger LOG = LoggerFactory.getLogger(DonneesDemoRecrutement.class);

	private final OffreRepository offreRepository;
	private final StatutOffreRepository statutOffreRepository;
	private final TypeContratRepository typeContratRepository;
	private final CandidatRepository candidatRepository;
	private final CandidatureRepository candidatureRepository;
	private final StatutCandidatureRepository statutCandidatureRepository;
	private final EntretienRepository entretienRepository;
	private final EvaluationCandidatRepository evaluationCandidatRepository;
	private final CompteRepository compteRepository;
	private final PasswordEncoder passwordEncoder;
	private final Clock clock;

	public DonneesDemoRecrutement(OffreRepository offreRepository, StatutOffreRepository statutOffreRepository,
			TypeContratRepository typeContratRepository, CandidatRepository candidatRepository,
			CandidatureRepository candidatureRepository, StatutCandidatureRepository statutCandidatureRepository,
			EntretienRepository entretienRepository, EvaluationCandidatRepository evaluationCandidatRepository,
			CompteRepository compteRepository, PasswordEncoder passwordEncoder, Clock clock) {
		this.offreRepository = offreRepository;
		this.statutOffreRepository = statutOffreRepository;
		this.typeContratRepository = typeContratRepository;
		this.candidatRepository = candidatRepository;
		this.candidatureRepository = candidatureRepository;
		this.statutCandidatureRepository = statutCandidatureRepository;
		this.entretienRepository = entretienRepository;
		this.evaluationCandidatRepository = evaluationCandidatRepository;
		this.compteRepository = compteRepository;
		this.passwordEncoder = passwordEncoder;
		this.clock = clock;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (offreRepository.count() > 0) {
			return;
		}
		LocalDate aujourdhui = LocalDate.now(clock);
		LocalDateTime maintenant = LocalDateTime.now(clock).withSecond(0).withNano(0);

		// TODO BK1 : relier les offres à des besoins « recrutement ouvert » quand ils existeront
		Offre developpeur = offre("Développeur Java / Spring Boot", "CDI", "Antananarivo",
			"""
				Au sein de l'équipe informatique, vous développez et maintenez nos applications internes de gestion.
				Vous participez à la conception, aux revues de code et aux mises en production.""",
			"""
				Bac+3 minimum en informatique, 2 ans d'expérience en Java.
				Connaissance de Spring Boot, SQL et Git. Esprit d'équipe et rigueur.""",
			StatutOffre.PUBLIEE, aujourdhui.minusDays(10), aujourdhui.plusDays(30));
		Offre comptable = offre("Assistant(e) comptable", "CDD", "Antananarivo",
			"""
				Vous assistez le responsable financier : saisie des pièces, rapprochements bancaires,
				préparation des déclarations et suivi des fournisseurs.""",
			"""
				Bac+2 en comptabilité ou gestion. Maîtrise d'Excel. Une première expérience est un plus.""",
			StatutOffre.PUBLIEE, aujourdhui.minusDays(5), aujourdhui.plusDays(20));
		offre("Chargé(e) de recrutement", "CE", "Antananarivo",
			"Vous gérez les recrutements de bout en bout, de la publication des offres à l'intégration.",
			"Bac+3 en ressources humaines.", StatutOffre.BROUILLON, null, aujourdhui.plusDays(45));
		Offre technicien = offre("Technicien(ne) réseau", "CDD", "Toamasina",
			"Installation et maintenance du réseau de l'agence de Toamasina.",
			"BTS réseaux, permis B.", StatutOffre.CLOTUREE, aujourdhui.minusDays(60), aujourdhui.minusDays(15));
		offre("Commercial(e) terrain", "CDD", "Fianarantsoa",
			"Prospection et suivi des clients de la région Haute Matsiatra.",
			"Expérience commerciale de 1 an.", StatutOffre.PUBLIEE, aujourdhui.minusDays(40), aujourdhui.minusDays(1));

		Candidat c1 = candidatRepository.findByEmailIgnoreCase("candidat1@mrtovo.local")
			.orElseGet(() -> candidat("Rakoto", "Candidat", "candidat1@mrtovo.local", null));
		Candidat c2 = candidat("Rasoanaivo", "Hery", "candidat2@mrtovo.local", "034 11 222 33");
		Candidat c3 = candidat("Randriamampionona", "Tiana", "candidat3@mrtovo.local", "032 44 555 66");
		Candidat c4 = candidat("Rabearivelo", "Mamy", "candidat4@mrtovo.local", "033 77 888 99");
		Candidat c5 = candidat("Andrianarisoa", "Fanja", "candidat5@mrtovo.local", null);
		Candidat c6 = candidat("Razafindrakoto", "Lova", "candidat6@mrtovo.local", "034 12 345 67");
		Candidat c7 = candidat("Rakotomalala", "Nirina", "candidat7@mrtovo.local", null);
		Candidat c8 = candidat("Ravelojaona", "Sitraka", "candidat8@mrtovo.local", "032 98 765 43");
		Candidat c9 = candidat("Rajaonarison", "Voahangy", "candidat9@mrtovo.local", null);
		Candidat c10 = candidat("Randrianasolo", "Toky", "candidat10@mrtovo.local", "033 22 333 44");

		Compte rh = compteRepository.findByIdentifiantIgnoreCase("rh").orElse(null);
		Compte manager = compteRepository.findByIdentifiantIgnoreCase("manager1").orElse(null);

		// Offre « Développeur » : un candidat à chaque étape
		candidature(c1, developpeur, StatutCandidature.RECUE, maintenant.minusDays(2), null);
		candidature(c2, developpeur, StatutCandidature.PRESELECTIONNEE, maintenant.minusDays(8), null);
		Candidature tiana = candidature(c3, developpeur, StatutCandidature.EN_ENTRETIEN, maintenant.minusDays(7), null);
		Entretien premier = entretien(tiana, Entretien.Type.TELEPHONIQUE, maintenant.minusDays(3).withHour(10), null, rh,
			Entretien.Statut.REALISE);
		evaluation(premier, 14, EvaluationCandidat.Avis.FAVORABLE, "Bonne communication, bases Spring solides.", rh);
		entretien(tiana, Entretien.Type.PHYSIQUE, maintenant.plusDays(2).withHour(14), "Salle de réunion 2", manager,
			Entretien.Statut.PLANIFIE);
		Candidature mamy = candidature(c4, developpeur, StatutCandidature.RETENUE, maintenant.minusDays(9), null);
		Entretien technique = entretien(mamy, Entretien.Type.PHYSIQUE, maintenant.minusDays(4).withHour(9),
			"Salle de réunion 1", manager, Entretien.Statut.REALISE);
		evaluation(technique, 17, EvaluationCandidat.Avis.FAVORABLE, "Excellent test technique.", manager);
		Entretien rhMamy = entretien(mamy, Entretien.Type.VISIO, maintenant.minusDays(3).withHour(15),
			"https://meet.exemple.mg/mrtovo-rh", rh, Entretien.Statut.REALISE);
		evaluation(rhMamy, 16, EvaluationCandidat.Avis.FAVORABLE, "Motivé, disponible dans un mois.", rh);
		candidature(c5, developpeur, StatutCandidature.REJETEE, maintenant.minusDays(9),
			"Profil trop junior pour le poste");

		// Offre « Comptable » : candidat1 a aussi postulé ici (deux offres)
		Candidature rakoto = candidature(c1, comptable, StatutCandidature.EN_ENTRETIEN, maintenant.minusDays(4), null);
		entretien(rakoto, Entretien.Type.PHYSIQUE, maintenant.plusDays(1).withHour(11), "Bureau des RH", rh,
			Entretien.Statut.PLANIFIE);
		candidature(c6, comptable, StatutCandidature.RECUE, maintenant.minusDays(1), null);
		candidature(c7, comptable, StatutCandidature.PRESELECTIONNEE, maintenant.minusDays(3), null);
		candidature(c8, comptable, StatutCandidature.REJETEE, maintenant.minusDays(4),
			"Pas de formation en comptabilité");
		candidature(c9, comptable, StatutCandidature.RETIREE, maintenant.minusDays(4), null);

		// Offre clôturée
		candidature(c10, technicien, StatutCandidature.REJETEE, maintenant.minusDays(50), "Poste pourvu en interne");

		LOG.info("Données de démo du recrutement créées : {} offres, {} candidatures",
			offreRepository.count(), candidatureRepository.count());
	}

	private Offre offre(String titre, String typeContrat, String lieu, String description, String profil,
			String statut, LocalDate datePublication, LocalDate dateLimite) {
		Offre offre = new Offre();
		offre.setTitre(titre);
		offre.setTypeContrat(typeContratRepository.findByLibelle(typeContrat).orElse(null));
		offre.setLieu(lieu);
		offre.setDescription(description.strip());
		offre.setProfil(profil.strip());
		offre.setStatutOffre(statutOffreRepository.findByLibelle(statut).orElseThrow());
		offre.setDatePublication(datePublication);
		offre.setDateLimite(dateLimite);
		return offreRepository.save(offre);
	}

	private Candidat candidat(String nom, String prenom, String email, String telephone) {
		Candidat candidat = new Candidat();
		candidat.setNom(nom);
		candidat.setPrenom(prenom);
		candidat.setEmail(email);
		candidat.setTelephone(telephone);
		candidat = candidatRepository.save(candidat);

		if (!compteRepository.existsByIdentifiantIgnoreCase(email)) {
			Compte compte = new Compte();
			compte.setIdentifiant(email);
			compte.setMotDePasse(passwordEncoder.encode(ComptesDemo.MOT_DE_PASSE));
			compte.setRole(Role.CANDIDAT);
			compte.setIdCandidat(candidat.getIdCandidat());
			compteRepository.save(compte);
		}
		return candidat;
	}

	private Candidature candidature(Candidat candidat, Offre offre, String statut, LocalDateTime date, String motifRejet) {
		Candidature candidature = new Candidature();
		candidature.setCandidat(candidat);
		candidature.setOffre(offre);
		candidature.setDateCandidature(date);
		candidature.setLettre("Madame, Monsieur, je souhaite vous proposer ma candidature au poste de "
			+ offre.getTitre() + ". Mon expérience correspond au profil recherché.");
		candidature.setStatutCandidature(statutCandidatureRepository.findByLibelle(statut).orElseThrow());
		candidature.setMotifRejet(motifRejet);
		return candidatureRepository.save(candidature);
	}

	private Entretien entretien(Candidature candidature, Entretien.Type type, LocalDateTime date, String lieu,
			Compte evaluateur, Entretien.Statut statut) {
		Entretien entretien = new Entretien();
		entretien.setCandidature(candidature);
		entretien.setType(type);
		entretien.setDateEntretien(date.withMinute(0));
		entretien.setLieu(lieu);
		entretien.setEvaluateur(evaluateur);
		entretien.setStatut(statut);
		return entretienRepository.save(entretien);
	}

	private void evaluation(Entretien entretien, int note, EvaluationCandidat.Avis avis, String commentaire,
			Compte evaluateur) {
		if (evaluateur == null) {
			return;
		}
		EvaluationCandidat evaluation = new EvaluationCandidat();
		evaluation.setEntretien(entretien);
		evaluation.setNote(note);
		evaluation.setAvis(avis);
		evaluation.setCommentaire(commentaire);
		evaluation.setEvaluateur(evaluateur);
		evaluationCandidatRepository.save(evaluation);
	}

}
