package com.mrtovo.recrutement.service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.mrtovo.commun.entity.Role;
import com.mrtovo.commun.securite.UtilisateurConnecte;
import com.mrtovo.commun.service.EmailSimuleService;
import com.mrtovo.commun.service.JournalAuditService;
import com.mrtovo.recrutement.dto.CandidatureFormulaire;
import com.mrtovo.recrutement.entity.Candidat;
import com.mrtovo.recrutement.entity.Candidature;
import com.mrtovo.recrutement.entity.Entretien;
import com.mrtovo.recrutement.entity.EvaluationCandidat;
import com.mrtovo.recrutement.entity.Offre;
import com.mrtovo.recrutement.entity.StatutCandidature;
import com.mrtovo.recrutement.repository.CandidatRepository;
import com.mrtovo.recrutement.repository.CandidatureRepository;
import com.mrtovo.recrutement.repository.EntretienRepository;
import com.mrtovo.recrutement.repository.EvaluationCandidatRepository;
import com.mrtovo.recrutement.repository.StatutCandidatureRepository;

/**
 * Candidatures : dépôt depuis la page carrières, espace candidat, consultation par les RH.
 * Règles : un compte = un candidat, une seule candidature par offre, offre publiée et non expirée.
 */
@Service
public class CandidatureService {

	public static final long TAILLE_MAX_CV = 5L * 1024 * 1024;
	public static final Set<String> EXTENSIONS_CV = Set.of("pdf", "doc", "docx");

	private static final String OBJET = "Candidature";

	private final CandidatureRepository candidatureRepository;
	private final CandidatRepository candidatRepository;
	private final StatutCandidatureRepository statutCandidatureRepository;
	private final EntretienRepository entretienRepository;
	private final EvaluationCandidatRepository evaluationCandidatRepository;
	private final OffreService offreService;
	private final JournalAuditService journalAuditService;
	private final EmailSimuleService emailSimuleService;

	public CandidatureService(CandidatureRepository candidatureRepository, CandidatRepository candidatRepository,
			StatutCandidatureRepository statutCandidatureRepository, EntretienRepository entretienRepository,
			EvaluationCandidatRepository evaluationCandidatRepository, OffreService offreService,
			JournalAuditService journalAuditService, EmailSimuleService emailSimuleService) {
		this.candidatureRepository = candidatureRepository;
		this.candidatRepository = candidatRepository;
		this.statutCandidatureRepository = statutCandidatureRepository;
		this.entretienRepository = entretienRepository;
		this.evaluationCandidatRepository = evaluationCandidatRepository;
		this.offreService = offreService;
		this.journalAuditService = journalAuditService;
		this.emailSimuleService = emailSimuleService;
	}

	// ----- Côté candidat -----

	/**
	 * Dépose la candidature du candidat connecté à une offre visible.
	 * @throws IllegalArgumentException si le CV est invalide ou si le candidat a déjà postulé
	 */
	@Transactional
	public Candidature postuler(Integer idOffre, UtilisateurConnecte utilisateur, CandidatureFormulaire formulaire) {
		Candidat candidat = candidatConnecte(utilisateur);
		Offre offre = offreService.trouverVisible(idOffre);
		if (aDejaPostule(idOffre, utilisateur)) {
			throw new IllegalArgumentException("Vous avez déjà postulé à cette offre");
		}
		String erreurCv = erreurCv(formulaire.getCv());
		if (erreurCv != null) {
			throw new IllegalArgumentException(erreurCv);
		}

		Candidature candidature = new Candidature();
		candidature.setCandidat(candidat);
		candidature.setOffre(offre);
		candidature.setLettre(formulaire.getLettre());
		candidature.setStatutCandidature(statut(StatutCandidature.RECUE));
		// TODO BK0-05 : enregistrer le CV dans la GED et remplir idDocumentCv
		candidature = candidatureRepository.save(candidature);

		journalAuditService.enregistrer("POSTULER", OBJET, candidature.getIdCandidature(),
			candidat.getEmail() + " → " + offre.getTitre());
		emailSimuleService.envoyer(candidat.getEmail(), "Candidature reçue : " + offre.getTitre(),
			"Bonjour " + candidat.getPrenom() + ", nous avons bien reçu votre candidature. "
				+ "Suivez son avancement depuis votre espace candidat.");
		emailSimuleService.envoyerAuRole(Role.RH, "Nouvelle candidature : " + offre.getTitre(),
			candidat.getNomComplet() + " a postulé.");
		return candidature;
	}

