package com.mrtovo.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Accès générique aux tables de référence (statuts et types), par libellé. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReferenceService {

    private final EntityManager entityManager;

    public <T> T parLibelle(Class<T> type, String libelle) {
        List<T> resultat = entityManager
                .createQuery("select r from " + type.getSimpleName() + " r where r.libelle = :libelle", type)
                .setParameter("libelle", libelle)
                .getResultList();
        if (resultat.isEmpty()) {
            throw new IllegalStateException("Valeur de référence absente en base : " + type.getSimpleName() + " « " + libelle + " »");
        }
        return resultat.getFirst();
    }

    public <T> T parId(Class<T> type, Integer id) {
        return id == null ? null : entityManager.find(type, id);
    }

    public <T> List<T> tous(Class<T> type) {
        return entityManager.createQuery("select r from " + type.getSimpleName() + " r order by r.id", type)
                .getResultList();
    }
}
