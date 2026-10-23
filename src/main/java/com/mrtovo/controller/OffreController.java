package com.mrtovo.controller;

import com.mrtovo.dto.OffreForm;
import com.mrtovo.entity.Besoin;
import com.mrtovo.entity.HistoriqueStatut;
import com.mrtovo.entity.OffreEmploi;
import com.mrtovo.entity.reference.StatutOffre;
import com.mrtovo.exception.RegleMetierException;
import com.mrtovo.repository.CandidatureRepository;
import com.mrtovo.security.UtilisateurConnecte;
import com.mrtovo.service.BesoinService;
import com.mrtovo.service.OffreService;
import com.mrtovo.service.ReferenceService;
import com.mrtovo.service.TraceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Offres d'emploi : saisie et publication (RH), validation (directeur). */
@Controller
@RequiredArgsConstructor
public class OffreController {

    private final OffreService offreService;
    private final BesoinService besoinService;
    private final TraceService traceService;
    private final ReferenceService references;
    private final CandidatureRepository candidatureRepository;

    @GetMapping("/rh/offres")
    public String liste(@RequestParam(required = false) String statut, Model model) {
        model.addAttribute("titre", "Offres d'emploi");
        model.addAttribute("offres", offreService.listerParStatut(statut));
        model.addAttribute("statuts", references.tous(StatutOffre.class));
        model.addAttribute("statut", statut);
        model.addAttribute("urlFiltre", "/rh/offres");
        return "offre/liste";
    }

    @GetMapping("/rh/offres/besoins-valides")
    public String besoinsValides(Model model) {
        model.addAttribute("titre", "Besoins validés par la Finance — offre à créer");
        model.addAttribute("besoins", besoinService.listerValidesSansOffre());
        model.addAttribute("vueCreationOffre", true);
        return "besoin/liste";
    }

    @GetMapping("/rh/offres/nouvelle")
    public String formulaire(@RequestParam Integer besoin, Model model) {
        Besoin b = besoinService.trouver(besoin);
        return afficherFormulaire(b, offreService.formulairePour(b), model);
    }

    @PostMapping("/rh/offres/nouvelle")
    public String creer(@RequestParam Integer besoin, @Valid @ModelAttribute("form") OffreForm form, BindingResult erreurs,
                        @AuthenticationPrincipal UtilisateurConnecte u, Model model, RedirectAttributes redirect) {
        Besoin b = besoinService.trouver(besoin);
        if (erreurs.hasErrors()) {
            return afficherFormulaire(b, form, model);
        }
        try {
            OffreEmploi o = offreService.creer(besoin, form, u);
            redirect.addFlashAttribute("succes", "Offre enregistrée : elle attend la validation du directeur.");
            return "redirect:/rh/offres/" + o.getId();
        } catch (RegleMetierException e) {
            model.addAttribute("erreur", e.getMessage());
            return afficherFormulaire(b, form, model);
        }
    }

    @GetMapping("/rh/offres/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("offre", offreService.trouver(id));
        model.addAttribute("candidatures", candidatureRepository.findByOffreIdOrderByDateCandidatureDesc(id));
        model.addAttribute("historique", traceService.historique(HistoriqueStatut.OFFRE, id));
        return "offre/detail";
    }

    @PostMapping("/rh/offres/{id}/publier")
    public String publier(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte u, RedirectAttributes redirect) {
        offreService.publier(id, u);
        redirect.addFlashAttribute("succes", "Offre publiée : elle est visible sur la page d'accueil.");
        return "redirect:/rh/offres/" + id;
    }

    @PostMapping("/rh/offres/{id}/cloturer")
    public String cloturer(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte u, RedirectAttributes redirect) {
        offreService.cloturer(id, u);
        redirect.addFlashAttribute("succes", "Offre clôturée.");
        return "redirect:/rh/offres/" + id;
    }

    // ---------------------------------------------------------------- Directeur

    @GetMapping("/directeur/offres")
    public String aValider(Model model) {
        model.addAttribute("titre", "Offres à valider (Directeur)");
        model.addAttribute("offres", offreService.listerParStatut(StatutOffre.ENREGISTRE));
        return "offre/liste";
    }

    @PostMapping("/directeur/offres/{id}/valider")
    public String valider(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte u, RedirectAttributes redirect) {
        offreService.valider(id, u);
        redirect.addFlashAttribute("succes", "Offre validée : la RH peut maintenant la publier.");
        return "redirect:/directeur/offres";
    }

    private String afficherFormulaire(Besoin b, OffreForm form, Model model) {
        model.addAttribute("besoin", b);
        model.addAttribute("form", form);
        return "offre/formulaire";
    }
}
