package com.mrtovo.entity.reference;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "type_contrat")
@Getter
@Setter
public class TypeContrat {

    public static final String CE = "CE";
    public static final String CDD = "CDD";
    public static final String CDI = "CDI";

    /** Un CDI n'a pas de date de fin. */
    public boolean aUneDateFin() {
        return !CDI.equals(libelle);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_contrat")
    private Integer id;

    private String libelle;
}
