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
DROP TABLE IF EXISTS document;
DROP TABLE IF EXISTS onboarding_tache;
DROP TABLE IF EXISTS onboarding;
DROP TABLE IF EXISTS historique_employe;
DROP TABLE IF EXISTS grille_salaire;
DROP TABLE IF EXISTS contrat;
DROP TABLE IF EXISTS proposition;
DROP TABLE IF EXISTS evaluation_candidat;
DROP TABLE IF EXISTS entretien;
DROP TABLE IF EXISTS compte;
DROP TABLE IF EXISTS candidat;
DROP TABLE IF EXISTS candidature;
DROP TABLE IF EXISTS offre;
DROP TABLE IF EXISTS validation;
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
    id_poste          INT NULL,              -- poste concerné ou créé
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
    CONSTRAINT fk_besoin_poste
        FOREIGN KEY (id_poste) REFERENCES poste (id_poste),
    CONSTRAINT fk_besoin_type_contrat
        FOREIGN KEY (id_type_contrat) REFERENCES type_contrat (id_type_contrat)
) ENGINE = InnoDB;

-- Circuit de validation d'un besoin : une ligne par étape (envoi RH, finance…)
CREATE TABLE validation (
    id_validation   INT AUTO_INCREMENT PRIMARY KEY,
    id_besoin       INT NOT NULL,
    etape           VARCHAR(100) NOT NULL,   -- ex. envoye_rh, envoye_finance
    decision        VARCHAR(50)  NULL,       -- ex. approuve, refuse, completer
    commentaire     VARCHAR(1000) NULL,
    date_validation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_employe      INT NULL,                -- employé qui a validé
    CONSTRAINT fk_validation_besoin
        FOREIGN KEY (id_besoin) REFERENCES besoin (id_besoin),
    CONSTRAINT fk_validation_employe
        FOREIGN KEY (id_employe) REFERENCES employe (id_employe)
) ENGINE = InnoDB;

-- Offre d'emploi (BK2) : vient d'un besoin validé ; visible au public si « publiée » et non expirée
CREATE TABLE offre (
    id_offre          INT AUTO_INCREMENT PRIMARY KEY,
    id_besoin         INT NULL,
    id_poste          INT NULL,
    id_departement    INT NULL,
    titre             VARCHAR(150) NOT NULL,
    description       TEXT NULL,
    profil            TEXT NULL,
    id_type_contrat   INT NULL,
    lieu              VARCHAR(150) NULL,
    date_publication  DATE NULL,
    date_limite       DATE NULL,            -- NULL = pas de date limite
    id_statut_offre   INT NOT NULL,
    date_creation     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modification DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_offre_besoin
        FOREIGN KEY (id_besoin) REFERENCES besoin (id_besoin),
    CONSTRAINT fk_offre_poste
        FOREIGN KEY (id_poste) REFERENCES poste (id_poste),
    CONSTRAINT fk_offre_departement
        FOREIGN KEY (id_departement) REFERENCES departement (id_departement),
    CONSTRAINT fk_offre_type_contrat
        FOREIGN KEY (id_type_contrat) REFERENCES type_contrat (id_type_contrat),
    CONSTRAINT fk_offre_statut
        FOREIGN KEY (id_statut_offre) REFERENCES statut_offre (id_statut_offre)
) ENGINE = InnoDB;

-- CANDIDAT ≠ EMPLOYÉ : une personne peut avoir plusieurs candidatures avant d'être embauchée
CREATE TABLE candidat (
    id_candidat INT AUTO_INCREMENT PRIMARY KEY,
    nom         VARCHAR(100) NOT NULL,
    prenom      VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    telephone   VARCHAR(30)  NULL
) ENGINE = InnoDB;

