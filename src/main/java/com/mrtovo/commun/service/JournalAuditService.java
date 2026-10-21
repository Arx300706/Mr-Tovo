package com.mrtovo.commun.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mrtovo.commun.entity.JournalAudit;
import com.mrtovo.commun.repository.JournalAuditRepository;
import com.mrtovo.commun.securite.UtilisateurConnecte;

/**
 * Écrit dans le journal d'audit. L'auteur est l'utilisateur connecté, ou « systeme ».
 * Exemple : {@code journalAuditService.enregistrer("DESACTIVER", "Compte", compte.getIdCompte(), "rh");}
 */
@Service
public class JournalAuditService {

	public static final String SYSTEME = "systeme";

	private final JournalAuditRepository journalAuditRepository;

	public JournalAuditService(JournalAuditRepository journalAuditRepository) {
		this.journalAuditRepository = journalAuditRepository;
	}

	@Transactional
	public void enregistrer(String action, String objet, Integer idObjet, String detail) {
		journalAuditRepository.save(new JournalAudit(auteurConnecte(), action, objet, idObjet, detail));
	}

	@Transactional(readOnly = true)
	public List<JournalAudit> derniers(int nombre) {
		return journalAuditRepository.findAllByOrderByDateActionDescIdJournalAuditDesc(PageRequest.of(0, nombre))
			.getContent();
	}

	private static String auteurConnecte() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof UtilisateurConnecte utilisateur) {
			return utilisateur.getIdentifiant();
		}
		return SYSTEME;
	}

}
