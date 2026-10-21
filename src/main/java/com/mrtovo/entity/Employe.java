package com.mrtovo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employe_compte")
@Getter
@Setter
public class Employe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_employe")
    private Integer id;

    private String nom;

    private String prenom;

    private String matricule;

    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_poste_actuel")
    private Poste posteActuel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contrat_actuel")
    private Contrat contratActuel;

    private String motDePasse;

    private LocalDate dateEmbauche;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(insertable = false, updatable = false)
    private LocalDateTime dateModification;

    public String getNomComplet() {
        return prenom + " " + nom;
    }
}
