package com.mrtovo.config;

import com.mrtovo.security.UtilisateurConnecte;
import com.mrtovo.service.BadgeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Map;

/** Données communes à toutes les pages : utilisateur connecté, pastilles du menu, page courante. */
@ControllerAdvice
@RequiredArgsConstructor
public class ModeleGlobalAdvice {

    private final BadgeService badgeService;

    @ModelAttribute("utilisateur")
    public UtilisateurConnecte utilisateur(@AuthenticationPrincipal UtilisateurConnecte u) {
        return u;
    }

    @ModelAttribute("badges")
    public Map<String, Long> badges(@AuthenticationPrincipal UtilisateurConnecte u) {
        return badgeService.calculer(u);
    }

    @ModelAttribute("pageCourante")
    public String pageCourante(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
