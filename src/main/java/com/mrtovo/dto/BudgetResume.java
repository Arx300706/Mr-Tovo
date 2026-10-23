package com.mrtovo.dto;

import java.math.BigDecimal;

public record BudgetResume(int annee, BigDecimal montant, BigDecimal engage, BigDecimal restant) {

    public boolean couvre(BigDecimal depense) {
        return depense != null && restant.compareTo(depense) >= 0;
    }
}
