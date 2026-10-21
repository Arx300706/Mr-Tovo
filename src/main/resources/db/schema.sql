-- =====================================================================
--  Mr Tovo - Logiciel de gestion RH
--  Script de création de la base (MySQL 8+)
-- =====================================================================

CREATE DATABASE IF NOT EXISTS mr_tovo
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE mr_tovo;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS journal_audit;
DROP TABLE IF EXISTS historique_employe;
DROP TABLE IF EXISTS grille_salaire;
DROP TABLE IF EXISTS contrat;
DROP TABLE IF EXISTS proposition;
DROP TABLE IF EXISTS compte;
DROP TABLE IF EXISTS candidat;
DROP TABLE IF EXISTS candidature;
DROP TABLE IF EXISTS offre;
DROP TABLE IF EXISTS besoin;
DROP TABLE IF EXISTS employe;
DROP TABLE IF EXISTS poste;
DROP TABLE IF EXISTS service;
DROP TABLE IF EXISTS departement;
DROP TABLE IF EXISTS statut_contrat;
DROP TABLE IF EXISTS type_contrat;
DROP TABLE IF EXISTS statut_besoin;
DROP TABLE IF EXISTS type_besoin;
DROP TABLE IF EXISTS statut_offre;
DROP TABLE IF EXISTS statut_candidature;
DROP TABLE IF EXISTS type_candidature;
DROP TABLE IF EXISTS statut_proposition;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
--  Tables de référence (statuts / types)
-- =====================================================================

CREATE TABLE statut_contrat (
    id_statut_contrat INT AUTO_INCREMENT PRIMARY KEY,
    libelle           VARCHAR(50) NOT NULL UNIQUE
) ENGINE = InnoDB;

CREATE TABLE type_contrat (
    id_type_contrat INT AUTO_INCREMENT PRIMARY KEY,
    libelle         VARCHAR(50) NOT NULL UNIQUE
) ENGINE = InnoDB;

CREATE TABLE statut_besoin (
    id_statut_besoin INT AUTO_INCREMENT PRIMARY KEY,
    libelle          VARCHAR(50) NOT NULL UNIQUE
) ENGINE = InnoDB;

CREATE TABLE type_besoin (
    id_type_besoin INT AUTO_INCREMENT PRIMARY KEY,
    libelle        VARCHAR(50) NOT NULL UNIQUE
) ENGINE = InnoDB;

CREATE TABLE statut_offre (
    id_statut_offre INT AUTO_INCREMENT PRIMARY KEY,
    libelle         VARCHAR(50) NOT NULL UNIQUE
) ENGINE = InnoDB;

CREATE TABLE statut_candidature (
    id_statut_candidature INT AUTO_INCREMENT PRIMARY KEY,
    libelle               VARCHAR(50) NOT NULL UNIQUE
) ENGINE = InnoDB;

CREATE TABLE type_candidature (
    id_type_candidature INT AUTO_INCREMENT PRIMARY KEY,
    libelle             VARCHAR(50) NOT NULL UNIQUE
) ENGINE = InnoDB;

CREATE TABLE statut_proposition (
    id_statut_proposition INT AUTO_INCREMENT PRIMARY KEY,
    libelle               VARCHAR(50) NOT NULL UNIQUE
) ENGINE = InnoDB;

-- =====================================================================
--  Structure entreprise
-- =====================================================================

CREATE TABLE departement (
    id_departement INT AUTO_INCREMENT PRIMARY KEY,
    libelle        VARCHAR(100) NOT NULL UNIQUE,
    id_manager     INT NULL            -- FK vers employe (ajoutée plus bas)
) ENGINE = InnoDB;

CREATE TABLE service (
    id_service     INT AUTO_INCREMENT PRIMARY KEY,
    libelle        VARCHAR(100) NOT NULL,
    id_departement INT NOT NULL,
    CONSTRAINT fk_service_departement
        FOREIGN KEY (id_departement) REFERENCES departement (id_departement)
) ENGINE = InnoDB;

