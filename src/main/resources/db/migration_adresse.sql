-- Migration non destructive : adresse structurée Ville → Commune → Fokontany.
CREATE TABLE IF NOT EXISTS ville (
    id_ville SERIAL PRIMARY KEY,
    libelle VARCHAR(150) NOT NULL UNIQUE
);
CREATE TABLE IF NOT EXISTS commune (
    id_commune SERIAL PRIMARY KEY,
    id_ville INT NOT NULL REFERENCES ville (id_ville),
    libelle VARCHAR(150) NOT NULL,
    UNIQUE (id_ville, libelle)
);
CREATE TABLE IF NOT EXISTS fokontany (
    id_fokontany SERIAL PRIMARY KEY,
    id_commune INT NOT NULL REFERENCES commune (id_commune),
    libelle VARCHAR(150) NOT NULL,
    UNIQUE (id_commune, libelle)
);

ALTER TABLE compte
    ADD COLUMN IF NOT EXISTS id_ville INT REFERENCES ville (id_ville),
    ADD COLUMN IF NOT EXISTS id_commune INT REFERENCES commune (id_commune),
    ADD COLUMN IF NOT EXISTS id_fokontany INT REFERENCES fokontany (id_fokontany),
    ADD COLUMN IF NOT EXISTS lot VARCHAR(100);

INSERT INTO ville (libelle) VALUES
    ('Antananarivo'), ('Antsirabe'), ('Toamasina'), ('Fianarantsoa'), ('Mahajanga'), ('Toliara')
ON CONFLICT (libelle) DO NOTHING;

INSERT INTO commune (id_ville, libelle)
SELECT v.id_ville, x.libelle FROM (VALUES
    ('Antananarivo','Antananarivo Renivohitra'), ('Antananarivo','Ambohimangakely'),
    ('Antananarivo','Alasora'), ('Antananarivo','Tanjombato'), ('Antsirabe','Antsirabe I'),
    ('Toamasina','Toamasina I'), ('Fianarantsoa','Fianarantsoa I'),
    ('Mahajanga','Mahajanga I'), ('Toliara','Toliara I')
) AS x(ville, libelle) JOIN ville v ON v.libelle = x.ville
ON CONFLICT (id_ville, libelle) DO NOTHING;

INSERT INTO fokontany (id_commune, libelle)
SELECT c.id_commune, x.fokontany FROM (VALUES
    ('Antananarivo Renivohitra','Analakely'), ('Antananarivo Renivohitra','Antaninarenina'),
    ('Antananarivo Renivohitra','Isoraka'), ('Antananarivo Renivohitra','Ankadifotsy'),
    ('Antananarivo Renivohitra','Andavamamba'), ('Ambohimangakely','Ambohimangakely'),
    ('Ambohimangakely','Betsizaraina'), ('Alasora','Alasora'), ('Alasora','Ambohimarina'),
    ('Tanjombato','Tanjombato'), ('Tanjombato','Ambohimanatrika'),
    ('Antsirabe I','Mahazoarivo'), ('Antsirabe I','Avaratsena'),
    ('Toamasina I','Tanambao V'), ('Toamasina I','Ankirihiry'),
    ('Fianarantsoa I','Ambatomena'), ('Fianarantsoa I','Talatamaty'),
    ('Mahajanga I','Mahabibo'), ('Mahajanga I','Tsararano'),
    ('Toliara I','Tanambao I'), ('Toliara I','Betania')
) AS x(commune, fokontany) JOIN commune c ON c.libelle = x.commune
ON CONFLICT (id_commune, libelle) DO NOTHING;

CREATE INDEX IF NOT EXISTS idx_commune_ville ON commune (id_ville);
CREATE INDEX IF NOT EXISTS idx_fokontany_commune ON fokontany (id_commune);
