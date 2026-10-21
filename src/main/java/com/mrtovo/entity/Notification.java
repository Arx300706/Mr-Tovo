package com.mrtovo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/** Message de l'entreprise vers un candidat (compte) ou un employé. */
@Entity
@Table(name = "notification")
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notification")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_compte")
    private Compte compte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employe")
    private Employe employe;

    private String message;

    private String lien;

    private boolean lue;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dateCreation;
}
