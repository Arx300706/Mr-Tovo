package com.mrtovo.service;

import com.mrtovo.dto.BesoinForm;
import com.mrtovo.dto.BudgetResume;
import com.mrtovo.entity.*;
import com.mrtovo.entity.reference.StatutBesoin;
import com.mrtovo.entity.reference.TypeBesoin;
import com.mrtovo.entity.reference.TypeContrat;
import com.mrtovo.exception.RegleMetierException;
import com.mrtovo.exception.RessourceIntrouvableException;
import com.mrtovo.repository.*;
import com.mrtovo.security.Roles;
import com.mrtovo.security.UtilisateurConnecte;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Cycle de vie d'un besoin :
 * cree (manager) -> envoie Finance (RH) -> valide Finance / refuse finance (Finance).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BesoinService {

    private final BesoinRepository besoinRepository;
    private final EmployeRepository employeRepository;
    private final DepartementRepository departementRepository;
    private final ServiceEntrepriseRepository serviceRepository;
    private final PosteRepository posteRepository;
    private final ReferenceService references;
    private final BudgetService budgetService;
    private final TraceService traceService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Besoin trouver(Integer id) {
        return besoinRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Besoin introuvable"));
    }

    /** Un manager ne voit que les besoins de son département ; le directeur voit tout. */
    @Transactional(readOnly = true)
    public List<Besoin> listerPourManager(UtilisateurConnecte u) {
        return u.aRole(Roles.DIRECTEUR)
                ? besoinRepository.findAllByOrderByDateCreationDesc()
                : besoinRepository.findByDepartementIdOrderByDateCreationDesc(u.getIdDepartement());
    }

    @Transactional(readOnly = true)
    public List<Besoin> listerParStatut(String statut) {
        return statut == null || statut.isBlank()
                ? besoinRepository.findAllByOrderByDateCreationDesc()
                : besoinRepository.findByStatutLibelleOrderByDateCreationDesc(statut);
    }

    @Transactional(readOnly = true)
    public List<Besoin> listerValidesSansOffre() {
        return besoinRepository.findValidesSansOffre();
    }

    @Transactional(readOnly = true)
    public boolean peutVoir(Besoin b, UtilisateurConnecte u) {
        return u.aRole(Roles.DIRECTEUR) || u.aRole(Roles.RH) || u.aRole(Roles.FINANCE)
                || Objects.equals(b.getDepartement().getId(), u.getIdDepartement());
    }

    // ---------------------------------------------------------------- Étape 1 : manager

    public Besoin creer(BesoinForm f, UtilisateurConnecte u) {
        Employe manager = employeRepository.findById(u.getId())
                .orElseThrow(() -> new RegleMetierException("Employé introuvable"));
        Integer idDepartement = u.aRole(Roles.DIRECTEUR) && f.getIdDepartement() != null
                ? f.getIdDepartement() : u.getIdDepartement();
        Departement departement = departementRepository.findById(idDepartement)
                .orElseThrow(() -> new RegleMetierException("Département introuvable"));
        ServiceEntreprise service = serviceRepository.findById(f.getIdService())
                .filter(s -> s.getDepartement().getId().equals(departement.getId()))
                .orElseThrow(() -> new RegleMetierException("Ce service n'appartient pas au département " + departement.getLibelle()));
        TypeBesoin type = references.parId(TypeBesoin.class, f.getIdTypeBesoin());
        if (type == null) {
            throw new RegleMetierException("Type de besoin invalide");
        }

        Poste poste = null;
        if (f.getIdPoste() != null) {
            poste = posteRepository.findById(f.getIdPoste())
                    .filter(p -> p.getDepartement() != null && p.getDepartement().getId().equals(departement.getId()))
                    .orElseThrow(() -> new RegleMetierException("Ce poste n'appartient pas au département"));
        } else if (TypeBesoin.RECHERCHE_PERSONNEL.equals(type.getLibelle())) {
            throw new RegleMetierException("Pour une recherche de personnel, choisissez le poste concerné");
        }
        if (f.getAgeMin() != null && f.getAgeMax() != null && f.getAgeMin() > f.getAgeMax()) {
            throw new RegleMetierException("L'âge minimum doit être inférieur à l'âge maximum");
        }

        Besoin b = new Besoin();
        b.setManager(manager);
        b.setTypeBesoin(type);
        b.setDepartement(departement);
        b.setService(service);
        b.setPoste(poste);
        b.setIntitule(f.getIntitule().trim());
        b.setNombre(f.getNombre());
        b.setSexe(f.getSexe());
        b.setAgeMin(f.getAgeMin());
        b.setAgeMax(f.getAgeMax());
        b.setTypeContrat(references.parId(TypeContrat.class, f.getIdTypeContrat()));
        b.setDateCommencement(f.getDateCommencement());
        b.setBudget(f.getBudget());
        b.setMotif(f.getMotif());
        b.setStatut(references.parLibelle(StatutBesoin.class, StatutBesoin.CREE));
        besoinRepository.save(b);
        traceService.tracer(HistoriqueStatut.BESOIN, b.getId(), null, StatutBesoin.CREE, u, null);
        return b;
    }

    // ---------------------------------------------------------------- Étape 2 : RH

    public void envoyerFinance(Integer id, BigDecimal salaireMensuel, String commentaire, UtilisateurConnecte u) {
        Besoin b = trouver(id);
        verifierStatut(b, StatutBesoin.CREE);
        if (salaireMensuel == null || salaireMensuel.signum() <= 0) {
            throw new RegleMetierException("Indiquez le salaire mensuel retenu d'après la grille salariale");
        }
        b.setSalaireMensuel(salaireMensuel);
        b.setCommentaireRh(commentaire);
        changerStatut(b, StatutBesoin.ENVOYE_FINANCE, u, commentaire);
    }

    // ---------------------------------------------------------------- Étape 3 : Finance

    /** Année budgétaire imputée : celle de la date de commencement, sinon l'année en cours. */
    public int anneeBudget(Besoin b) {
        return b.getDateCommencement() != null ? b.getDateCommencement().getYear() : LocalDate.now().getYear();
    }

    public void validerFinance(Integer id, String commentaire, UtilisateurConnecte u) {
        Besoin b = trouver(id);
        verifierStatut(b, StatutBesoin.ENVOYE_FINANCE);
        int annee = anneeBudget(b);
        BudgetResume budget = budgetService.resume(annee);
        if (budget == null) {
            throw new RegleMetierException("Aucun budget annuel n'est défini pour " + annee);
        }
        BigDecimal depense = b.calculerDepenseAnnuelle();
        if (!budget.couvre(depense)) {
            throw new RegleMetierException("Budget insuffisant : la dépense dépasse le budget restant. Le besoin doit être refusé.");
        }
        b.setDepenseAnnuelle(depense);
        b.setAnneeBudget(annee);
        b.setCommentaireFinance(commentaire);
        changerStatut(b, StatutBesoin.VALIDE_FINANCE, u, commentaire);
        notificationService.notifierEmploye(b.getManager(),
                "Votre besoin « " + b.getIntitule() + " » a été validé par la Finance.", "/manager/besoins/" + b.getId());
    }

    public void refuserFinance(Integer id, String commentaire, UtilisateurConnecte u) {
        Besoin b = trouver(id);
        verifierStatut(b, StatutBesoin.ENVOYE_FINANCE);
        b.setCommentaireFinance(commentaire);
        changerStatut(b, StatutBesoin.REFUSE_FINANCE, u, commentaire);
        notificationService.notifierEmploye(b.getManager(),
                "Votre besoin « " + b.getIntitule() + " » a été refusé par la Finance.", "/manager/besoins/" + b.getId());
    }

    // ----------------------------------------------------------------

    private void verifierStatut(Besoin b, String attendu) {
        if (!attendu.equals(b.getStatut().getLibelle())) {
            throw new RegleMetierException("Action impossible : le besoin est au statut « " + b.getStatut().getLibelle() + " »");
        }
    }

    private void changerStatut(Besoin b, String nouveau, UtilisateurConnecte u, String commentaire) {
        String ancien = b.getStatut().getLibelle();
        b.setStatut(references.parLibelle(StatutBesoin.class, nouveau));
        traceService.tracer(HistoriqueStatut.BESOIN, b.getId(), ancien, nouveau, u, commentaire);
    }
}
