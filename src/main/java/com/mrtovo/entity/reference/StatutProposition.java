package com.mrtovo.entity.reference;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "statut_proposition")
@Getter
@Setter
public class StatutProposition {

    public static final String ENREGISTRE = "enregistre";
    public static final String VALIDE = "valide";
    public static final String REFUSE = "refuse";
    public static final String EN_ATTENTE = "en attente";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_statut_proposition")
    private Integer id;

    private String libelle;
}
