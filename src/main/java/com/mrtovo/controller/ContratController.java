package com.mrtovo.controller;

import com.mrtovo.entity.Contrat;
import com.mrtovo.entity.Employe;
import com.mrtovo.entity.HistoriqueStatut;
import com.mrtovo.entity.reference.StatutContrat;
import com.mrtovo.security.UtilisateurConnecte;
import com.mrtovo.service.ContratService;
import com.mrtovo.service.PropositionService;
import com.mrtovo.service.ReferenceService;
import com.mrtovo.service.TraceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Contrats côté RH : génération, signature (validation) et annulation. */
@Controller
@RequestMapping("/rh/contrats")
@RequiredArgsConstructor
public class ContratController {

    private final ContratService contratService;
    private final PropositionService propositionService;
    private final TraceService traceService;
    private final ReferenceService references;

    @GetMapping
    public String liste(@RequestParam(required = false) String statut, Model model) {
        model.addAttribute("propositionsValides", propositionService.listerValidesSansContrat());
        model.addAttribute("contrats", contratService.listerParStatut(statut));
        model.addAttribute("statuts", references.tous(StatutContrat.class));
        model.addAttribute("statut", statut);
        return "contrat/liste";
    }

    @PostMapping("/generer")
    public String generer(@RequestParam Integer proposition, @AuthenticationPrincipal UtilisateurConnecte u,
                          RedirectAttributes redirect) {
        Contrat c = contratService.generer(proposition, u);
        redirect.addFlashAttribute("succes", "Contrat généré : il attend la signature du candidat.");
        return "redirect:/rh/contrats/" + c.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("contrat", contratService.trouver(id));
        model.addAttribute("historique", traceService.historique(HistoriqueStatut.CONTRAT, id));
        return "contrat/detail";
    }

    @PostMapping("/{id}/valider")
    public String valider(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte u, RedirectAttributes redirect) {
        Employe e = contratService.validerParRh(id, u);
        redirect.addFlashAttribute("succes", "Contrat validé. " + e.getNomComplet() + " est employé avec le matricule " + e.getMatricule() + ".");
        return "redirect:/rh/contrats/" + id;
    }

    @PostMapping("/{id}/annuler")
    public String annuler(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte u, RedirectAttributes redirect) {
        contratService.annulerParRh(id, u);
        redirect.addFlashAttribute("succes", "Contrat annulé.");
        return "redirect:/rh/contrats/" + id;
    }
}