CREATE TABLE poste (
    id_poste          INT AUTO_INCREMENT PRIMARY KEY,
    libelle           VARCHAR(100) NOT NULL,
    id_departement    INT NOT NULL,
    id_service        INT NULL,
    est_manager       BOOLEAN NOT NULL DEFAULT FALSE,
    est_directeur     BOOLEAN NOT NULL DEFAULT FALSE,
    est_occupe        BOOLEAN NOT NULL DEFAULT FALSE,
    est_actif         BOOLEAN NOT NULL DEFAULT TRUE,
    date_modification DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_poste_departement
        FOREIGN KEY (id_departement) REFERENCES departement (id_departement),
    CONSTRAINT fk_poste_service
        FOREIGN KEY (id_service) REFERENCES service (id_service)
) ENGINE = InnoDB;

CREATE TABLE employe (
    id_employe INT AUTO_INCREMENT PRIMARY KEY,
    nom        VARCHAR(100) NOT NULL,
    prenom     VARCHAR(100) NOT NULL,
    matricule  VARCHAR(30)  NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    id_poste   INT NULL,               -- poste actuel
    id_contrat INT NULL,               -- contrat actuel (FK ajoutée plus bas)
    CONSTRAINT fk_employe_poste
        FOREIGN KEY (id_poste) REFERENCES poste (id_poste)
) ENGINE = InnoDB;

-- =====================================================================
--  Mouvement dans l'entreprise (recrutement)
-- =====================================================================

CREATE TABLE besoin (
    id_besoin         INT AUTO_INCREMENT PRIMARY KEY,
    id_manager        INT NOT NULL,
    id_type_besoin    INT NOT NULL,
    id_statut_besoin  INT NOT NULL,
    id_departement    INT NOT NULL,
    id_service        INT NULL,
    intitule          VARCHAR(150) NOT NULL,
    nombre            INT NOT NULL DEFAULT 1,
    sexe              ENUM('H', 'F') NULL,   -- NULL = indifférent
    age_min           INT NULL,
    age_max           INT NULL,
    id_type_contrat   INT NOT NULL,
    date_commencement DATE NULL,
    budget            DECIMAL(15, 2) NULL,
    motif             TEXT NULL,
    date_modification DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_besoin_nombre CHECK (nombre > 0),
    CONSTRAINT chk_besoin_age CHECK (age_min IS NULL OR age_max IS NULL OR age_min <= age_max),
    CONSTRAINT fk_besoin_manager
        FOREIGN KEY (id_manager) REFERENCES employe (id_employe),
    CONSTRAINT fk_besoin_type_besoin
        FOREIGN KEY (id_type_besoin) REFERENCES type_besoin (id_type_besoin),
    CONSTRAINT fk_besoin_statut_besoin
        FOREIGN KEY (id_statut_besoin) REFERENCES statut_besoin (id_statut_besoin),
    CONSTRAINT fk_besoin_departement
        FOREIGN KEY (id_departement) REFERENCES departement (id_departement),
    CONSTRAINT fk_besoin_service
        FOREIGN KEY (id_service) REFERENCES service (id_service),
    CONSTRAINT fk_besoin_type_contrat
        FOREIGN KEY (id_type_contrat) REFERENCES type_contrat (id_type_contrat)
) ENGINE = InnoDB;

CREATE TABLE offre (
    id_offre          INT AUTO_INCREMENT PRIMARY KEY,
    id_besoin         INT NOT NULL,
    intitule          VARCHAR(150) NOT NULL,
    id_departement    INT NOT NULL,
    id_service        INT NULL,
    description       TEXT NULL,
    date_creation     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_fin          DATE NULL,
    date_modification DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    id_statut_offre   INT NOT NULL,
    CONSTRAINT fk_offre_besoin
        FOREIGN KEY (id_besoin) REFERENCES besoin (id_besoin),
    CONSTRAINT fk_offre_departement
        FOREIGN KEY (id_departement) REFERENCES departement (id_departement),
    CONSTRAINT fk_offre_service
        FOREIGN KEY (id_service) REFERENCES service (id_service),
    CONSTRAINT fk_offre_statut_offre
        FOREIGN KEY (id_statut_offre) REFERENCES statut_offre (id_statut_offre)
) ENGINE = InnoDB;

