package com.mrtovo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ville")
@Getter @Setter
public class Ville {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ville")
    private Integer id;
    @Column(nullable = false, unique = true)
    private String libelle;
}
