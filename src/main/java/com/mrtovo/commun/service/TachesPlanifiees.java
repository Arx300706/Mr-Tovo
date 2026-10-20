package com.mrtovo.commun.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mrtovo.commun.entity.Role;

/**
 * Tâche quotidienne (BK0-11) : clôture des offres expirées, propositions expirées, alertes fin de contrat.
 * Écrit en SQL sur les tables des autres pistes tant que leurs entités et le moteur de statuts ne sont pas là.
 * Heure réglable par {@code mrtovo.taches.cron} ; désactivable par {@code mrtovo.taches.actives=false}.
 */
@Component
@EnableScheduling
@ConditionalOnProperty(name = "mrtovo.taches.actives", havingValue = "true", matchIfMissing = true)
public class TachesPlanifiees {

	private static final Logger LOG = LoggerFactory.getLogger(TachesPlanifiees.class);
	private static final int JOURS_ALERTE_FIN_CONTRAT = 30;

	private final JdbcTemplate jdbcTemplate;
	private final JournalAuditService journalAuditService;
	private final EmailSimuleService emailSimuleService;

	public TachesPlanifiees(JdbcTemplate jdbcTemplate, JournalAuditService journalAuditService,
			EmailSimuleService emailSimuleService) {
		this.jdbcTemplate = jdbcTemplate;
		this.journalAuditService = journalAuditService;
		this.emailSimuleService = emailSimuleService;
	}

	@Scheduled(cron = "${mrtovo.taches.cron:0 0 6 * * *}")
	public void executer() {
		cloturerOffresExpirees();
		expirerPropositions();
		alerterFinsDeContrat();
	}

	/** Offre publiée dont la date de fin est passée → statut « fin ». */
	@Transactional
	public int cloturerOffresExpirees() {
		int nombre = jdbcTemplate.update("""
			UPDATE offre
			SET id_statut_offre = (SELECT id_statut_offre FROM statut_offre WHERE libelle = 'fin')
			WHERE date_fin < CURDATE()
			  AND id_statut_offre = (SELECT id_statut_offre FROM statut_offre WHERE libelle = 'publie')""");
		tracer("CLOTURER_EXPIREES", "Offre", nombre);
		return nombre;
	}

	/** Proposition « en attente » dont la date limite est passée → « refuse » (pas encore de statut « expirée »). */
	@Transactional
	public int expirerPropositions() {
		int nombre = jdbcTemplate.update("""
			UPDATE proposition
			SET id_statut_proposition = (SELECT id_statut_proposition FROM statut_proposition WHERE libelle = 'refuse')
			WHERE date_limite_validation < CURDATE()
			  AND id_statut_proposition = (SELECT id_statut_proposition FROM statut_proposition WHERE libelle = 'en attente')""");
		tracer("EXPIRER", "Proposition", nombre);
		return nombre;
	}

	/** Contrats en cours qui finissent dans les 30 jours → e-mail aux RH. */
	public int alerterFinsDeContrat() {
		List<Map<String, Object>> contrats = jdbcTemplate.queryForList("""
			SELECT c.id_contrat, e.matricule, e.prenom, e.nom, p.date_fin
			FROM contrat c
			JOIN employe e ON e.id_employe = c.id_employe
			JOIN proposition p ON p.id_proposition = c.id_proposition
			JOIN statut_contrat s ON s.id_statut_contrat = c.id_statut_contrat
			WHERE s.libelle NOT IN ('annule', 'rompu')
			  AND p.date_fin BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY)
			ORDER BY p.date_fin""", JOURS_ALERTE_FIN_CONTRAT);
		if (!contrats.isEmpty()) {
			StringBuilder corps = new StringBuilder("Contrats qui finissent dans les " + JOURS_ALERTE_FIN_CONTRAT + " jours :\n");
			for (Map<String, Object> c : contrats) {
				corps.append("- ").append(c.get("matricule")).append(' ').append(c.get("prenom")).append(' ')
					.append(c.get("nom")).append(" : fin le ").append(c.get("date_fin")).append('\n');
			}
			emailSimuleService.envoyerAuRole(Role.RH, "Alerte : contrats bientôt terminés", corps.toString());
		}
		LOG.info("Tâche quotidienne : {} contrat(s) finissent dans les {} jours", contrats.size(), JOURS_ALERTE_FIN_CONTRAT);
		return contrats.size();
	}

	private void tracer(String action, String objet, int nombre) {
		LOG.info("Tâche quotidienne : {} {} → {}", nombre, objet, action);
		if (nombre > 0) {
			journalAuditService.enregistrer(action, objet, null, nombre + " ligne(s) modifiée(s) automatiquement");
		}
	}

}
