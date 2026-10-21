-- 0. (Optionnel de sécurité) Désactiver temporairement les contraintes
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Départements (avec id_manager mis à NULL temporairement pour éviter la boucle)
INSERT INTO departement (libelle, id_manager) VALUES 
('Ressources Humaines', NULL),
('Informatique / Tech', NULL),
('Finance', NULL);

-- 2. Services (dépendent de departement)
INSERT INTO service (libelle, id_departement) VALUES 
('Recrutement et Paie', 1),
('Développement Logiciel', 2),
('Comptabilité Générale', 3);

-- 3. Postes (dépendent de departement et service)
INSERT INTO poste (libelle, id_departement, id_service, est_manager, est_directeur, est_occupe, est_actif) VALUES 
('Responsable RH', 1, 1, TRUE, FALSE, TRUE, TRUE),
('Secretaire RH', 1, 1, FALSE, FALSE, TRUE, TRUE),
('Manager Informatique', 2, 2, TRUE, FALSE, TRUE, TRUE),
('Développeur FullStack Senior', 2, 2, FALSE, FALSE, TRUE, TRUE),
('Développeur FullStack Junior', 2, 2, FALSE, FALSE, TRUE, TRUE),
('Manager Finance', 3, 3, TRUE, FALSE, TRUE, TRUE),
('Comptable', 3, 3, FALSE, FALSE, TRUE, TRUE),
('Directeur General', 2, NULL, TRUE, TRUE, TRUE, TRUE);

-- 4. Employés (dépendent de poste, mais id_contrat est mis à NULL temporairement)
INSERT INTO employe (id_employe, nom, prenom, matricule, mot_de_passe, id_poste, id_contrat) VALUES 
(1, 'Tovo', 'Mr', 'MAT-0001', '$2a$10$e83pYpA4jC7zZ23V2q4y.O22A3T3nL1NfK1Y04aI6A/ZgJ0nK3G/m', 1, NULL),
(2, 'Lala', 'Mme', 'MAT-0002', '$2a$10$e83pYpA4jC7zZ23V2q4y.O22A3T3nL1NfK1Y04aI6A/ZgJ0nK3G/m', 2, NULL),
(3, 'Antsa', 'Mme', 'MAT-0003', '$2a$10$e83pYpA4jC7zZ23V2q4y.O22A3T3nL1NfK1Y04aI6A/ZgJ0nK3G/m', 3, NULL),
(4, 'Tiana', 'Mr', 'MAT-0004', '$2a$10$e83pYpA4jC7zZ23V2q4y.O22A3T3nL1NfK1Y04aI6A/ZgJ0nK3G/m', 4, NULL),
(5, 'John', 'Mr', 'MAT-0005', '$2a$10$e83pYpA4jC7zZ23V2q4y.O22A3T3nL1NfK1Y04aI6A/ZgJ0nK3G/m', 5, NULL),
(6, 'Koto', 'Mr', 'MAT-0006', '$2a$10$e83pYpA4jC7zZ23V2q4y.O22A3T3nL1NfK1Y04aI6A/ZgJ0nK3G/m', 6, NULL),
(7, 'Liantsoa', 'Mme', 'MAT-0007', '$2a$10$e83pYpA4jC7zZ23V2q4y.O22A3T3nL1NfK1Y04aI6A/ZgJ0nK3G/m', 7, NULL),
(8, 'Rakoto', 'Mr', 'MAT-0008', '$2a$10$e83pYpA4jC7zZ23V2q4y.O22A3T3nL1NfK1Y04aI6A/ZgJ0nK3G/m', 8, NULL);

-- 5. Types & Statuts de Contrat nécessaires pour la table contrat (si ce n'est pas déjà fait)
INSERT INTO type_contrat (id_type_contrat, libelle) VALUES 
(1, 'CDI'), (2, 'CDD'), (3, 'Stage'), (4, 'Alternance')
ON DUPLICATE KEY UPDATE libelle=libelle;

INSERT INTO statut_contrat (id_statut_contrat, libelle) VALUES 
(1, 'Actif'), (2, 'En attente'), (3, 'Suspendu'), (4, 'Terminé')
ON DUPLICATE KEY UPDATE libelle=libelle;

-- 6. Contrats (dépendent des employés maintenant existants)
INSERT INTO contrat (id_contrat, id_employe, id_proposition, id_statut_contrat, id_type_contrat) VALUES 
(1, 1, NULL, 1, 1), 
(2, 2, NULL, 1, 1), 
(3, 3, NULL, 1, 1), 
(4, 4, NULL, 1, 1), 
(5, 5, NULL, 1, 2), 
(6, 6, NULL, 1, 1), 
(7, 7, NULL, 1, 1), 
(8, 8, NULL, 1, 1);

-- 7. Mettre à jour les employés avec leurs contrats réels
UPDATE employe SET id_contrat = id_employe;

-- 8. Mettre à jour les managers de départements maintenant que les employés existent
UPDATE departement SET id_manager = 1 WHERE id_departement = 1;
UPDATE departement SET id_manager = 3 WHERE id_departement = 2;
UPDATE departement SET id_manager = 6 WHERE id_departement = 3;

-- 9. Grille de salaire (dépend des postes)
INSERT INTO grille_salaire (valeur, id_poste, anciennete_min, anciennete_max) VALUES 
(2500000.00, 1, 0, 3),
(3200000.00, 1, 3, 7),
(1200000.00, 2, 0, 3),
(1600000.00, 2, 3, NULL),
(4000000.00, 3, 0, 5),
(5200000.00, 3, 5, NULL),
(3000000.00, 4, 0, 4),
(4000000.00, 4, 4, NULL),
(1500000.00, 5, 0, 2),
(2000000.00, 5, 2, 4),
(3800000.00, 6, 0, 5),
(4800000.00, 6, 5, NULL),
(1800000.00, 7, 0, 3),
(2400000.00, 7, 3, NULL),
(6000000.00, 8, 0, 5),
(8000000.00, 8, 5, NULL);

-- 10. Réactiver les contraintes de clés étrangères
SET FOREIGN_KEY_CHECKS = 1;



-- Identifiant : MAT-0001
-- Mot de passe : demo1234