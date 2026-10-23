package com.mrtovo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "fokontany")
@Getter @Setter
public class Fokontany {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fokontany")
    private Integer id;
    @Column(nullable = false)
    private String libelle;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_commune")
    private Commune commune;
}
