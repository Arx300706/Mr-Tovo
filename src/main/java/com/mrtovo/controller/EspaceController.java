package com.mrtovo.controller;

import com.mrtovo.repository.EmployeRepository;
import com.mrtovo.repository.HistoriqueEmployeRepository;
import com.mrtovo.security.UtilisateurConnecte;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Tableau de bord de l'employé connecté : raccourcis selon ses rôles. */
@Controller
@RequiredArgsConstructor
public class EspaceController {

    private final EmployeRepository employeRepository;
    private final HistoriqueEmployeRepository historiqueEmployeRepository;

    @GetMapping("/espace")
    public String accueil(@AuthenticationPrincipal UtilisateurConnecte u, Model model) {
        model.addAttribute("employe", employeRepository.findById(u.getId()).orElseThrow());
        model.addAttribute("historique", historiqueEmployeRepository.findByEmployeIdOrderByDateDesc(u.getId()));
        return "espace/accueil";
    }
}
