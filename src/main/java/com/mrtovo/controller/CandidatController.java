package com.mrtovo.controller;

import com.mrtovo.dto.ReponseForm;
import com.mrtovo.entity.Candidature;
import com.mrtovo.entity.Contrat;
import com.mrtovo.entity.HistoriqueStatut;
import com.mrtovo.entity.Proposition;
import com.mrtovo.entity.reference.StatutProposition;
import com.mrtovo.entity.reference.TypeContrat;
import com.mrtovo.security.UtilisateurConnecte;
import com.mrtovo.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/** Espace du candidat connecté : candidatures, propositions, contrats. */
@Controller
@RequestMapping("/candidat")
@RequiredArgsConstructor
public class CandidatController {

    private static final Map<String, String> MESSAGES_DECISION = Map.of(
            StatutProposition.VALIDE, "Vous avez accepté la proposition. La RH va préparer votre contrat.",
            StatutProposition.REFUSE, "Vous avez refusé la proposition.",
            StatutProposition.EN_ATTENTE, "Votre contre-proposition est envoyée à la RH.");

    private final CandidatureService candidatureService;
    private final PropositionService propositionService;
    private final ContratService contratService;
    private final ContratPdfService contratPdfService;
    private final TraceService traceService;
    private final ReferenceService references;

    @GetMapping
    public String accueil(@AuthenticationPrincipal UtilisateurConnecte u, Model model) {
        model.addAttribute("candidatures", candidatureService.listerPourCompte(u.getId()));
        model.addAttribute("propositions", propositionService.listerPourCompte(u.getId()));
        model.addAttribute("contrats", contratService.listerPourCompte(u.getId()));
        return "candidat/accueil";
    }

    @GetMapping("/candidatures/{id}")
    public String candidature(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte u, Model model) {
        Candidature c = candidatureService.trouverPourCandidat(id, u);
        model.addAttribute("candidature", c);
        model.addAttribute("cv", candidatureService.cvDe(c).orElse(null));
        model.addAttribute("propositions", propositionService.listerPourCandidature(id));
        model.addAttribute("historique", traceService.historique(HistoriqueStatut.CANDIDATURE, id));
        return "candidat/candidature";
    }

    @GetMapping("/propositions/{id}")
    public String proposition(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte u, Model model) {
        Proposition p = propositionService.trouverPourCandidat(id, u);
        model.addAttribute("proposition", p);
        model.addAttribute("reponses", propositionService.reponses(id));
        model.addAttribute("messages", propositionService.messages(id));
        model.addAttribute("contrat", contratService.pourProposition(id));
        model.addAttribute("typesContrat", references.tous(TypeContrat.class));
        model.addAttribute("form", new ReponseForm());
        return "candidat/proposition";
    }

    @PostMapping("/propositions/{id}/reponse")
    public String repondre(@PathVariable Integer id, @ModelAttribute ReponseForm form,
                           @AuthenticationPrincipal UtilisateurConnecte u, RedirectAttributes redirect) {
        String decision = propositionService.repondre(id, form, u);
        redirect.addFlashAttribute("succes", MESSAGES_DECISION.get(decision));
        return "redirect:/candidat/propositions/" + id;
    }

    @PostMapping("/propositions/{id}/messages")
    public String message(@PathVariable Integer id, @RequestParam String contenu,
                          @AuthenticationPrincipal UtilisateurConnecte u) {
        propositionService.envoyerMessageCandidat(id, contenu, u);
        return "redirect:/candidat/propositions/" + id + "#discussion";
    }

    /** Le candidat peut relire le CV déposé avec sa candidature. */
    @GetMapping("/candidatures/{id}/cv")
    public ResponseEntity<byte[]> cv(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte u) {
        return ReponsePdf.cv(candidatureService.cvDe(candidatureService.trouverPourCandidat(id, u)));
    }

    @GetMapping("/contrats/{id}")
    public String contrat(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte u, Model model) {
        model.addAttribute("contrat", contratService.trouverPourCandidat(id, u));
        model.addAttribute("historique", traceService.historique(HistoriqueStatut.CONTRAT, id));
        return "candidat/contrat";
    }

    @GetMapping("/contrats/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte u) {
        Contrat c = contratService.trouverPourCandidat(id, u);
        return ReponsePdf.inline(contratPdfService.generer(c), "contrat-" + c.getId() + ".pdf");
    }

    @PostMapping("/contrats/{id}/signer")
    public String signer(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte u, RedirectAttributes redirect) {
        contratService.signerParCandidat(id, u);
        redirect.addFlashAttribute("succes", "Contrat signé. Il sera définitif après la signature de la RH.");
        return "redirect:/candidat/contrats/" + id;
    }

    @PostMapping("/contrats/{id}/annuler")
    public String annuler(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte u, RedirectAttributes redirect) {
        contratService.annulerParCandidat(id, u);
        redirect.addFlashAttribute("succes", "Contrat annulé.");
        return "redirect:/candidat/contrats/" + id;
    }
}