-- Une seule candidature par candidat et par offre
CREATE TABLE candidature (
    id_candidature        INT AUTO_INCREMENT PRIMARY KEY,
    id_candidat           INT NOT NULL,
    id_offre              INT NOT NULL,
    date_candidature      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lettre                TEXT NULL,
    id_document_cv        INT NULL,     -- document de la GED (BK0-05), vide tant qu'elle n'est pas prête
    id_statut_candidature INT NOT NULL,
    id_type_candidature   INT NULL,
    motif_rejet           VARCHAR(500) NULL,
    date_modification     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_candidature_candidat_offre UNIQUE (id_candidat, id_offre),
    CONSTRAINT fk_candidature_candidat
        FOREIGN KEY (id_candidat) REFERENCES candidat (id_candidat),
    CONSTRAINT fk_candidature_offre
        FOREIGN KEY (id_offre) REFERENCES offre (id_offre),
    CONSTRAINT fk_candidature_statut
        FOREIGN KEY (id_statut_candidature) REFERENCES statut_candidature (id_statut_candidature),
    CONSTRAINT fk_candidature_type
        FOREIGN KEY (id_type_candidature) REFERENCES type_candidature (id_type_candidature)
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

CREATE TABLE entretien (
    id_entretien   INT AUTO_INCREMENT PRIMARY KEY,
    id_candidature INT NOT NULL,
    type           VARCHAR(20)  NOT NULL,                   -- TELEPHONIQUE, PHYSIQUE, VISIO
    date_entretien DATETIME     NOT NULL,
    lieu           VARCHAR(200) NULL,                       -- salle ou lien visio
    id_evaluateur  INT NULL,                                -- compte de l'évaluateur
    statut         VARCHAR(20)  NOT NULL DEFAULT 'PLANIFIE', -- PLANIFIE, REALISE, ANNULE
    CONSTRAINT fk_entretien_candidature
        FOREIGN KEY (id_candidature) REFERENCES candidature (id_candidature),
    CONSTRAINT fk_entretien_evaluateur
        FOREIGN KEY (id_evaluateur) REFERENCES compte (id_compte)
) ENGINE = InnoDB;

-- Une évaluation par évaluateur et par entretien
CREATE TABLE evaluation_candidat (
    id_evaluation   INT AUTO_INCREMENT PRIMARY KEY,
    id_entretien    INT NOT NULL,
    note            INT NOT NULL,                           -- sur 20
    avis            VARCHAR(20) NOT NULL,                   -- FAVORABLE, RESERVE, DEFAVORABLE
    commentaire     TEXT NULL,
    id_evaluateur   INT NOT NULL,
    date_evaluation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_evaluation_note CHECK (note BETWEEN 0 AND 20),
    CONSTRAINT uq_evaluation_entretien_evaluateur UNIQUE (id_entretien, id_evaluateur),
    CONSTRAINT fk_evaluation_entretien
        FOREIGN KEY (id_entretien) REFERENCES entretien (id_entretien),
    CONSTRAINT fk_evaluation_evaluateur
        FOREIGN KEY (id_evaluateur) REFERENCES compte (id_compte)
) ENGINE = InnoDB;

CREATE TABLE proposition (
    id_proposition        INT AUTO_INCREMENT PRIMARY KEY,
    id_offre              INT NOT NULL,
    id_candidature        INT NULL,    -- candidature retenue
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
    CONSTRAINT fk_proposition_candidature
        FOREIGN KEY (id_candidature) REFERENCES candidature (id_candidature),
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
    date_debut        DATE NULL,
    date_fin          DATE NULL,   -- NULL pour un CDI
    salaire           DECIMAL(15, 2) NULL,
    id_contrat_precedent INT NULL, -- renouvellement / avenant
    CONSTRAINT chk_contrat_dates CHECK (date_fin IS NULL OR date_debut IS NULL OR date_fin >= date_debut),
    CONSTRAINT fk_contrat_employe
        FOREIGN KEY (id_employe) REFERENCES employe (id_employe),
    CONSTRAINT fk_contrat_proposition
        FOREIGN KEY (id_proposition) REFERENCES proposition (id_proposition),
    CONSTRAINT fk_contrat_statut
        FOREIGN KEY (id_statut_contrat) REFERENCES statut_contrat (id_statut_contrat),
    CONSTRAINT fk_contrat_type
        FOREIGN KEY (id_type_contrat) REFERENCES type_contrat (id_type_contrat),
    CONSTRAINT fk_contrat_precedent
        FOREIGN KEY (id_contrat_precedent) REFERENCES contrat (id_contrat)
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
--  Onboarding (BK4) et documents (GED, BK0-05)
-- =====================================================================

CREATE TABLE onboarding (
    id_onboarding INT AUTO_INCREMENT PRIMARY KEY,
    id_employe    INT NOT NULL,
    date_debut    DATE NULL,
    statut        VARCHAR(20) NOT NULL DEFAULT 'EN_COURS', -- EN_COURS, TERMINE, ABANDONNE
    progression   INT NOT NULL DEFAULT 0,                  -- en %
    CONSTRAINT chk_onboarding_progression CHECK (progression BETWEEN 0 AND 100),
    CONSTRAINT fk_onboarding_employe
        FOREIGN KEY (id_employe) REFERENCES employe (id_employe)
) ENGINE = InnoDB;

CREATE TABLE onboarding_tache (
    id_onboarding_tache INT AUTO_INCREMENT PRIMARY KEY,
    id_onboarding       INT NOT NULL,
    libelle             VARCHAR(150) NOT NULL,
    categorie           VARCHAR(30)  NULL,                   -- CONTRAT_SIGNE, BADGE, COMPTE_INFORMATIQUE…
    id_responsable      INT NULL,
    echeance            DATE NULL,
    statut              VARCHAR(20)  NOT NULL DEFAULT 'A_FAIRE', -- A_FAIRE, FAIT, NON_APPLICABLE
    CONSTRAINT fk_onboarding_tache_onboarding
        FOREIGN KEY (id_onboarding) REFERENCES onboarding (id_onboarding),
    CONSTRAINT fk_onboarding_tache_responsable
        FOREIGN KEY (id_responsable) REFERENCES employe (id_employe)
) ENGINE = InnoDB;

-- Document rattaché à un candidat, un employé, un contrat ou une tâche d'onboarding
CREATE TABLE document (
    id_document         INT AUTO_INCREMENT PRIMARY KEY,
    type                VARCHAR(30)  NOT NULL,   -- CV, CONTRAT_SIGNE, PIECE_IDENTITE, JUSTIFICATIF_DOMICILE, DIPLOME, AUTRE
    nom_fichier         VARCHAR(255) NULL,
    fichier             LONGBLOB     NULL,
    date_document       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_candidat         INT NULL,
    id_employe          INT NULL,
    id_contrat          INT NULL,
    id_onboarding_tache INT NULL,
    CONSTRAINT fk_document_candidat
        FOREIGN KEY (id_candidat) REFERENCES candidat (id_candidat),
    CONSTRAINT fk_document_employe
        FOREIGN KEY (id_employe) REFERENCES employe (id_employe),
    CONSTRAINT fk_document_contrat
        FOREIGN KEY (id_contrat) REFERENCES contrat (id_contrat),
    CONSTRAINT fk_document_onboarding_tache
        FOREIGN KEY (id_onboarding_tache) REFERENCES onboarding_tache (id_onboarding_tache)
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
    ADD CONSTRAINT fk_candidature_document_cv
        FOREIGN KEY (id_document_cv) REFERENCES document (id_document);

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
    ('brouillon'), ('publiée'), ('clôturée'), ('pourvue'), ('annulée');

INSERT INTO statut_candidature (libelle) VALUES
    ('reçue'), ('présélectionnée'), ('en entretien'), ('retenue'), ('rejetée'), ('retirée'), ('embauchée');

INSERT INTO type_candidature (libelle) VALUES
    ('employe'), ('externe'), ('ancienne relation');

INSERT INTO statut_proposition (libelle) VALUES
    ('valide'), ('refuse'), ('en attente');
