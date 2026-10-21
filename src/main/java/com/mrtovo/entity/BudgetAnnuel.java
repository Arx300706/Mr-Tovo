package com.mrtovo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "budget_annuel")
@Getter
@Setter
public class BudgetAnnuel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_budget_annuel")
    private Integer id;

    private int annee;

    private BigDecimal montant;
}
