package com.mrtovo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "departement")
@Getter
@Setter
public class Departement {

    /** Codes qui donnent des droits particuliers aux employés du département. */
    public static final String CODE_DIRECTION = "DIR";
    public static final String CODE_RH = "RH";
    public static final String CODE_FINANCE = "FIN";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_departement")
    private Integer id;

    private String code;

    private String libelle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_manager_actuel")
    private Employe managerActuel;
}
