package com.mrtovo.recrutement.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mrtovo.commun.securite.UtilisateurConnecte;
import com.mrtovo.commun.service.JournalAuditService;
import com.mrtovo.embauche.entity.TypeContrat;
import com.mrtovo.embauche.repository.TypeContratRepository;
import com.mrtovo.recrutement.dto.OffreFormulaire;
import com.mrtovo.recrutement.entity.Offre;
import com.mrtovo.recrutement.entity.StatutOffre;
import com.mrtovo.recrutement.repository.CandidatureRepository;
import com.mrtovo.recrutement.repository.OffreRepository;
import com.mrtovo.recrutement.repository.StatutOffreRepository;

/**
 * Offres d'emploi : lecture (RH et public), brouillon, publication, clôture, annulation.
 * Les erreurs métier sont des {@link IllegalArgumentException} avec un message à afficher.
 */
@Service
public class OffreService {

	private static final String OBJET = "Offre";

	private final OffreRepository offreRepository;
	private final StatutOffreRepository statutOffreRepository;
	private final TypeContratRepository typeContratRepository;
	private final CandidatureRepository candidatureRepository;
	private final OffreWorkflow offreWorkflow;
	private final JournalAuditService journalAuditService;
	private final Clock clock;

	public OffreService(OffreRepository offreRepository, StatutOffreRepository statutOffreRepository,
			TypeContratRepository typeContratRepository, CandidatureRepository candidatureRepository,
			OffreWorkflow offreWorkflow, JournalAuditService journalAuditService, Clock clock) {
		this.offreRepository = offreRepository;
		this.statutOffreRepository = statutOffreRepository;
		this.typeContratRepository = typeContratRepository;
		this.candidatureRepository = candidatureRepository;
		this.offreWorkflow = offreWorkflow;
		this.journalAuditService = journalAuditService;
		this.clock = clock;
	}

	// ----- Côté RH -----

	@Transactional(readOnly = true)
	public List<Offre> toutes(String libelleStatut) {
		if (libelleStatut == null || libelleStatut.isBlank()) {
			return offreRepository.findAllByOrderByDateCreationDescIdOffreDesc();
		}
		return offreRepository.findByStatutOffreLibelleOrderByDateCreationDescIdOffreDesc(libelleStatut);
	}

	@Transactional(readOnly = true)
	public Offre trouver(Integer idOffre) {
		return offreRepository.findById(idOffre)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offre introuvable"));
	}

	/** Nombre de candidatures par offre (les offres sans candidature sont absentes de la map). */
	@Transactional(readOnly = true)
	public Map<Integer, Long> nombresCandidatures() {
		Map<Integer, Long> nombres = new HashMap<>();
		for (Object[] ligne : candidatureRepository.compterParOffre()) {
			nombres.put((Integer) ligne[0], (Long) ligne[1]);
		}
		return nombres;
	}

	@Transactional(readOnly = true)
	public long nombreCandidatures(Integer idOffre) {
		return candidatureRepository.countByOffreIdOffre(idOffre);
	}

	public List<String> actionsPossibles(Offre offre, UtilisateurConnecte utilisateur) {
		return offreWorkflow.actionsPossibles(offre, utilisateur);
	}

	/**
	 * Crée une offre en brouillon. Appelée par les RH (/offres/nouveau) et, plus tard,
	 * par BK1 à l'ouverture du recrutement d'un besoin validé.
	 */
	@Transactional
	public Offre creerBrouillon(OffreFormulaire formulaire, Integer idBesoin) {
		Offre offre = new Offre();
		offre.setIdBesoin(idBesoin);
		offre.setStatutOffre(statut(StatutOffre.BROUILLON));
		remplir(offre, formulaire);
		offre = offreRepository.save(offre);
		journalAuditService.enregistrer("CREER", OBJET, offre.getIdOffre(), offre.getTitre());
		return offre;
	}

	@Transactional
	public Offre modifierBrouillon(Integer idOffre, OffreFormulaire formulaire, UtilisateurConnecte utilisateur) {
		Offre offre = trouver(idOffre);
		offreWorkflow.verifier(offre, OffreWorkflow.MODIFIER, utilisateur, aujourdhui());
		remplir(offre, formulaire);
		journalAuditService.enregistrer("MODIFIER", OBJET, idOffre, offre.getTitre());
		return offre;
	}

	/** Publier, clôturer ou annuler (codes de {@link OffreWorkflow}). */
	@Transactional
	public Offre changerStatut(Integer idOffre, String action, UtilisateurConnecte utilisateur) {
		Offre offre = trouver(idOffre);
		String ancien = offre.getStatutOffre().getLibelle();
		String vers = offreWorkflow.verifier(offre, action, utilisateur, aujourdhui());
		offre.setStatutOffre(statut(vers));
		if (OffreWorkflow.PUBLIER.equals(action)) {
			offre.setDatePublication(aujourdhui());
		}
		journalAuditService.enregistrer(action, OBJET, idOffre, ancien + " → " + vers);
		return offre;
	}

	// ----- Côté public -----

	/** Offres publiées et non expirées, avec filtres facultatifs (mot-clé, type de contrat). */
	@Transactional(readOnly = true)
	public List<Offre> visibles(String motCle, Integer idTypeContrat) {
		String filtre = motCle == null || motCle.isBlank() ? null : "%" + motCle.trim().toLowerCase(Locale.ROOT) + "%";
		return offreRepository.rechercherVisibles(StatutOffre.PUBLIEE, aujourdhui(), idTypeContrat, filtre);
	}

	/** Offre visible au public, sinon 404 (brouillon, clôturée, expirée…). */
	@Transactional(readOnly = true)
	public Offre trouverVisible(Integer idOffre) {
		Offre offre = offreRepository.findById(idOffre)
			.filter(o -> o.estVisiblePubliquement(aujourdhui()))
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offre introuvable"));
		return offre;
	}

	@Transactional(readOnly = true)
	public List<TypeContrat> typesContrat() {
		return typeContratRepository.findAllByOrderByIdTypeContratAsc();
	}

	@Transactional(readOnly = true)
	public List<StatutOffre> statuts() {
		return statutOffreRepository.findAllByOrderByIdStatutOffreAsc();
	}

	public LocalDate aujourdhui() {
		return LocalDate.now(clock);
	}

	private void remplir(Offre offre, OffreFormulaire formulaire) {
		offre.setTitre(formulaire.getTitre().trim());
		offre.setDescription(formulaire.getDescription());
		offre.setProfil(formulaire.getProfil());
		offre.setLieu(formulaire.getLieu());
		offre.setDateLimite(formulaire.getDateLimite());
		offre.setTypeContrat(formulaire.getIdTypeContrat() == null ? null
			: typeContratRepository.findById(formulaire.getIdTypeContrat())
				.orElseThrow(() -> new IllegalArgumentException("Type de contrat inconnu")));
	}

	private StatutOffre statut(String libelle) {
		return statutOffreRepository.findByLibelle(libelle)
			.orElseThrow(() -> new IllegalStateException("Statut d'offre absent de la base : " + libelle));
	}

}
