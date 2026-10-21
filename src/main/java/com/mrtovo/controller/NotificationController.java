package com.mrtovo.controller;

import com.mrtovo.security.UtilisateurConnecte;
import com.mrtovo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Notifications de l'utilisateur connecté (candidat ou employé). */
@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public String liste(@AuthenticationPrincipal UtilisateurConnecte u, Model model) {
        model.addAttribute("notifications", notificationService.lister(u));
        return "commun/notifications";
    }

    @GetMapping("/{id}")
    public String ouvrir(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte u) {
        String lien = notificationService.ouvrir(id, u);
        return "redirect:" + (lien == null ? "/notifications" : lien);
    }

    @PostMapping("/tout-lu")
    public String toutLu(@AuthenticationPrincipal UtilisateurConnecte u) {
        notificationService.toutMarquerLu(u);
        return "redirect:/notifications";
    }
}
