package com.mrtovo.entity.reference;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "type_besoin")
@Getter
@Setter
public class TypeBesoin {

    public static final String CREATION_POSTE = "creation poste";
    public static final String RECHERCHE_PERSONNEL = "recherche personnel";
    public static final String RECHERCHE_STAGIAIRE = "recherche stagiaire";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_besoin")
    private Integer id;

    private String libelle;
}
