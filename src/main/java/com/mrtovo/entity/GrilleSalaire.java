package com.mrtovo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "grille_salaire")
@Getter
@Setter
public class GrilleSalaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_grille_salaire")
    private Integer id;

    private BigDecimal valeur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_poste")
    private Poste poste;

    private int ancienneteMin;

    private int ancienneteMax;
}
