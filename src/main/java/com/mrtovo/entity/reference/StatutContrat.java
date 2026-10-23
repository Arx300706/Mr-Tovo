package com.mrtovo.entity.reference;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "statut_contrat")
@Getter
@Setter
public class StatutContrat {

    public static final String CREE = "cree";
    public static final String SIGNE = "signe";
    public static final String VALIDE = "valide";
    public static final String ANNULE = "annule";
    public static final String ROMPU = "rompu";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_statut_contrat")
    private Integer id;

    private String libelle;
}
