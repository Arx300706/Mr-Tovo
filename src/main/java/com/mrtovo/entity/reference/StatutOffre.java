package com.mrtovo.entity.reference;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "statut_offre")
@Getter
@Setter
public class StatutOffre {

    public static final String ENREGISTRE = "enregistre";
    public static final String VALIDE = "valide";
    public static final String PUBLIE = "publie";
    public static final String FIN = "fin";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_statut_offre")
    private Integer id;

    private String libelle;
}
