package com.mrtovo.controller;

import com.mrtovo.dto.BesoinForm;
import com.mrtovo.entity.Besoin;
import com.mrtovo.entity.HistoriqueStatut;
import com.mrtovo.entity.reference.StatutBesoin;
import com.mrtovo.entity.reference.TypeBesoin;
import com.mrtovo.entity.reference.TypeContrat;
import com.mrtovo.exception.RegleMetierException;
import com.mrtovo.exception.RessourceIntrouvableException;
import com.mrtovo.repository.DepartementRepository;
import com.mrtovo.repository.GrilleSalaireRepository;
import com.mrtovo.repository.OffreEmploiRepository;
import com.mrtovo.repository.PosteRepository;
import com.mrtovo.repository.ServiceEntrepriseRepository;
import com.mrtovo.security.Roles;
import com.mrtovo.security.UtilisateurConnecte;
import com.mrtovo.service.BesoinService;
import com.mrtovo.service.BudgetService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Besoins en personnel : création (manager), analyse et envoi à la Finance (RH), décision budgétaire (Finance).
 * Le détail /besoins/{id} est commun ; les boutons affichés dépendent du rôle et du statut.
 */
@Controller
@RequiredArgsConstructor
public class BesoinController {

    private final BesoinService besoinService;
    private final BudgetService budgetService;
    private final TraceService traceService;
    private final ReferenceService references;
    private final DepartementRepository departementRepository;
    private final ServiceEntrepriseRepository serviceRepository;
    private final PosteRepository posteRepository;
    private final GrilleSalaireRepository grilleRepository;
    private final OffreEmploiRepository offreRepository;

    // ---------------------------------------------------------------- Manager

    @GetMapping("/manager/besoins")
    public String listeManager(@AuthenticationPrincipal UtilisateurConnecte u, Model model) {
        model.addAttribute("titre", "Besoins de mon département");
        model.addAttribute("besoins", besoinService.listerPourManager(u));
        model.addAttribute("peutCreer", true);
        return "besoin/liste";
    }

    @GetMapping("/manager/besoins/nouveau")
    public String formulaire(@AuthenticationPrincipal UtilisateurConnecte u, Model model) {
        BesoinForm form = new BesoinForm();
        form.setNombre(1);
        form.setIdDepartement(u.getIdDepartement());
        return afficherFormulaire(form, u, model);
    }

    @PostMapping("/manager/besoins/nouveau")
    public String creer(@Valid @ModelAttribute("form") BesoinForm form, BindingResult erreurs,
                        @AuthenticationPrincipal UtilisateurConnecte u, Model model, RedirectAttributes redirect) {
        if (erreurs.hasErrors()) {
            return afficherFormulaire(form, u, model);
        }
        try {
            Besoin b = besoinService.creer(form, u);
            redirect.addFlashAttribute("succes", "Besoin enregistré au statut « cree » : il est transmis à la RH.");
            return "redirect:/besoins/" + b.getId();
        } catch (RegleMetierException e) {
            model.addAttribute("erreur", e.getMessage());
            return afficherFormulaire(form, u, model);
        }
    }

    @GetMapping("/manager/besoins/{id}")
    public String ancienLien(@PathVariable Integer id) {
        return "redirect:/besoins/" + id;
    }

    private String afficherFormulaire(BesoinForm form, UtilisateurConnecte u, Model model) {
        boolean directeur = u.aRole(Roles.DIRECTEUR);
        model.addAttribute("form", form);
        model.addAttribute("directeur", directeur);
        model.addAttribute("departements", departementRepository.findAllByOrderByLibelleAsc());
        model.addAttribute("services", directeur
                ? serviceRepository.findAllByOrderByDepartementLibelleAscLibelleAsc()
                : serviceRepository.findByDepartementIdOrderByLibelleAsc(u.getIdDepartement()));
        model.addAttribute("postes", directeur
                ? posteRepository.findAllByOrderByDepartementLibelleAscLibelleAsc()
                : posteRepository.findByDepartementIdAndEstActifTrueOrderByLibelleAsc(u.getIdDepartement()));
        model.addAttribute("typesBesoin", references.tous(TypeBesoin.class));
        model.addAttribute("typesContrat", references.tous(TypeContrat.class));
        return "besoin/formulaire";
    }

