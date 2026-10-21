package com.mrtovo.service;

import com.mrtovo.entity.Compte;
import com.mrtovo.entity.Employe;
import com.mrtovo.entity.Notification;
import com.mrtovo.exception.RessourceIntrouvableException;
import com.mrtovo.repository.NotificationRepository;
import com.mrtovo.security.UtilisateurConnecte;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    @Transactional
    public void notifierCompte(Compte compte, String message, String lien) {
        Notification n = new Notification();
        n.setCompte(compte);
        n.setMessage(message);
        n.setLien(lien);
        repository.save(n);
    }

    @Transactional
    public void notifierEmploye(Employe employe, String message, String lien) {
        if (employe == null) {
            return;
        }
        Notification n = new Notification();
        n.setEmploye(employe);
        n.setMessage(message);
        n.setLien(lien);
        repository.save(n);
    }

    @Transactional(readOnly = true)
    public List<Notification> lister(UtilisateurConnecte u) {
        return u.estCandidat()
                ? repository.findByCompteIdOrderByDateCreationDesc(u.getId())
                : repository.findByEmployeIdOrderByDateCreationDesc(u.getId());
    }

    @Transactional(readOnly = true)
    public long nonLues(UtilisateurConnecte u) {
        return u.estCandidat()
                ? repository.countByCompteIdAndLueFalse(u.getId())
                : repository.countByEmployeIdAndLueFalse(u.getId());
    }

    /** Marque la notification comme lue et renvoie son lien (ou null). */
    @Transactional
    public String ouvrir(Integer idNotification, UtilisateurConnecte u) {
        Notification n = repository.findById(idNotification)
                .filter(x -> appartientA(x, u))
                .orElseThrow(() -> new RessourceIntrouvableException("Notification introuvable"));
        n.setLue(true);
        return n.getLien();
    }

    @Transactional
    public void toutMarquerLu(UtilisateurConnecte u) {
        lister(u).forEach(n -> n.setLue(true));
    }

    private boolean appartientA(Notification n, UtilisateurConnecte u) {
        return u.estCandidat()
                ? n.getCompte() != null && Objects.equals(n.getCompte().getId(), u.getId())
                : n.getEmploye() != null && Objects.equals(n.getEmploye().getId(), u.getId());
    }
}
