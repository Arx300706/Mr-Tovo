# TODO — Mr Tovo (application de gestion RH)

Stack : **Spring Boot 4 + Thymeleaf + PostgreSQL**. Installation et structure : voir [`README.md`](README.md).

Légende : `[x]` fait et testé de bout en bout · `[ ]` reste à faire.

```
Manager          RH                Finance          Directeur        RH              Candidat / RH ...
besoin «cree» → «envoie Finance» → «valide Finance» → offre «enregistre» → «valide» → «publie» → candidature → proposition → contrat → employé
```

---

## Phase 0 — Socle technique

- [x] Projet Spring Boot 4 (Web MVC, Data JPA, Security, Validation, Thymeleaf, Lombok), driver PostgreSQL
- [x] Schéma PostgreSQL `db/migration/V1__schema.sql` (Flyway) (tables de référence, structure, flux, contraintes, triggers `date_modification`)
- [x] Données `db/migration/V2__donnees.sql` : statuts/types + entreprise de démo (4 départements, 6 services, 11 postes, grille salariale, budgets 2026–2027, 9 employés)
- [x] Hibernate en `validate` (le schéma SQL fait foi)
- [x] Mise en page commune : menu latéral par rôle, messages flash, badges de statut, montants en Ariary, historique
- [x] Pages d'erreur 403 / 404 / 500 sans trace technique
- [x] Gestion des règles métier : `RegleMetierException` → retour sur la page avec le message

## Phase 1 — Connexion et rôles

- [x] Page d'accueil publique : liste des offres **publiées** et non expirées, détail d'une offre
- [x] Une seule page de connexion, deux onglets : **Employé** (matricule + mot de passe) / **Candidat** (e-mail + mot de passe)
- [x] Rôles calculés depuis le poste actuel : DIRECTEUR (`est_directeur`, a tous les droits), MANAGER (`est_manager`), RH (département `RH`), FINANCE (département `FIN`), EMPLOYE, CANDIDAT
- [x] Routes protégées par rôle (`/manager/**`, `/rh/**`, `/finance/**`, `/directeur/**`, `/candidat/**`, `/espace/**`)
- [x] Mots de passe chiffrés en BCrypt, protection CSRF sur tous les formulaires
- [x] Après connexion : employé → tableau de bord `/espace`, candidat → `/candidat`

## Phase 2 — Flux métier

### Étape 1 — Expression du besoin (Manager)
- [x] Liste des besoins **de son département** (le directeur voit tout)
- [x] Formulaire : type (*création de poste* / *recherche de personnel pour un poste* / *recherche de stagiaire*), service, poste existant (obligatoire pour une recherche de personnel), intitulé, nombre, type de contrat, sexe, âge min/max, date de commencement, budget estimé, motif
- [x] Contrôles : service et poste du bon département, âge min ≤ âge max, date non passée
- [x] Validation → statut **`cree`** (+ trace)

### Étape 2 — Analyse RH
- [x] Liste des besoins **`cree`** (filtres par statut) — pastille dans le menu
- [x] Détail du besoin avec la **grille salariale** du poste concerné (ou toute la grille si nouveau poste)
- [x] Saisie du **salaire mensuel adéquat** + commentaire → statut **`envoie Finance`**
- [x] Page « Grille salariale » (salaire mensuel et coût annuel par poste et ancienneté)

