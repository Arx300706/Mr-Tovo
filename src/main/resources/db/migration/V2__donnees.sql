-- =================================================================
-- Mr Tovo : données de référence + jeu de démonstration
-- Migration Flyway, appliquée après V1__schema.sql.
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
