-- =================================================================
-- Candidature : expérience professionnelle en texte libre + table cv
-- =================================================================

ALTER TABLE candidature ADD COLUMN IF NOT EXISTS experience_professionnelle TEXT; -- parcours professionnel détaillé (facultatif)

-- CV déposés avec une candidature (un enregistrement par dépôt)
CREATE TABLE IF NOT EXISTS cv (
    id_cv           SERIAL PRIMARY KEY,
    id_candidature  INT          NOT NULL REFERENCES candidature (id_candidature),
    nom_fichier     VARCHAR(255) NOT NULL,
    fichier         BYTEA        NOT NULL,                      -- PDF
    date_depot      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_employe      INT NULL REFERENCES employe_compte (id_employe), -- employé RH qui a traité le CV
    date_traitement TIMESTAMP NULL
);