    // ---------------------------------------------------------------- Détail commun

    @GetMapping("/besoins/{id}")
    public String detail(@PathVariable Integer id, @AuthenticationPrincipal UtilisateurConnecte u, Model model) {
        Besoin b = besoinService.trouver(id);
        if (!besoinService.peutVoir(b, u)) {
            throw new RessourceIntrouvableException("Besoin introuvable");
        }
        model.addAttribute("besoin", b);
        model.addAttribute("historique", traceService.historique(HistoriqueStatut.BESOIN, id));
        model.addAttribute("offre", offreRepository.findByBesoinId(id).orElse(null));
        if (u.aRole(Roles.RH)) {
            model.addAttribute("grille", b.getPoste() != null
                    ? grilleRepository.findByPosteIdOrderByAncienneteMinAsc(b.getPoste().getId())
                    : grilleRepository.findAllByOrderByPosteDepartementLibelleAscPosteLibelleAscAncienneteMinAsc());
        }
        if (u.aRole(Roles.FINANCE)) {
            model.addAttribute("budget", budgetService.resume(besoinService.anneeBudget(b)));
            model.addAttribute("depense", b.getDepenseAnnuelle() != null ? b.getDepenseAnnuelle() : b.calculerDepenseAnnuelle());
        }
        return "besoin/detail";
    }

    // ---------------------------------------------------------------- RH

    @GetMapping("/rh/besoins")
    public String listeRh(@RequestParam(defaultValue = StatutBesoin.CREE) String statut, Model model) {
        model.addAttribute("titre", "Besoins des départements");
        model.addAttribute("besoins", besoinService.listerParStatut(statut));
        model.addAttribute("statuts", references.tous(StatutBesoin.class));
        model.addAttribute("statut", statut);
        model.addAttribute("urlFiltre", "/rh/besoins");
        return "besoin/liste";
    }

    @PostMapping("/rh/besoins/{id}/envoyer-finance")
    public String envoyerFinance(@PathVariable Integer id, @RequestParam(required = false) BigDecimal salaireMensuel,
                                 @RequestParam(required = false) String commentaire,
                                 @AuthenticationPrincipal UtilisateurConnecte u, RedirectAttributes redirect) {
        besoinService.envoyerFinance(id, salaireMensuel, commentaire, u);
        redirect.addFlashAttribute("succes", "Besoin envoyé à la Finance.");
        return "redirect:/besoins/" + id;
    }

    @GetMapping("/rh/grille-salaire")
    public String grille(Model model) {
        model.addAttribute("grille", grilleRepository.findAllByOrderByPosteDepartementLibelleAscPosteLibelleAscAncienneteMinAsc());
        return "rh/grille";
    }

    // ---------------------------------------------------------------- Finance

    @GetMapping("/finance/besoins")
    public String listeFinance(Model model) {
        List<Besoin> besoins = besoinService.listerParStatut(StatutBesoin.ENVOYE_FINANCE);
        model.addAttribute("titre", "Besoins à valider (Finance)");
        model.addAttribute("besoins", besoins);
        model.addAttribute("budget", budgetService.resume(LocalDate.now().getYear()));
        model.addAttribute("vueFinance", true);
        return "besoin/liste";
    }

    @PostMapping("/finance/besoins/{id}/valider")
    public String valider(@PathVariable Integer id, @RequestParam(required = false) String commentaire,
                          @AuthenticationPrincipal UtilisateurConnecte u, RedirectAttributes redirect) {
        besoinService.validerFinance(id, commentaire, u);
        redirect.addFlashAttribute("succes", "Besoin validé : la dépense est imputée sur le budget.");
        return "redirect:/finance/besoins";
    }

    @PostMapping("/finance/besoins/{id}/refuser")
    public String refuser(@PathVariable Integer id, @RequestParam(required = false) String commentaire,
                          @AuthenticationPrincipal UtilisateurConnecte u, RedirectAttributes redirect) {
        besoinService.refuserFinance(id, commentaire, u);
        redirect.addFlashAttribute("succes", "Besoin refusé.");
        return "redirect:/finance/besoins";
    }
}
