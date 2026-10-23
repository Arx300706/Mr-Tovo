package com.mrtovo.controller;

import com.mrtovo.dto.PropositionForm;
import com.mrtovo.entity.Candidature;
import com.mrtovo.entity.HistoriqueStatut;
import com.mrtovo.entity.Proposition;
import com.mrtovo.entity.reference.StatutProposition;
import com.mrtovo.entity.reference.TypeContrat;
import com.mrtovo.exception.RegleMetierException;
import com.mrtovo.security.UtilisateurConnecte;
import com.mrtovo.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Propositions d'emploi côté RH : création, négociation, discussion. */
@Controller
@RequestMapping("/rh/propositions")
@RequiredArgsConstructor
public class PropositionController {

    private final PropositionService propositionService;
    private final CandidatureService candidatureService;
    private final ContratService contratService;
    private final TraceService traceService;
    private final ReferenceService references;

    @GetMapping
    public String liste(@RequestParam(required = false) String statut, Model model) {
        model.addAttribute("candidaturesSucces", candidatureService.listerSuccesSansProposition());
        model.addAttribute("propositions", propositionService.listerParStatut(statut));
        model.addAttribute("statuts", references.tous(StatutProposition.class));
        model.addAttribute("statut", statut);
        return "proposition/liste";
    }

    @GetMapping("/nouvelle")
    public String formulaire(@RequestParam Integer candidature, Model model) {
        Candidature c = candidatureService.trouver(candidature);
        return afficherFormulaire(c, propositionService.formulairePour(c), null, model);
    }

    @PostMapping("/nouvelle")
    public String creer(@RequestParam Integer candidature, @Valid @ModelAttribute("form") PropositionForm form,
                        BindingResult erreurs, @AuthenticationPrincipal UtilisateurConnecte u,
                        Model model, RedirectAttributes redirect) {
        Candidature c = candidatureService.trouver(candidature);
        if (erreurs.hasErrors()) {
            return afficherFormulaire(c, form, null, model);
        }
        try {
            Proposition p = propositionService.creer(candidature, form, u);
            redirect.addFlashAttribute("succes", "Proposition envoyée au candidat.");
            return "redirect:/rh/propositions/" + p.getId();
        } catch (RegleMetierException e) {
            model.addAttribute("erreur", e.getMessage());
            // La transaction annulée a vidé le contexte de persistance : la candidature est rechargée
            return afficherFormulaire(candidatureService.trouver(candidature), form, null, model);
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Proposition p = propositionService.trouver(id);
        model.addAttribute("proposition", p);
        model.addAttribute("reponses", propositionService.reponses(id));
        model.addAttribute("messages", propositionService.messages(id));
        model.addAttribute("contrat", contratService.pourProposition(id));
        model.addAttribute("historique", traceService.historique(HistoriqueStatut.PROPOSITION, id));
        return "proposition/detail";
    }

    @GetMapping("/{id}/reviser")
    public String formulaireRevision(@PathVariable Integer id, Model model) {
        Proposition p = propositionService.trouver(id);
        return afficherFormulaire(p.getCandidature(), propositionService.formulairePour(p), p, model);
    }

    @PostMapping("/{id}/reviser")
    public String reviser(@PathVariable Integer id, @Valid @ModelAttribute("form") PropositionForm form,
                          BindingResult erreurs, @AuthenticationPrincipal UtilisateurConnecte u,
                          Model model, RedirectAttributes redirect) {
        Proposition p = propositionService.trouver(id);
        if (erreurs.hasErrors()) {
            return afficherFormulaire(p.getCandidature(), form, p, model);
        }
        try {
            propositionService.reviser(id, form, u);
        } catch (RegleMetierException e) {
            model.addAttribute("erreur", e.getMessage());
            Proposition actuelle = propositionService.trouver(id);
            return afficherFormulaire(actuelle.getCandidature(), form, actuelle, model);
        }
        redirect.addFlashAttribute("succes", "Proposition révisée et renvoyée au candidat.");
        return "redirect:/rh/propositions/" + id;
    }

    @PostMapping("/{id}/accepter-contre-proposition")
    public String accepter(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte u, RedirectAttributes redirect) {
        propositionService.accepterContreProposition(id, u);
        redirect.addFlashAttribute("succes", "Contre-proposition acceptée : la proposition est validée.");
        return "redirect:/rh/propositions/" + id;
    }

    @PostMapping("/{id}/clore")
    public String clore(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte u, RedirectAttributes redirect) {
        propositionService.clore(id, u);
        redirect.addFlashAttribute("succes", "Négociation close.");
        return "redirect:/rh/propositions/" + id;
    }

    @PostMapping("/{id}/messages")
    public String message(@PathVariable Integer id, @RequestParam String contenu, @AuthenticationPrincipal UtilisateurConnecte u) {
        propositionService.envoyerMessageRh(id, contenu, u);
        return "redirect:/rh/propositions/" + id + "#discussion";
    }

    private String afficherFormulaire(Candidature c, PropositionForm form, Proposition revision, Model model) {
        model.addAttribute("candidature", c);
        model.addAttribute("form", form);
        model.addAttribute("revision", revision);
        model.addAttribute("postesVacants", propositionService.postesVacants(c));
        model.addAttribute("typesContrat", references.tous(TypeContrat.class));
        if (revision != null) {
            model.addAttribute("derniereReponse", propositionService.reponses(revision.getId()).stream().findFirst().orElse(null));
        }
        return "proposition/formulaire";
    }
}
