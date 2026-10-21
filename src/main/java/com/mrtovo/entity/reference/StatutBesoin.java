package com.mrtovo.entity.reference;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "statut_besoin")
@Getter
@Setter
public class StatutBesoin {

    public static final String CREE = "cree";
    public static final String ENVOYE_FINANCE = "envoie Finance";
    public static final String VALIDE_FINANCE = "valide Finance";
    public static final String REFUSE_FINANCE = "refuse finance";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_statut_besoin")
    private Integer id;

    private String libelle;
}
