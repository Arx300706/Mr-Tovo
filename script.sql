-- =====================================================================
-- CV multiples et documents (CV / contrats) des employés existants
-- PostgreSQL — script non destructif, exécutable sur une base existante
-- =====================================================================

-- Chaque ligne représente un élément ajouté avec le bouton « + ».
-- L'élément appartient soit à une candidature, soit à un employé.
CREATE TABLE IF NOT EXISTS cv_element (
    id_cv_element  SERIAL PRIMARY KEY,
    id_candidature INT NULL REFERENCES candidature (id_candidature) ON DELETE CASCADE,
    id_employe     INT NULL REFERENCES employe_compte (id_employe) ON DELETE CASCADE,
    type           VARCHAR(30) NOT NULL
                   CHECK (type IN ('EXPERIENCE','FORMATION','COMPETENCE','QUALITE','LANGUE',
                                   'PROJET','CERTIFICATION','INTERET','LIEN')),
    titre          VARCHAR(255) NOT NULL, -- entreprise, école, compétence, langue...
    sous_titre     VARCHAR(255),          -- poste, diplôme, organisme...
    date_debut     VARCHAR(20),
    date_fin       VARCHAR(20),
    description    TEXT,
    niveau         VARCHAR(100),
    url            VARCHAR(500),
    ordre          INT NOT NULL DEFAULT 0,
    date_creation  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK ((id_candidature IS NOT NULL)::int + (id_employe IS NOT NULL)::int = 1)
);

-- Met aussi à jour la contrainte si une ancienne version de la table existe.
ALTER TABLE cv_element DROP CONSTRAINT IF EXISTS cv_element_type_check;
ALTER TABLE cv_element ADD CONSTRAINT cv_element_type_check
    CHECK (type IN ('EXPERIENCE','FORMATION','COMPETENCE','QUALITE','LANGUE',
                    'PROJET','CERTIFICATION','INTERET','LIEN'));

CREATE INDEX IF NOT EXISTS idx_cv_element_candidature
    ON cv_element (id_candidature, type, ordre);
CREATE INDEX IF NOT EXISTS idx_cv_element_employe
    ON cv_element (id_employe, type, ordre);

-- CV PDF et anciens contrats des personnes déjà présentes dans l'entreprise.
CREATE TABLE IF NOT EXISTS document_employe (
    id_document   SERIAL PRIMARY KEY,
    id_employe    INT NOT NULL REFERENCES employe_compte (id_employe) ON DELETE CASCADE,
    type           VARCHAR(20) NOT NULL CHECK (type IN ('CV','CONTRAT')),
    nom_fichier    VARCHAR(255) NOT NULL,
    fichier        BYTEA NOT NULL,
    mime_type      VARCHAR(100) NOT NULL DEFAULT 'application/pdf',
    date_document  DATE,
    date_depot     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description    TEXT
);

CREATE INDEX IF NOT EXISTS idx_document_employe
    ON document_employe (id_employe, type, date_depot DESC);

-- Pièce d'identité visible dans le CV protégé : CIN, passeport ou permis.
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

-- Exemple d'expérience pour un candidat :
-- INSERT INTO cv_element
--   (id_candidature, type, titre, sous_titre, date_debut, date_fin, description, ordre)
-- VALUES
--   (1, 'EXPERIENCE', 'Entreprise ABC', 'Développeur Java', '2024-01', '2025-06',
--    'Développement et maintenance des applications internes.', 0);

-- Exemple de compétence pour un employé existant :
-- INSERT INTO cv_element (id_employe, type, titre, niveau, ordre)
-- VALUES (1, 'COMPETENCE', 'Spring Boot', 'Avancé', 0);

-- Exemple d'import d'un ancien contrat PDF (à exécuter avec psql) :
-- INSERT INTO document_employe (id_employe, type, nom_fichier, fichier, date_document)
-- VALUES (1, 'CONTRAT', 'contrat-2024.pdf', pg_read_binary_file('/chemin/contrat-2024.pdf'), '2024-01-15');