	@Transactional(readOnly = true)
	public boolean aDejaPostule(Integer idOffre, UtilisateurConnecte utilisateur) {
		return utilisateur != null && utilisateur.getIdCandidat() != null
			&& candidatureRepository.existsByCandidatIdCandidatAndOffreIdOffre(utilisateur.getIdCandidat(), idOffre);
	}

	@Transactional(readOnly = true)
	public List<Candidature> mesCandidatures(UtilisateurConnecte utilisateur) {
		return candidatureRepository.findByCandidatIdCandidatOrderByDateCandidatureDesc(candidatConnecte(utilisateur).getIdCandidat());
	}

	/** Message d'erreur si le CV est absent, trop gros ou dans un format refusé ; null s'il est valide. */
	public static String erreurCv(MultipartFile cv) {
		if (cv == null || cv.isEmpty()) {
			return "Le CV est obligatoire";
		}
		if (cv.getSize() > TAILLE_MAX_CV) {
			return "Le CV dépasse 5 Mo";
		}
		String nom = cv.getOriginalFilename() == null ? "" : cv.getOriginalFilename().toLowerCase(Locale.ROOT);
		String extension = nom.contains(".") ? nom.substring(nom.lastIndexOf('.') + 1) : "";
		if (!EXTENSIONS_CV.contains(extension)) {
			return "Le CV doit être un fichier PDF ou Word (.pdf, .doc, .docx)";
		}
		return null;
	}

	// ----- Côté RH / manager -----

	@Transactional(readOnly = true)
	public List<Candidature> deLOffre(Integer idOffre, String libelleStatut) {
		if (libelleStatut == null || libelleStatut.isBlank()) {
			return candidatureRepository.findByOffreIdOffreOrderByDateCandidatureDesc(idOffre);
		}
		return candidatureRepository.findByOffreIdOffreAndStatutCandidatureLibelleOrderByDateCandidatureDesc(idOffre,
			libelleStatut);
	}

	@Transactional(readOnly = true)
	public Candidature trouver(Integer idCandidature) {
		return candidatureRepository.findById(idCandidature)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidature introuvable"));
	}

	@Transactional(readOnly = true)
	public List<Entretien> entretiens(Integer idCandidature) {
		return entretienRepository.findByCandidatureIdCandidatureOrderByDateEntretienAsc(idCandidature);
	}

	@Transactional(readOnly = true)
	public List<EvaluationCandidat> evaluations(Integer idCandidature) {
		return evaluationCandidatRepository.findByEntretienCandidatureIdCandidatureOrderByDateEvaluationAsc(idCandidature);
	}

	@Transactional(readOnly = true)
	public Double noteMoyenne(Integer idCandidature) {
		return evaluationCandidatRepository.noteMoyenne(idCandidature);
	}

	@Transactional(readOnly = true)
	public List<StatutCandidature> statuts() {
		return statutCandidatureRepository.findAllByOrderByIdStatutCandidatureAsc();
	}

	private Candidat candidatConnecte(UtilisateurConnecte utilisateur) {
		if (utilisateur == null || utilisateur.getIdCandidat() == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Réservé aux comptes candidat");
		}
		return candidatRepository.findById(utilisateur.getIdCandidat())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Candidat introuvable"));
	}

	private StatutCandidature statut(String libelle) {
		return statutCandidatureRepository.findByLibelle(libelle)
			.orElseThrow(() -> new IllegalStateException("Statut de candidature absent de la base : " + libelle));
	}

}