### Étape 3 — Validation budgétaire (Finance)
- [x] Liste des besoins **`envoie Finance`** — pastille dans le menu
- [x] Budget annuel affiché : montant, déjà engagé, **reste disponible**
- [x] **Dépense annuelle calculée automatiquement** : `nombre × salaire mensuel × 12` (ex. 2 × 2 000 000 × 12 = 48 000 000 Ar)
- [x] Si budget restant ≥ dépense → bouton **Valider** → **`valide Finance`** (la dépense est imputée sur le budget de l'année)
- [x] Sinon validation impossible → **Refuser** → **`refuse finance`**
- [x] Notification au manager (validé / refusé)

### Étape 4 — Création de l'offre (RH)
- [x] Liste des besoins **`valide Finance`** sans offre (« Offres à créer ») — pastille
- [x] Formulaire d'offre pré-rempli depuis le besoin : intitulé, description, profil, date limite de candidature
- [x] Enregistrement → offre **`enregistre`** (une seule offre par besoin)
- [x] Liste de toutes les offres avec leur statut (filtres)

### Étape 5 — Validation de l'offre (Directeur)
- [x] Liste des offres **`enregistre`** — pastille
- [x] Bouton **Valider** → **`valide`**

### Étape 6 — Publication (RH)
- [x] Offres **`valide`** — pastille sur « Offres d'emploi »
- [x] Bouton **Publier** → **`publie`** : l'offre apparaît sur l'accueil public ; notification au manager
- [x] Bouton **Clôturer** → **`fin`**
- [x] Passage automatique à **`fin`** quand tous les postes du besoin sont pourvus

### Étape 7 — Candidature (visiteur)
- [x] Choix : **employé** (matricule obligatoire, doit avoir un poste) / **externe** / **ancienne relation** (matricule facultatif)
- [x] Saisie : identité, contact, date de naissance, sexe, adresse, diplôme, expérience, lettre de motivation
- [x] **Création du compte candidat** (e-mail + mot de passe) au moment de valider ; e-mail déjà connu → on demande le mot de passe du compte existant
- [x] Connexion automatique après la candidature ; un candidat déjà connecté postule sans ressaisir son compte
- [x] Une seule candidature par compte et par offre ; offre fermée/expirée refusée
- [x] Candidature → statut **`cree`** + notification au candidat

### Étape 8 — Sélection (RH)
- [x] Liste des candidatures **`cree`** (filtres par statut) — pastille
- [x] Dossier du candidat : âge et sexe comparés aux critères du besoin, diplôme, expérience, lettre
- [x] Saisie des dates du **test technique** et de l'**entretien** → **`selectionne`** (dates modifiables ensuite)
- [x] Notification au candidat avec les dates

### Étape 9 — Suivi par le candidat
- [x] Espace candidat : ses candidatures, statut, dates du test et de l'entretien, historique

### Étape 10 — Épreuves (RH)
- [x] `selectionne` → **`teste techniquement`** → **`entretien ok`** → **`succes`** (notification)
- [x] **`rejete`** possible à chaque étape avant `succes` (motif + notification)

### Étape 11 — Proposition d'emploi (RH)
- [x] Liste des candidatures **`succes`** sans proposition — pastille
- [x] Formulaire pré-rempli (salaire retenu par la RH, contrat et date du besoin) : poste vacant **ou nouveau poste**, salaire, type de contrat, date de commencement, date de fin (CE/CDD seulement), date limite de réponse
- [x] Proposition → statut **`enregistre`** + notification au candidat

### Étape 12 — Réponse du candidat (négociation)
- [x] Chaque champ (salaire, type de contrat, date de début, date de fin si CE/CDD) est coché **Oui** ou **Non** ; si Non, un champ « valeur souhaitée » s'affiche
- [x] Tout Oui → **`valide`** · tout Non → **`refuse`** · sinon → **`en attente`**
- [x] Réponse impossible après la date limite
- [x] Discussion RH ↔ candidat (messages) sur la proposition
- [x] RH, sur une proposition `en attente` : **accepter les souhaits** du candidat (→ `valide`), **réviser** la proposition (→ `enregistre`, le candidat répond à nouveau), ou **clore** (→ `refuse`)

### Étape 13 — Contrat (RH puis candidat)
- [x] Propositions **`valide`** sans contrat — pastille ; bouton **Générer le contrat** → **`cree`**
- [x] Candidat : **Signer** → **`signe`** ou **Annuler** → **`annule`**
- [x] RH sur un contrat signé : **Signer et valider** → **`valide`**, ou **Annuler** → **`annule`**

### Étape 14 — Embauche (automatique à la validation du contrat)
- [x] Nouveau candidat → création de l'employé avec matricule `EMPxxxx` (mot de passe = celui du compte candidat)
- [x] Employé existant / ancienne relation → l'ancien poste passe à `est_occupe = false`, l'ancien contrat passe à **`rompu`**
- [x] Nouveau poste `est_occupe = true`, `id_poste_actuel` et `id_contrat_actuel` mis à jour
- [x] Si le poste est un poste de manager → devient `manager_actuel` du département
- [x] Ligne dans `historique_employe` ; notifications au candidat (avec son matricule) et au manager

## Phase 3 — Transversal

- [x] **Traçabilité** : `date_modification` par trigger + table `historique_statut` (ancien → nouveau statut, auteur, date, commentaire) affichée sur chaque détail
- [x] **Pastilles / notifications** : chaque entrée de menu est marquée quand quelque chose attend une action (besoins, offres, candidatures, propositions, contrats, messages non lus)
- [x] Page Notifications (candidat et employé), clic = marqué comme lu, « tout marquer comme lu »
- [x] Tableau de bord employé : raccourcis selon les rôles, dossier et parcours (historique des postes)
- [x] Tests unitaires : règle Oui/Non de la proposition, calcul de la dépense annuelle
- [x] Test de bout en bout manuel (curl) du flux complet + cas : employé interne, budget insuffisant, accès croisé

---

## À faire ensuite

### Priorité 1 — utile pour la démo
- [ ] Écrans d'administration de la structure : départements, services, postes, grille salariale, budget annuel (aujourd'hui uniquement par SQL)
- [ ] Le manager peut modifier / supprimer un besoin tant qu'il est `cree`
- [ ] La RH peut modifier une offre tant qu'elle est `enregistre` ; le directeur peut la renvoyer avec un commentaire
- [ ] Clôture automatique quotidienne (`@Scheduled`) des offres dont la date limite est passée (aujourd'hui elles disparaissent de l'accueil mais restent `publie`)

