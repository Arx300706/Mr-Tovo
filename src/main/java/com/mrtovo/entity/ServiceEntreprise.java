package com.mrtovo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Service d'un département (nommé ainsi pour ne pas confondre avec l'annotation Spring @Service). */
@Entity
@Table(name = "service")
@Getter
@Setter
public class ServiceEntreprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_service")
    private Integer id;

    private String libelle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_departement")
    private Departement departement;
}
