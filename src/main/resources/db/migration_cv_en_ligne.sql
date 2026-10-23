-- À exécuter une seule fois si la base « rh » existe déjà.
-- Une nouvelle installation utilisant schema.sql contient déjà ces colonnes.
ALTER TABLE compte
    ADD COLUMN IF NOT EXISTS ville VARCHAR(100);

ALTER TABLE candidature
    ADD COLUMN IF NOT EXISTS metier_recherche VARCHAR(150),
    ADD COLUMN IF NOT EXISTS presentation TEXT,
    ADD COLUMN IF NOT EXISTS formations TEXT,
    ADD COLUMN IF NOT EXISTS competences TEXT,
    ADD COLUMN IF NOT EXISTS qualites TEXT,
    ADD COLUMN IF NOT EXISTS langues TEXT,
    ADD COLUMN IF NOT EXISTS projets TEXT,
    ADD COLUMN IF NOT EXISTS certifications TEXT,
    ADD COLUMN IF NOT EXISTS centres_interet TEXT,
    ADD COLUMN IF NOT EXISTS linkedin VARCHAR(255),
    ADD COLUMN IF NOT EXISTS github VARCHAR(255),
    ADD COLUMN IF NOT EXISTS portfolio VARCHAR(255),
    ADD COLUMN IF NOT EXISTS photo BYTEA,
    ADD COLUMN IF NOT EXISTS photo_type VARCHAR(50);

CREATE TABLE IF NOT EXISTS cv_element (
    id_cv_element  SERIAL PRIMARY KEY,
    id_candidature INT NULL REFERENCES candidature (id_candidature) ON DELETE CASCADE,
    id_employe     INT NULL REFERENCES employe_compte (id_employe) ON DELETE CASCADE,
    type           VARCHAR(30) NOT NULL CHECK (type IN ('EXPERIENCE','FORMATION','COMPETENCE','QUALITE','LANGUE','PROJET','CERTIFICATION','INTERET','LIEN')),
    titre          VARCHAR(255) NOT NULL,
    sous_titre     VARCHAR(255), date_debut VARCHAR(20), date_fin VARCHAR(20),
    description    TEXT, niveau VARCHAR(100), url VARCHAR(500), ordre INT NOT NULL DEFAULT 0,
    CHECK ((id_candidature IS NOT NULL)::int + (id_employe IS NOT NULL)::int = 1)
);
ALTER TABLE cv_element DROP CONSTRAINT IF EXISTS cv_element_type_check;
ALTER TABLE cv_element ADD CONSTRAINT cv_element_type_check
    CHECK (type IN ('EXPERIENCE','FORMATION','COMPETENCE','QUALITE','LANGUE','PROJET','CERTIFICATION','INTERET','LIEN'));

CREATE TABLE IF NOT EXISTS document_employe (
    id_document SERIAL PRIMARY KEY,
    id_employe INT NOT NULL REFERENCES employe_compte (id_employe) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL CHECK (type IN ('CV','CONTRAT')),
    nom_fichier VARCHAR(255) NOT NULL, fichier BYTEA NOT NULL,
    mime_type VARCHAR(100) NOT NULL DEFAULT 'application/pdf',
    date_document DATE, date_depot TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, description TEXT
);

CREATE TABLE IF NOT EXISTS document_identite (
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
