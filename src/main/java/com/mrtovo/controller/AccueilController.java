package com.mrtovo.controller;

import com.mrtovo.dto.CandidatureForm;
import com.mrtovo.entity.Candidature;
import com.mrtovo.entity.OffreEmploi;
import com.mrtovo.entity.Ville;
import com.mrtovo.entity.Commune;
import com.mrtovo.entity.Fokontany;
import com.mrtovo.entity.reference.StatutOffre;
import com.mrtovo.entity.reference.TypeCandidature;
import com.mrtovo.exception.RegleMetierException;
import com.mrtovo.exception.RessourceIntrouvableException;
import com.mrtovo.security.UtilisateurConnecte;
import com.mrtovo.security.UtilisateurDetailsService;
import com.mrtovo.service.CandidatureService;
import com.mrtovo.service.OffreService;
import com.mrtovo.service.ReferenceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Partie publique : liste des offres, candidature, connexion. */
@Controller
@RequiredArgsConstructor
public class AccueilController {

    private final OffreService offreService;
    private final CandidatureService candidatureService;
    private final ReferenceService references;
    private final UtilisateurDetailsService utilisateurDetailsService;
    private final SecurityContextRepository securityContextRepository;

    @GetMapping("/")
    public String accueil(Model model) {
        model.addAttribute("offres", offreService.listerPubliees());
        return "public/accueil";
    }

    @GetMapping("/offres/{id}")
    public String offre(@PathVariable Integer id, Model model) {
        model.addAttribute("offre", offrePubliee(id));
        return "public/offre";
    }

    @GetMapping("/offres/{id}/candidater")
    public String formulaireCandidature(@PathVariable Integer id, Model model) {
        OffreEmploi offre = offrePubliee(id);
        if (!offre.estOuverte()) {
            throw new RessourceIntrouvableException("Offre fermée");
        }
        CandidatureForm form = new CandidatureForm();
        form.setIdTypeCandidature(references.parLibelle(TypeCandidature.class, TypeCandidature.EXTERNE).getId());
        return afficherFormulaire(offre, form, model);
    }

    @PostMapping("/offres/{id}/candidater")
    public String candidater(@PathVariable Integer id,
                             @Valid @ModelAttribute("form") CandidatureForm form, BindingResult erreurs,
                             @AuthenticationPrincipal UtilisateurConnecte u, Model model,
                             HttpServletRequest request, HttpServletResponse response,
                             RedirectAttributes redirect) {
        OffreEmploi offre = offrePubliee(id);
        if (erreurs.hasErrors()) {
            return afficherFormulaire(offre, form, model);
        }
        Candidature c;
        try {
            c = candidatureService.postuler(id, form, u);
        } catch (RegleMetierException e) {
            model.addAttribute("erreur", e.getMessage());
            // La transaction annulée a vidé le contexte de persistance : l'offre est rechargée
            return afficherFormulaire(offrePubliee(id), form, model);
        }
        if (u == null || !u.estCandidat()) {
            connecterCandidat(c, request, response);
        }
        redirect.addFlashAttribute("succes", "Votre candidature est enregistrée. Suivez son avancement depuis votre espace candidat.");
        return "redirect:/candidat/candidatures/" + c.getId();
    }

    @GetMapping("/connexion")
    public String connexion() {
        return "auth/connexion";
    }

    /** Après connexion : espace candidat ou espace employé. */
    @GetMapping("/tableau-de-bord")
    public String tableauDeBord(@AuthenticationPrincipal UtilisateurConnecte u) {
        return u.estCandidat() ? "redirect:/candidat" : "redirect:/espace";
    }

    private String afficherFormulaire(OffreEmploi offre, CandidatureForm form, Model model) {
        model.addAttribute("offre", offre);
        model.addAttribute("form", form);
        model.addAttribute("typesCandidature", references.tous(TypeCandidature.class));
        model.addAttribute("villes", references.tous(Ville.class));
        model.addAttribute("communes", references.tous(Commune.class));
        model.addAttribute("fokontanyListe", references.tous(Fokontany.class));
        return "public/candidater";
    }

    private OffreEmploi offrePubliee(Integer id) {
        OffreEmploi offre = offreService.trouver(id);
        String statut = offre.getStatut().getLibelle();
        if (!StatutOffre.PUBLIE.equals(statut) && !StatutOffre.FIN.equals(statut)) {
            throw new RessourceIntrouvableException("Offre introuvable");
        }
        return offre;
    }

    /** Le compte vient d'être créé avec la candidature : on connecte directement le candidat. */
    private void connecterCandidat(Candidature c, HttpServletRequest request, HttpServletResponse response) {
        UtilisateurConnecte principal = utilisateurDetailsService.pourCandidat(c.getCompte());
        SecurityContext contexte = SecurityContextHolder.createEmptyContext();
        contexte.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities()));
        SecurityContextHolder.setContext(contexte);
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        securityContextRepository.saveContext(contexte, request, response);
    }
}
