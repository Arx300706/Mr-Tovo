package com.mrtovo.entity;

import com.mrtovo.entity.reference.StatutBesoin;
import com.mrtovo.entity.reference.TypeBesoin;
import com.mrtovo.entity.reference.TypeContrat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Expression de besoin en personnel, créée par un manager. */
@Entity
@Table(name = "besoin")
@Getter
@Setter
public class Besoin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_besoin")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_manager")
    private Employe manager;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_type_besoin")
    private TypeBesoin typeBesoin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_statut_besoin")
    private StatutBesoin statut;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_departement")
    private Departement departement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_service")
    private ServiceEntreprise service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_poste")
    private Poste poste;

    private String intitule;

    private int nombre;

    private String sexe;

    private Integer ageMin;

    private Integer ageMax;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_type_contrat")
    private TypeContrat typeContrat;

    private LocalDate dateCommencement;

    private BigDecimal budget;

    @Column(columnDefinition = "text")
    private String motif;

    private BigDecimal salaireMensuel;

    @Column(columnDefinition = "text")
    private String commentaireRh;

    private BigDecimal depenseAnnuelle;

    private Integer anneeBudget;

    @Column(columnDefinition = "text")
    private String commentaireFinance;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dateCreation;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(insertable = false, updatable = false)
    private LocalDateTime dateModification;

    /** Dépense annuelle = nombre x salaire mensuel x 12 (null tant que la RH n'a pas fixé le salaire). */
    public BigDecimal calculerDepenseAnnuelle() {
        if (salaireMensuel == null) {
            return null;
        }
        return salaireMensuel.multiply(BigDecimal.valueOf(12L * nombre));
    }
}