### Priorité 2
- [x] Dépôt du CV (fichier PDF, 5 Mo max) avec la candidature ; expérience professionnelle en texte libre
- [x] Contrat au format PDF (RH et candidat)
- [ ] Gestion des comptes : changement de mot de passe, mot de passe oublié
- [ ] Recherche et pagination dans les listes RH
- [ ] Tests d'intégration (MockMvc + base PostgreSQL de test) sur chaque étape du flux

### Priorité 3
- [ ] Envoi réel des notifications par e-mail
- [ ] Rupture de contrat et fin de CDD (suite du cycle de vie du salarié)
- [ ] Tableau de bord chiffré (nombre de recrutements, budget consommé par département)

---

## Corrections apportées à la todo initiale (Gemini)

- Base **PostgreSQL** et non MySQL.
- Étape 11 : la proposition est créée au statut **`enregistre`** (ajouté à `statut_proposition`), pas `en attente` ; `en attente` signifie « contre-proposition du candidat à traiter ».
- Étape 3 : ajout d'une table **`budget_annuel`** et du **salaire mensuel retenu par la RH** sur le besoin, nécessaires au calcul automatique de la dépense.
- Étape 7 : le compte candidat doit contenir l'**e-mail** (identifiant) et l'identité ; le matricule n'est demandé qu'aux employés / anciennes relations.
- Étape 12 : la négociation est stockée dans `reponse_proposition` (une ligne par réponse) et `message_proposition` (discussion), et la RH a trois actions explicites (accepter les souhaits, réviser, clore).
- Étapes 13–14 : le contrat n'a pas encore d'employé quand il est créé (`id_employe` rempli à la validation) ; l'ancien contrat d'un employé interne passe à `rompu`.
- Ajout de la **traçabilité des statuts** (`historique_statut`) et des **notifications** (`notification`) demandées dans le flux.