CREATE TABLE candidature (
    id_candidature        INT AUTO_INCREMENT PRIMARY KEY,
    id_compte             INT NULL,    -- FK vers compte (ajoutée plus bas)
    date_candidature      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_departement        INT NOT NULL,
    id_employe            INT NULL,    -- renseigné si candidat interne / ancien employé
    id_offre              INT NOT NULL,
    id_statut_candidature INT NOT NULL,
    id_type_candidature   INT NOT NULL,
    id_type_contrat       INT NOT NULL,
    date_test_technique   DATETIME NULL,
    date_entretien        DATETIME NULL,
    date_modification     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_candidature_departement
        FOREIGN KEY (id_departement) REFERENCES departement (id_departement),
    CONSTRAINT fk_candidature_employe
        FOREIGN KEY (id_employe) REFERENCES employe (id_employe),
    CONSTRAINT fk_candidature_offre
        FOREIGN KEY (id_offre) REFERENCES offre (id_offre),
    CONSTRAINT fk_candidature_statut
        FOREIGN KEY (id_statut_candidature) REFERENCES statut_candidature (id_statut_candidature),
    CONSTRAINT fk_candidature_type
        FOREIGN KEY (id_type_candidature) REFERENCES type_candidature (id_type_candidature),
    CONSTRAINT fk_candidature_type_contrat
        FOREIGN KEY (id_type_contrat) REFERENCES type_contrat (id_type_contrat)
) ENGINE = InnoDB;

CREATE TABLE candidat (
    id_candidat INT AUTO_INCREMENT PRIMARY KEY,
    nom         VARCHAR(100) NOT NULL,
    prenom      VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    telephone   VARCHAR(30)  NULL
) ENGINE = InnoDB;

-- Compte de connexion : employé (identifiant = matricule) OU candidat (identifiant = e-mail)
CREATE TABLE compte (
    id_compte     INT AUTO_INCREMENT PRIMARY KEY,
    identifiant   VARCHAR(150) NOT NULL UNIQUE,
    mot_de_passe  VARCHAR(255) NOT NULL,  -- chiffré (BCrypt)
    role          VARCHAR(20)  NOT NULL,  -- CANDIDAT, EMPLOYE, MANAGER, RH, FINANCE, ADMIN
    id_employe    INT NULL,
    id_candidat   INT NULL,
    actif         BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_compte_personne CHECK (id_employe IS NULL OR id_candidat IS NULL),
    CONSTRAINT fk_compte_employe
        FOREIGN KEY (id_employe) REFERENCES employe (id_employe),
    CONSTRAINT fk_compte_candidat
        FOREIGN KEY (id_candidat) REFERENCES candidat (id_candidat)
) ENGINE = InnoDB;

CREATE TABLE proposition (
    id_proposition        INT AUTO_INCREMENT PRIMARY KEY,
    id_offre              INT NOT NULL,
    salaire               DECIMAL(15, 2) NOT NULL,
    id_type_contrat       INT NOT NULL,
    date_commencement     DATE NOT NULL,
    date_fin              DATE NULL,   -- NULL pour un CDI
    date_limite_validation DATE NULL,
    id_statut_proposition INT NOT NULL,
    date_modification     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_proposition_dates CHECK (date_fin IS NULL OR date_fin >= date_commencement),
    CONSTRAINT fk_proposition_offre
        FOREIGN KEY (id_offre) REFERENCES offre (id_offre),
    CONSTRAINT fk_proposition_type_contrat
        FOREIGN KEY (id_type_contrat) REFERENCES type_contrat (id_type_contrat),
    CONSTRAINT fk_proposition_statut
        FOREIGN KEY (id_statut_proposition) REFERENCES statut_proposition (id_statut_proposition)
) ENGINE = InnoDB;

