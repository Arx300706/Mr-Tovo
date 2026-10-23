-- Base complète autonome Mr Tovo (PostgreSQL).
-- Contient le schéma courant et les données de référence/démonstration.
-- IMPORTANT : DROP SCHEMA public CASCADE efface les tables et données existantes.
-- Cible attendue : base rh (voir application.properties).

-- =================================================================
-- Mr Tovo : application de gestion RH — schéma PostgreSQL
--
-- Installation (base « rh ») :
--   createdb -h localhost -U postgres rh
--   psql -h localhost -U postgres -d rh -f src/main/resources/db/schema.sql
--   psql -h localhost -U postgres -d rh -f src/main/resources/db/data.sql
--
-- ATTENTION : ce script supprime puis recrée toutes les tables.
-- =================================================================

DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;

-- -----------------------------------------------------------------
-- 0. TRAÇABILITÉ : date_modification mise à jour à chaque UPDATE
--    (équivalent PostgreSQL du « ON UPDATE CURRENT_TIMESTAMP » MySQL)
-- -----------------------------------------------------------------
CREATE OR REPLACE FUNCTION maj_date_modification() RETURNS TRIGGER AS $$
BEGIN
    NEW.date_modification = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- -----------------------------------------------------------------
-- 1. TABLES DE RÉFÉRENCE (LOOKUPS)
-- -----------------------------------------------------------------
CREATE TABLE statut_besoin (
    id_statut_besoin SERIAL PRIMARY KEY,
    libelle          VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE type_besoin (
    id_type_besoin SERIAL PRIMARY KEY,
    libelle        VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE statut_offre (
    id_statut_offre SERIAL PRIMARY KEY,
    libelle         VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE statut_candidature (
    id_statut_candidature SERIAL PRIMARY KEY,
    libelle               VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE type_candidature (
    id_type_candidature SERIAL PRIMARY KEY,
    libelle             VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE statut_contrat (
    id_statut_contrat SERIAL PRIMARY KEY,
    libelle           VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE type_contrat (
    id_type_contrat SERIAL PRIMARY KEY,
    libelle         VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE statut_proposition (
    id_statut_proposition SERIAL PRIMARY KEY,
    libelle               VARCHAR(100) NOT NULL UNIQUE
);

-- Adresse structurée de résidence (évite les saisies libres incohérentes).
CREATE TABLE ville (
    id_ville SERIAL PRIMARY KEY,
    libelle VARCHAR(150) NOT NULL UNIQUE
);
CREATE TABLE commune (
    id_commune SERIAL PRIMARY KEY,
    id_ville INT NOT NULL REFERENCES ville (id_ville),
    libelle VARCHAR(150) NOT NULL,
    UNIQUE (id_ville, libelle)
);
CREATE TABLE fokontany (
    id_fokontany SERIAL PRIMARY KEY,
    id_commune INT NOT NULL REFERENCES commune (id_commune),
    libelle VARCHAR(150) NOT NULL,
    UNIQUE (id_commune, libelle)
);

-- -----------------------------------------------------------------
-- 2. STRUCTURE ENTREPRISE
-- -----------------------------------------------------------------
CREATE TABLE departement (
    id_departement    SERIAL PRIMARY KEY,
    code              VARCHAR(20)  NOT NULL UNIQUE, -- DIR, RH, FIN, ... : sert à donner les droits
    libelle           VARCHAR(150) NOT NULL,
    id_manager_actuel INT NULL                      -- FK vers employe_compte (ajoutée plus bas)
);

CREATE TABLE service (
    id_service     SERIAL PRIMARY KEY,
    libelle        VARCHAR(150) NOT NULL,
    id_departement INT NOT NULL REFERENCES departement (id_departement)
);

CREATE TABLE poste (
    id_poste          SERIAL PRIMARY KEY,
    libelle           VARCHAR(150) NOT NULL,
    id_departement    INT REFERENCES departement (id_departement),
    id_service        INT REFERENCES service (id_service),
    est_manager       BOOLEAN   NOT NULL DEFAULT FALSE,
    est_directeur     BOOLEAN   NOT NULL DEFAULT FALSE,
    est_occupe        BOOLEAN   NOT NULL DEFAULT FALSE,
    est_actif         BOOLEAN   NOT NULL DEFAULT TRUE,
    date_modification TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE grille_salaire (
    id_grille_salaire SERIAL PRIMARY KEY,
    valeur            NUMERIC(12, 2) NOT NULL CHECK (valeur > 0),
    id_poste          INT NOT NULL REFERENCES poste (id_poste),
    anciennete_min    INT NOT NULL DEFAULT 0,
    anciennete_max    INT NOT NULL DEFAULT 0
);

-- Budget annuel de l'entreprise (étape Finance)
CREATE TABLE budget_annuel (
    id_budget_annuel SERIAL PRIMARY KEY,
    annee            INT            NOT NULL UNIQUE,
    montant          NUMERIC(15, 2) NOT NULL CHECK (montant >= 0)
);

-- Employé = compte de connexion par matricule
CREATE TABLE employe_compte (
    id_employe        SERIAL PRIMARY KEY,
    nom               VARCHAR(100) NOT NULL,
    prenom            VARCHAR(100) NOT NULL,
    matricule         VARCHAR(50)  NOT NULL UNIQUE,
    email             VARCHAR(150),
    id_poste_actuel   INT NULL REFERENCES poste (id_poste),
    id_contrat_actuel INT NULL,                     -- FK vers contrat (ajoutée plus bas)
    mot_de_passe      VARCHAR(255) NOT NULL,        -- BCrypt
    date_embauche     DATE,
    date_modification TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE departement
    ADD CONSTRAINT fk_departement_manager FOREIGN KEY (id_manager_actuel) REFERENCES employe_compte (id_employe);

-- -----------------------------------------------------------------
-- 3. BESOINS ET OFFRES D'EMPLOI
-- -----------------------------------------------------------------
CREATE TABLE besoin (
    id_besoin           SERIAL PRIMARY KEY,
    id_manager          INT          NOT NULL REFERENCES employe_compte (id_employe),
    id_type_besoin      INT          NOT NULL REFERENCES type_besoin (id_type_besoin),
    id_statut_besoin    INT          NOT NULL REFERENCES statut_besoin (id_statut_besoin),
    id_departement      INT          NOT NULL REFERENCES departement (id_departement),
    id_service          INT          NOT NULL REFERENCES service (id_service),
    id_poste            INT NULL REFERENCES poste (id_poste), -- poste existant visé (recherche personnel / stagiaire)
    intitule            VARCHAR(150) NOT NULL,
    nombre              INT          NOT NULL CHECK (nombre > 0),
    sexe                VARCHAR(20),                          -- Homme / Femme / Indifférent
    age_min             INT,
    age_max             INT,
    id_type_contrat     INT          NOT NULL REFERENCES type_contrat (id_type_contrat),
    date_commencement   DATE,
    budget              NUMERIC(12, 2),                       -- budget mensuel estimé par le manager
    motif               TEXT,
    -- Analyse RH (étape 3)
    salaire_mensuel     NUMERIC(12, 2),                       -- salaire retenu d'après la grille salariale
    commentaire_rh      TEXT,
    -- Décision Finance (étape 4)
    depense_annuelle    NUMERIC(15, 2),                       -- nombre x salaire_mensuel x 12
    annee_budget        INT,
    commentaire_finance TEXT,
    date_creation       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modification   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (age_min IS NULL OR age_max IS NULL OR age_min <= age_max)
);

CREATE TABLE offre_emploi (
    id_offre          SERIAL PRIMARY KEY,
    id_besoin         INT          NOT NULL UNIQUE REFERENCES besoin (id_besoin), -- une offre par besoin
    intitule          VARCHAR(150) NOT NULL,
    id_departement    INT          NOT NULL REFERENCES departement (id_departement),
    id_service        INT          NOT NULL REFERENCES service (id_service),
    description       TEXT,
    profil            TEXT,                                -- compétences / profil recherché
    date_creation     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_fin          DATE,                                -- date limite de candidature
    date_modification TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_statut_offre   INT          NOT NULL REFERENCES statut_offre (id_statut_offre)
);

-- -----------------------------------------------------------------
-- 4. CANDIDATURES ET COMPTES CANDIDATS
-- -----------------------------------------------------------------
-- Compte du candidat (employé, externe ou ancienne relation), identifiant = e-mail.
-- Un compte peut porter plusieurs candidatures (candidature.id_compte).
CREATE TABLE compte (
    id_compte      SERIAL PRIMARY KEY,
    email          VARCHAR(150) NOT NULL UNIQUE,
    mot_de_passe   VARCHAR(255) NOT NULL,                  -- BCrypt
    nom            VARCHAR(100) NOT NULL,
    prenom         VARCHAR(100) NOT NULL,
    telephone      VARCHAR(30),
    date_naissance DATE,
    sexe           VARCHAR(20),
    adresse        VARCHAR(255),
    ville          VARCHAR(100),
    id_ville       INT REFERENCES ville (id_ville),
    id_commune     INT REFERENCES commune (id_commune),
    id_fokontany   INT REFERENCES fokontany (id_fokontany),
    lot             VARCHAR(100),
    date_creation  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE candidature (
    id_candidature        SERIAL PRIMARY KEY,
    id_compte             INT       NOT NULL REFERENCES compte (id_compte),
    date_candidature      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_departement        INT       NOT NULL REFERENCES departement (id_departement),
    id_employe            INT NULL REFERENCES employe_compte (id_employe), -- si employé / ancienne relation
    id_offre              INT       NOT NULL REFERENCES offre_emploi (id_offre),
    id_statut_candidature INT       NOT NULL REFERENCES statut_candidature (id_statut_candidature),
    id_type_candidature   INT       NOT NULL REFERENCES type_candidature (id_type_candidature),
    id_type_contrat       INT       NOT NULL REFERENCES type_contrat (id_type_contrat),
    diplome               VARCHAR(150),
    annees_experience     INT,
    lettre_motivation     TEXT,
    experience_professionnelle TEXT,              -- parcours professionnel détaillé (facultatif)
    metier_recherche       VARCHAR(150),
    presentation           TEXT,
    formations             TEXT,
    competences            TEXT,
    qualites               TEXT,
    langues                TEXT,
    projets                TEXT,
    certifications         TEXT,
    centres_interet        TEXT,
    linkedin               VARCHAR(255),
    github                 VARCHAR(255),
    portfolio              VARCHAR(255),
    photo                   BYTEA,
    photo_type              VARCHAR(50),
    date_test_technique   TIMESTAMP NULL,
    date_entretien        TIMESTAMP NULL,
    commentaire_rh        TEXT,
    date_modification     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (id_compte, id_offre)                         -- une seule candidature par offre et par compte
);

-- CV déposés avec une candidature (un enregistrement par dépôt)
CREATE TABLE cv (
    id_cv           SERIAL PRIMARY KEY,
    id_candidature  INT          NOT NULL REFERENCES candidature (id_candidature),
    nom_fichier     VARCHAR(255) NOT NULL,
    fichier         BYTEA        NOT NULL,                      -- PDF
    date_depot      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_employe      INT NULL REFERENCES employe_compte (id_employe), -- employé RH qui a traité le CV
    date_traitement TIMESTAMP NULL
);

-- Rubriques répétables d'un CV : candidat ou personne déjà employée.
CREATE TABLE cv_element (
    id_cv_element  SERIAL PRIMARY KEY,
    id_candidature INT NULL REFERENCES candidature (id_candidature) ON DELETE CASCADE,
    id_employe     INT NULL REFERENCES employe_compte (id_employe) ON DELETE CASCADE,
    type           VARCHAR(30)  NOT NULL CHECK (type IN ('EXPERIENCE','FORMATION','COMPETENCE','QUALITE','LANGUE','PROJET','CERTIFICATION','INTERET','LIEN')),
    titre          VARCHAR(255) NOT NULL,
    sous_titre     VARCHAR(255),
    date_debut     VARCHAR(20),
    date_fin       VARCHAR(20),
    description    TEXT,
    niveau         VARCHAR(100),
    url            VARCHAR(500),
    ordre          INT NOT NULL DEFAULT 0,
    CHECK ((id_candidature IS NOT NULL)::int + (id_employe IS NOT NULL)::int = 1)
);
CREATE INDEX idx_cv_element_candidature ON cv_element (id_candidature, type, ordre);
CREATE INDEX idx_cv_element_employe ON cv_element (id_employe, type, ordre);

-- Documents administratifs des employés déjà présents (CV ou ancien contrat numérisé).
CREATE TABLE document_employe (
    id_document  SERIAL PRIMARY KEY,
    id_employe   INT NOT NULL REFERENCES employe_compte (id_employe) ON DELETE CASCADE,
    type         VARCHAR(20) NOT NULL CHECK (type IN ('CV','CONTRAT')),
    nom_fichier  VARCHAR(255) NOT NULL,
    fichier      BYTEA NOT NULL,
    mime_type    VARCHAR(100) NOT NULL DEFAULT 'application/pdf',
    date_document DATE,
    date_depot   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description TEXT
);

-- CIN, passeport ou permis lié au CV d'un candidat ou d'un employé.
CREATE TABLE document_identite (
    id_document_identite SERIAL PRIMARY KEY,
    id_candidature INT UNIQUE REFERENCES candidature (id_candidature) ON DELETE CASCADE,
    id_employe INT UNIQUE REFERENCES employe_compte (id_employe) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL CHECK (type IN ('CIN','PASSEPORT','PERMIS')),
    nom_fichier VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    fichier BYTEA NOT NULL,
    date_depot TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK ((id_candidature IS NOT NULL)::int + (id_employe IS NOT NULL)::int = 1)
);

-- -----------------------------------------------------------------
-- 5. PROPOSITIONS, CONTRATS ET EMPLOYÉS
-- -----------------------------------------------------------------
CREATE TABLE proposition (
    id_proposition         SERIAL PRIMARY KEY,
    id_offre               INT            NOT NULL REFERENCES offre_emploi (id_offre),
    id_candidature         INT            NOT NULL REFERENCES candidature (id_candidature),
    id_poste               INT            NOT NULL REFERENCES poste (id_poste), -- poste qui sera occupé
    salaire                NUMERIC(12, 2) NOT NULL CHECK (salaire > 0),
    id_type_contrat        INT            NOT NULL REFERENCES type_contrat (id_type_contrat),
    date_commencement      DATE           NOT NULL,
    date_fin               DATE,                                               -- vide pour un CDI
    date_limite_validation DATE           NOT NULL,
    id_statut_proposition  INT            NOT NULL REFERENCES statut_proposition (id_statut_proposition),
    date_creation          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modification      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Réponse du candidat, champ par champ (Oui / Non + valeur souhaitée si Non)
CREATE TABLE reponse_proposition (
    id_reponse_proposition     SERIAL PRIMARY KEY,
    id_proposition             INT       NOT NULL REFERENCES proposition (id_proposition),
    accepte_salaire            BOOLEAN   NOT NULL,
    salaire_souhaite           NUMERIC(12, 2),
    accepte_type_contrat       BOOLEAN   NOT NULL,
    id_type_contrat_souhaite   INT REFERENCES type_contrat (id_type_contrat),
    accepte_date_commencement  BOOLEAN   NOT NULL,
    date_commencement_souhaite DATE,
    accepte_date_fin           BOOLEAN,                 -- NULL si la proposition n'a pas de date de fin
    date_fin_souhaite          DATE,
    commentaire                TEXT,
    date_reponse               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Discussion RH <-> candidat autour d'une proposition
CREATE TABLE message_proposition (
    id_message     SERIAL PRIMARY KEY,
    id_proposition INT         NOT NULL REFERENCES proposition (id_proposition),
    auteur         VARCHAR(20) NOT NULL CHECK (auteur IN ('CANDIDAT', 'RH')),
    id_employe     INT REFERENCES employe_compte (id_employe), -- employé RH auteur
    contenu        TEXT        NOT NULL,
    date_envoi     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE contrat (
    id_contrat              SERIAL PRIMARY KEY,
    id_employe              INT NULL REFERENCES employe_compte (id_employe), -- rempli à la validation
    id_candidature          INT            NOT NULL REFERENCES candidature (id_candidature),
    id_proposition          INT            NOT NULL UNIQUE REFERENCES proposition (id_proposition),
    id_statut_contrat       INT            NOT NULL REFERENCES statut_contrat (id_statut_contrat),
    id_type_contrat         INT            NOT NULL REFERENCES type_contrat (id_type_contrat),
    id_poste                INT            NOT NULL REFERENCES poste (id_poste),
    salaire                 NUMERIC(12, 2) NOT NULL,
    date_debut              DATE           NOT NULL,
    date_fin                DATE,
    date_creation           TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_signature_candidat TIMESTAMP,
    date_signature_rh       TIMESTAMP,
    date_modification       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE employe_compte
    ADD CONSTRAINT fk_employe_contrat FOREIGN KEY (id_contrat_actuel) REFERENCES contrat (id_contrat);

CREATE TABLE historique_employe (
    id_historique_employe SERIAL PRIMARY KEY,
    id_employe            INT       NOT NULL REFERENCES employe_compte (id_employe),
    id_poste              INT       NOT NULL REFERENCES poste (id_poste),
    id_contrat            INT       NOT NULL REFERENCES contrat (id_contrat),
    date                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------------
-- 6. TRANSVERSAL : historique des statuts et notifications
-- -----------------------------------------------------------------
-- Chaque changement de statut (besoin, offre, candidature, proposition, contrat) est tracé ici
CREATE TABLE historique_statut (
    id_historique_statut SERIAL PRIMARY KEY,
    entite               VARCHAR(30)  NOT NULL, -- BESOIN, OFFRE, CANDIDATURE, PROPOSITION, CONTRAT
    id_entite            INT          NOT NULL,
    ancien_statut        VARCHAR(100),
    nouveau_statut       VARCHAR(100) NOT NULL,
    auteur               VARCHAR(150),
    commentaire          TEXT,
    date_changement      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_historique_statut_entite ON historique_statut (entite, id_entite);

-- Messages de l'entreprise vers un candidat (id_compte) ou un employé (id_employe)
CREATE TABLE notification (
    id_notification SERIAL PRIMARY KEY,
    id_compte       INT REFERENCES compte (id_compte),
    id_employe      INT REFERENCES employe_compte (id_employe),
    message         VARCHAR(500) NOT NULL,
    lien            VARCHAR(255),
    lue             BOOLEAN      NOT NULL DEFAULT FALSE,
    date_creation   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (id_compte IS NOT NULL OR id_employe IS NOT NULL)
);

-- -----------------------------------------------------------------
-- 7. TRIGGERS date_modification
-- -----------------------------------------------------------------
CREATE TRIGGER trg_poste_modif BEFORE UPDATE ON poste
    FOR EACH ROW EXECUTE FUNCTION maj_date_modification();
CREATE TRIGGER trg_employe_modif BEFORE UPDATE ON employe_compte
    FOR EACH ROW EXECUTE FUNCTION maj_date_modification();
CREATE TRIGGER trg_besoin_modif BEFORE UPDATE ON besoin
    FOR EACH ROW EXECUTE FUNCTION maj_date_modification();
CREATE TRIGGER trg_offre_modif BEFORE UPDATE ON offre_emploi
    FOR EACH ROW EXECUTE FUNCTION maj_date_modification();
CREATE TRIGGER trg_candidature_modif BEFORE UPDATE ON candidature
    FOR EACH ROW EXECUTE FUNCTION maj_date_modification();
CREATE TRIGGER trg_proposition_modif BEFORE UPDATE ON proposition
    FOR EACH ROW EXECUTE FUNCTION maj_date_modification();
CREATE TRIGGER trg_contrat_modif BEFORE UPDATE ON contrat
    FOR EACH ROW EXECUTE FUNCTION maj_date_modification();


-- =================================================================
-- Données initiales intégrées depuis data.sql
-- =================================================================
-- =================================================================
-- Mr Tovo : données de référence + jeu de démonstration
-- À exécuter après schema.sql.
-- Mot de passe de tous les employés de démo : 1234
-- =================================================================

-- -----------------------------------------------------------------
-- 1. VALEURS DE RÉFÉRENCE (les libellés sont utilisés par le code : ne pas les renommer)
-- -----------------------------------------------------------------
INSERT INTO statut_besoin (libelle) VALUES ('cree'), ('envoie Finance'), ('valide Finance'), ('refuse finance');
INSERT INTO type_besoin (libelle) VALUES ('creation poste'), ('recherche personnel'), ('recherche stagiaire');
INSERT INTO statut_offre (libelle) VALUES ('enregistre'), ('valide'), ('publie'), ('fin');
INSERT INTO statut_candidature (libelle) VALUES ('cree'), ('selectionne'), ('teste techniquement'), ('entretien ok'), ('succes'), ('rejete');
INSERT INTO type_candidature (libelle) VALUES ('employe'), ('externe'), ('ancienne relation');
INSERT INTO statut_contrat (libelle) VALUES ('cree'), ('signe'), ('valide'), ('annule'), ('rompu');
INSERT INTO type_contrat (libelle) VALUES ('CE'), ('CDD'), ('CDI');
INSERT INTO statut_proposition (libelle) VALUES ('enregistre'), ('valide'), ('refuse'), ('en attente');

-- Localités de résidence proposées dans le formulaire.
INSERT INTO ville (libelle) VALUES ('Antananarivo'), ('Antsirabe'), ('Toamasina'), ('Fianarantsoa'), ('Mahajanga'), ('Toliara');

INSERT INTO commune (id_ville, libelle) VALUES
    (1, 'Antananarivo Renivohitra'), (1, 'Ambohimangakely'), (1, 'Alasora'), (1, 'Tanjombato'),
    (2, 'Antsirabe I'), (3, 'Toamasina I'), (4, 'Fianarantsoa I'), (5, 'Mahajanga I'), (6, 'Toliara I');

INSERT INTO fokontany (id_commune, libelle) VALUES
    (1, 'Analakely'), (1, 'Antaninarenina'), (1, 'Isoraka'), (1, 'Ankadifotsy'), (1, 'Andavamamba'),
    (2, 'Ambohimangakely'), (2, 'Betsizaraina'),
    (3, 'Alasora'), (3, 'Ambohimarina'),
    (4, 'Tanjombato'), (4, 'Ambohimanatrika'),
    (5, 'Mahazoarivo'), (5, 'Avaratsena'),
    (6, 'Tanambao V'), (6, 'Ankirihiry'),
    (7, 'Ambatomena'), (7, 'Talatamaty'),
    (8, 'Mahabibo'), (8, 'Tsararano'),
    (9, 'Tanambao I'), (9, 'Betania');

-- -----------------------------------------------------------------
-- 2. STRUCTURE DE L'ENTREPRISE (démo)
-- -----------------------------------------------------------------
INSERT INTO departement (code, libelle) VALUES
    ('DIR', 'Direction Générale'),     -- 1
    ('RH', 'Ressources Humaines'),     -- 2
    ('FIN', 'Finance'),                -- 3
    ('INFO', 'Informatique');          -- 4

INSERT INTO service (libelle, id_departement) VALUES
    ('Direction', 1),                     -- 1
    ('Recrutement', 2),                   -- 2
    ('Administration du personnel', 2),   -- 3
    ('Comptabilité', 3),                  -- 4
    ('Développement', 4),                 -- 5
    ('Infrastructure', 4);                -- 6

INSERT INTO poste (libelle, id_departement, id_service, est_manager, est_directeur, est_occupe) VALUES
    ('Directeur général', 1, 1, FALSE, TRUE, TRUE),      -- 1
    ('Manager RH', 2, 2, TRUE, FALSE, TRUE),             -- 2
    ('Chargé de recrutement', 2, 2, FALSE, FALSE, TRUE), -- 3
    ('Manager Finance', 3, 4, TRUE, FALSE, TRUE),        -- 4
    ('Comptable', 3, 4, FALSE, FALSE, TRUE),             -- 5
    ('Manager Informatique', 4, 5, TRUE, FALSE, TRUE),   -- 6
    ('Chef de projet', 4, 5, FALSE, FALSE, TRUE),        -- 7
    ('Chef de service', 4, 6, FALSE, FALSE, TRUE),       -- 8
    ('Développeur junior 1', 4, 5, FALSE, FALSE, TRUE),  -- 9
    ('Développeur junior 2', 4, 5, FALSE, FALSE, FALSE), -- 10 (vacant)
    ('Stagiaire développeur', 4, 5, FALSE, FALSE, FALSE);-- 11 (vacant)

-- Grille salariale mensuelle (Ariary) selon l'ancienneté (années)
INSERT INTO grille_salaire (valeur, id_poste, anciennete_min, anciennete_max) VALUES
    (8000000, 1, 0, 50),
    (4500000, 2, 0, 5), (5500000, 2, 6, 50),
    (1800000, 3, 0, 3), (2300000, 3, 4, 50),
    (4500000, 4, 0, 5), (5500000, 4, 6, 50),
    (1800000, 5, 0, 3), (2300000, 5, 4, 50),
    (4800000, 6, 0, 5), (5800000, 6, 6, 50),
    (3500000, 7, 0, 5), (4200000, 7, 6, 50),
    (3200000, 8, 0, 5), (3900000, 8, 6, 50),
    (2000000, 9, 0, 2), (2500000, 9, 3, 50),
    (2000000, 10, 0, 2), (2500000, 10, 3, 50),
    (500000, 11, 0, 1);

INSERT INTO budget_annuel (annee, montant) VALUES (2026, 400000000), (2027, 450000000);

-- Employés de démo (mot de passe : 1234)
INSERT INTO employe_compte (nom, prenom, matricule, email, id_poste_actuel, mot_de_passe, date_embauche) VALUES
    ('Rakoto', 'Jean', 'DIR001', 'jean.rakoto@mrtovo.mg', 1, '$2a$10$MXtT2ytIiLunRC7.cXWG4eb3XNCy/5WWlA8oLrq8zIHBRvEbFLL/i', '2015-01-05'),
    ('Rabe', 'Hery', 'RH001', 'hery.rabe@mrtovo.mg', 2, '$2a$10$MXtT2ytIiLunRC7.cXWG4eb3XNCy/5WWlA8oLrq8zIHBRvEbFLL/i', '2018-03-01'),
    ('Rasoa', 'Miora', 'RH002', 'miora.rasoa@mrtovo.mg', 3, '$2a$10$MXtT2ytIiLunRC7.cXWG4eb3XNCy/5WWlA8oLrq8zIHBRvEbFLL/i', '2021-06-14'),
    ('Randria', 'Fara', 'FIN001', 'fara.randria@mrtovo.mg', 4, '$2a$10$MXtT2ytIiLunRC7.cXWG4eb3XNCy/5WWlA8oLrq8zIHBRvEbFLL/i', '2017-09-01'),
    ('Rajao', 'Tiana', 'FIN002', 'tiana.rajao@mrtovo.mg', 5, '$2a$10$MXtT2ytIiLunRC7.cXWG4eb3XNCy/5WWlA8oLrq8zIHBRvEbFLL/i', '2022-02-01'),
    ('Rakotobe', 'Andry', 'INF001', 'andry.rakotobe@mrtovo.mg', 6, '$2a$10$MXtT2ytIiLunRC7.cXWG4eb3XNCy/5WWlA8oLrq8zIHBRvEbFLL/i', '2016-11-07'),
    ('Razafy', 'Lova', 'INF002', 'lova.razafy@mrtovo.mg', 7, '$2a$10$MXtT2ytIiLunRC7.cXWG4eb3XNCy/5WWlA8oLrq8zIHBRvEbFLL/i', '2019-04-15'),
    ('Ravelo', 'Niry', 'INF003', 'niry.ravelo@mrtovo.mg', 8, '$2a$10$MXtT2ytIiLunRC7.cXWG4eb3XNCy/5WWlA8oLrq8zIHBRvEbFLL/i', '2020-01-20'),
    ('Rasolo', 'Faly', 'INF004', 'faly.rasolo@mrtovo.mg', 9, '$2a$10$MXtT2ytIiLunRC7.cXWG4eb3XNCy/5WWlA8oLrq8zIHBRvEbFLL/i', '2024-07-01');

UPDATE departement SET id_manager_actuel = 1 WHERE code = 'DIR';
UPDATE departement SET id_manager_actuel = 2 WHERE code = 'RH';
UPDATE departement SET id_manager_actuel = 4 WHERE code = 'FIN';
UPDATE departement SET id_manager_actuel = 6 WHERE code = 'INFO';

