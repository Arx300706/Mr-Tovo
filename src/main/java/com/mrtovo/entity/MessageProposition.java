package com.mrtovo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/** Message échangé entre la RH et le candidat pendant la négociation d'une proposition. */
@Entity
@Table(name = "message_proposition")
@Getter
@Setter
public class MessageProposition {

    public static final String AUTEUR_CANDIDAT = "CANDIDAT";
    public static final String AUTEUR_RH = "RH";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_message")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proposition")
    private Proposition proposition;

    private String auteur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employe")
    private Employe employe;

    @Column(columnDefinition = "text")
    private String contenu;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dateEnvoi;
}