CREATE TABLE contrat (
    id_contrat        INT AUTO_INCREMENT PRIMARY KEY,
    id_employe        INT NOT NULL,
    id_proposition    INT NULL,
    id_statut_contrat INT NOT NULL,
    id_type_contrat   INT NOT NULL,
    CONSTRAINT fk_contrat_employe
        FOREIGN KEY (id_employe) REFERENCES employe (id_employe),
    CONSTRAINT fk_contrat_proposition
        FOREIGN KEY (id_proposition) REFERENCES proposition (id_proposition),
    CONSTRAINT fk_contrat_statut
        FOREIGN KEY (id_statut_contrat) REFERENCES statut_contrat (id_statut_contrat),
    CONSTRAINT fk_contrat_type
        FOREIGN KEY (id_type_contrat) REFERENCES type_contrat (id_type_contrat)
) ENGINE = InnoDB;

CREATE TABLE historique_employe (
    id_historique_employe INT AUTO_INCREMENT PRIMARY KEY,
    id_employe            INT NOT NULL,
    id_poste              INT NOT NULL,
    id_contrat            INT NULL,
    date                  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_historique_employe
        FOREIGN KEY (id_employe) REFERENCES employe (id_employe),
    CONSTRAINT fk_historique_poste
        FOREIGN KEY (id_poste) REFERENCES poste (id_poste),
    CONSTRAINT fk_historique_contrat
        FOREIGN KEY (id_contrat) REFERENCES contrat (id_contrat)
) ENGINE = InnoDB;

CREATE TABLE grille_salaire (
    id_grille_salaire INT AUTO_INCREMENT PRIMARY KEY,
    valeur            DECIMAL(15, 2) NOT NULL,
    id_poste          INT NOT NULL,
    anciennete_min    INT NOT NULL DEFAULT 0,   -- en années
    anciennete_max    INT NULL,                 -- NULL = pas de plafond
    CONSTRAINT chk_grille_anciennete CHECK (anciennete_max IS NULL OR anciennete_min <= anciennete_max),
    CONSTRAINT fk_grille_poste
        FOREIGN KEY (id_poste) REFERENCES poste (id_poste)
) ENGINE = InnoDB;

-- =====================================================================
--  Journal d'audit (BK0) : une ligne par action, jamais effacée
-- =====================================================================

CREATE TABLE journal_audit (
    id_journal_audit INT AUTO_INCREMENT PRIMARY KEY,
    date_action      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    auteur           VARCHAR(150) NOT NULL,  -- identifiant du compte, ou 'systeme'
    action           VARCHAR(50)  NOT NULL,
    objet            VARCHAR(50)  NOT NULL,
    id_objet         INT          NULL,
    detail           VARCHAR(500) NULL,
    INDEX idx_journal_audit_date (date_action)
) ENGINE = InnoDB;

-- =====================================================================
--  Clés étrangères circulaires (ajoutées après création des tables)
-- =====================================================================

ALTER TABLE departement
    ADD CONSTRAINT fk_departement_manager
        FOREIGN KEY (id_manager) REFERENCES employe (id_employe);

ALTER TABLE employe
    ADD CONSTRAINT fk_employe_contrat
        FOREIGN KEY (id_contrat) REFERENCES contrat (id_contrat);

ALTER TABLE candidature
    ADD CONSTRAINT fk_candidature_compte
        FOREIGN KEY (id_compte) REFERENCES compte (id_compte);

-- =====================================================================
--  Données de référence
-- =====================================================================

INSERT INTO statut_contrat (libelle) VALUES
    ('cree'), ('signe'), ('valide'), ('annule'), ('rompu');

INSERT INTO type_contrat (libelle) VALUES
    ('CE'), ('CDD'), ('CDI');

INSERT INTO statut_besoin (libelle) VALUES
    ('cree'), ('envoie finance'), ('valide finance'), ('refuse finance');

INSERT INTO type_besoin (libelle) VALUES
    ('creation poste'), ('recherche personnel'), ('recherche stagiaire');

INSERT INTO statut_offre (libelle) VALUES
    ('enregistre'), ('cree'), ('publie'), ('fin');

INSERT INTO statut_candidature (libelle) VALUES
    ('cree'), ('selectionne'), ('teste techniquement'), ('entretien ok'), ('rejete');

INSERT INTO type_candidature (libelle) VALUES
    ('employe'), ('externe'), ('ancienne relation');

INSERT INTO statut_proposition (libelle) VALUES
    ('valide'), ('refuse'), ('en attente');
