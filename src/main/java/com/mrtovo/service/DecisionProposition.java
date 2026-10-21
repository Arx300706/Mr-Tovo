package com.mrtovo.service;

import com.mrtovo.entity.reference.StatutProposition;

import java.util.List;
import java.util.Objects;

/**
 * Règle de l'étape 13 : chaque champ de la proposition est coché Oui ou Non par le candidat.
 * Tout Oui -> valide ; tout Non -> refuse ; sinon -> en attente (négociation avec la RH).
 */
public final class DecisionProposition {

    private DecisionProposition() {
    }

    public static String decider(List<Boolean> reponses) {
        if (reponses.isEmpty() || reponses.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Chaque champ doit recevoir une réponse Oui ou Non");
        }
        if (reponses.stream().allMatch(Boolean::booleanValue)) {
            return StatutProposition.VALIDE;
        }
        if (reponses.stream().noneMatch(Boolean::booleanValue)) {
            return StatutProposition.REFUSE;
        }
        return StatutProposition.EN_ATTENTE;
    }
}
