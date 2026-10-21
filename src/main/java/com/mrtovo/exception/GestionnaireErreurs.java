package com.mrtovo.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.net.URI;

/**
 * Une règle métier refusée sur un bouton d'action renvoie l'utilisateur sur la page d'où il vient,
 * avec le message d'erreur affiché en haut de page.
 */
@ControllerAdvice
public class GestionnaireErreurs {

    @ExceptionHandler(RegleMetierException.class)
    public String regleMetier(RegleMetierException e, HttpServletRequest request) {
        RequestContextUtils.getOutputFlashMap(request).put("erreur", e.getMessage());
        return "redirect:" + pageOrigine(request);
    }

    /** Chemin local de la page précédente (on ne redirige jamais vers un autre site). */
    private String pageOrigine(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null) {
            try {
                URI uri = URI.create(referer);
                if (uri.getPath() != null && uri.getPath().startsWith("/") && !uri.getPath().startsWith("//")) {
                    return uri.getPath() + (uri.getQuery() == null ? "" : "?" + uri.getQuery());
                }
            } catch (IllegalArgumentException ignored) {
                // Referer mal formé : on retombe sur le tableau de bord
            }
        }
        return "/tableau-de-bord";
    }
}
