package com.mrtovo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "commune")
@Getter @Setter
public class Commune {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_commune")
    private Integer id;
    @Column(nullable = false)
    private String libelle;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ville")
    private Ville ville;
}
