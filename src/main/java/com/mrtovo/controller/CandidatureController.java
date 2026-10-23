package com.mrtovo.controller;

import com.mrtovo.dto.PlanificationForm;
import com.mrtovo.entity.Candidature;
import com.mrtovo.entity.HistoriqueStatut;
import com.mrtovo.entity.reference.StatutCandidature;
import com.mrtovo.exception.RegleMetierException;
import com.mrtovo.security.UtilisateurConnecte;
import com.mrtovo.service.CandidatureService;
import com.mrtovo.service.PropositionService;
import com.mrtovo.service.ReferenceService;
import com.mrtovo.service.TraceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ContentDisposition;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Traitement des candidatures par la RH : sélection, tests, entretien, résultat. */
@Controller
@RequestMapping("/rh/candidatures")
@RequiredArgsConstructor
public class CandidatureController {

    private final CandidatureService candidatureService;
    private final PropositionService propositionService;
    private final TraceService traceService;
    private final ReferenceService references;

    @GetMapping
    public String liste(@RequestParam(defaultValue = StatutCandidature.CREE) String statut, Model model) {
        model.addAttribute("candidatures", candidatureService.listerParStatut(statut));
        model.addAttribute("statuts", references.tous(StatutCandidature.class));
        model.addAttribute("statut", statut);
        return "candidature/liste";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Candidature c = candidatureService.trouver(id);
        PlanificationForm form = new PlanificationForm();
        form.setDateTestTechnique(c.getDateTestTechnique());
        form.setDateEntretien(c.getDateEntretien());
        return afficherDetail(c, form, model);
    }

    @GetMapping("/{id}/cv")
    public ResponseEntity<byte[]> cv(@PathVariable Integer id) {
        return ReponsePdf.cv(candidatureService.cvDe(candidatureService.trouver(id)));
    }

    @GetMapping("/{id}/cv-en-ligne")
    public String cvEnLigne(@PathVariable Integer id, Model model) {
        model.addAttribute("candidature", candidatureService.trouver(id));
        model.addAttribute("elements", candidatureService.elementsCv(id));
        model.addAttribute("pieceIdentite", candidatureService.pieceIdentite(id).orElse(null));
        model.addAttribute("retour", "/rh/candidatures/" + id);
        model.addAttribute("photoUrl", "/rh/candidatures/" + id + "/photo");
        model.addAttribute("pieceIdentiteUrl", "/rh/candidatures/" + id + "/piece-identite");
        return "cv/detail";
    }

    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> photo(@PathVariable Integer id) {
        Candidature c = candidatureService.trouver(id);
        if (c.getPhoto() == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(c.getPhotoType())).body(c.getPhoto());
    }

    @GetMapping("/{id}/piece-identite")
    public ResponseEntity<byte[]> pieceIdentite(@PathVariable Integer id) {
        candidatureService.trouver(id);
        return candidatureService.pieceIdentite(id).map(document -> ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(document.getNomFichier()).build().toString())
                .body(document.getFichier())).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/planifier")
    public String planifier(@PathVariable Integer id, @Valid @ModelAttribute("planification") PlanificationForm form,
                            BindingResult erreurs, @AuthenticationPrincipal UtilisateurConnecte u,
                            Model model, RedirectAttributes redirect) {
        Candidature c = candidatureService.trouver(id);
        if (erreurs.hasErrors()) {
            return afficherDetail(c, form, model);
        }
        try {
            candidatureService.planifier(id, form, u);
        } catch (RegleMetierException e) {
            model.addAttribute("erreur", e.getMessage());
            return afficherDetail(c, form, model);
        }
        redirect.addFlashAttribute("succes", "Candidature sélectionnée : le candidat voit les dates dans son espace.");
        return "redirect:/rh/candidatures/" + id;
    }

    @PostMapping("/{id}/teste")
    public String teste(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte u, RedirectAttributes redirect) {
        candidatureService.marquerTeste(id, u);
        redirect.addFlashAttribute("succes", "Test technique passé.");
        return "redirect:/rh/candidatures/" + id;
    }

    @PostMapping("/{id}/entretien-ok")
    public String entretienOk(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte u, RedirectAttributes redirect) {
        candidatureService.marquerEntretienOk(id, u);
        redirect.addFlashAttribute("succes", "Entretien réussi.");
        return "redirect:/rh/candidatures/" + id;
    }

    @PostMapping("/{id}/succes")
    public String succes(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte u, RedirectAttributes redirect) {
        candidatureService.marquerSucces(id, u);
        redirect.addFlashAttribute("succes", "Candidature réussie : vous pouvez créer la proposition d'emploi.");
        return "redirect:/rh/candidatures/" + id;
    }

    @PostMapping("/{id}/rejeter")
    public String rejeter(@PathVariable Integer id, @RequestParam(required = false) String commentaire,
                          @AuthenticationPrincipal UtilisateurConnecte u, RedirectAttributes redirect) {
        candidatureService.rejeter(id, commentaire, u);
        redirect.addFlashAttribute("succes", "Candidature rejetée.");
        return "redirect:/rh/candidatures/" + id;
    }

    private String afficherDetail(Candidature c, PlanificationForm form, Model model) {
        model.addAttribute("candidature", c);
        model.addAttribute("cv", candidatureService.cvDe(c).orElse(null));
        model.addAttribute("planification", form);
        model.addAttribute("propositions", propositionService.listerPourCandidature(c.getId()));
        model.addAttribute("historique", traceService.historique(HistoriqueStatut.CANDIDATURE, c.getId()));
        return "candidature/detail";
    }
}
