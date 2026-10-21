package com.mrtovo.entity.reference;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "type_candidature")
@Getter
@Setter
public class TypeCandidature {

    public static final String EMPLOYE = "employe";
    public static final String EXTERNE = "externe";
    public static final String ANCIENNE_RELATION = "ancienne relation";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_candidature")
    private Integer id;

    private String libelle;
}
