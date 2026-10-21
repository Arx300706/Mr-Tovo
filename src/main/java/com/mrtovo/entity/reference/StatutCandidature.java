package com.mrtovo.entity.reference;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "statut_candidature")
@Getter
@Setter
public class StatutCandidature {

    public static final String CREE = "cree";
    public static final String SELECTIONNE = "selectionne";
    public static final String TESTE_TECHNIQUEMENT = "teste techniquement";
    public static final String ENTRETIEN_OK = "entretien ok";
    public static final String SUCCES = "succes";
    public static final String REJETE = "rejete";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_statut_candidature")
    private Integer id;

    private String libelle;
}
