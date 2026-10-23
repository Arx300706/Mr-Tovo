-- =================================================================
-- Mr Tovo : application de gestion RH — schéma PostgreSQL initial
--
-- Migration Flyway, appliquée automatiquement au démarrage.
-- Ne jamais modifier un fichier V*__ déjà appliqué : ajouter un nouveau V<n>__.
-- =================================================================

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
    date_test_technique   TIMESTAMP NULL,
    date_entretien        TIMESTAMP NULL,
    commentaire_rh        TEXT,
    date_modification     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (id_compte, id_offre)                         -- une seule candidature par offre et par compte
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
