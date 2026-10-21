package com.mrtovo.service;

import com.mrtovo.dto.BudgetResume;
import com.mrtovo.repository.BesoinRepository;
import com.mrtovo.repository.BudgetAnnuelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/** Budget annuel de l'entreprise : montant voté, dépenses déjà engagées et reste disponible. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetService {

    private final BudgetAnnuelRepository budgetRepository;
    private final BesoinRepository besoinRepository;

    /** Null si aucun budget n'est défini pour l'année. */
    public BudgetResume resume(int annee) {
        return budgetRepository.findByAnnee(annee)
                .map(b -> {
                    BigDecimal engage = besoinRepository.sommeDepensesValidees(annee);
                    return new BudgetResume(annee, b.getMontant(), engage, b.getMontant().subtract(engage));
                })
                .orElse(null);
    }
}
