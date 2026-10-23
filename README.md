# Mr Tovo — Gestion des ressources humaines

Application web de gestion RH : **besoin → offre d'emploi → candidature → proposition → contrat → embauche**.

Le dépôt de candidature comprend aussi un **CV en ligne structuré et responsive** (profil, expériences,
formations, compétences, langues, projets et liens professionnels), imprimable ou enregistrable en PDF.

| Élément       | Choix                                  |
|---------------|----------------------------------------|
| Java          | 21                                     |
| Framework     | Spring Boot 4.1 (Web MVC, Data JPA, Security, Validation) |
| Vues          | Thymeleaf (+ CSS/JS maison, sans dépendance) |
| Base          | PostgreSQL (base `rh`)                 |
| Build         | Maven (wrapper `./mvnw`)               |

Le parcours fonctionnel complet et l'état d'avancement sont dans [`TODO.md`](TODO.md).

---

## 1. Installation

### Base de données

```bash
# Créer la base (une seule fois)
createdb -h localhost -U postgres rh

# Créer les tables puis insérer les données de référence + la démo
# ATTENTION : schema.sql supprime et recrée tout (remise à zéro)
psql -h localhost -U postgres -d rh -f src/main/resources/db/schema.sql
psql -h localhost -U postgres -d rh -f src/main/resources/db/data.sql
```

Pour mettre à jour une base déjà créée sans effacer ses données :

```bash
psql -h localhost -U postgres -d rh -f src/main/resources/db/migration_cv_en_ligne.sql
psql -h localhost -U postgres -d rh -f src/main/resources/db/migration_adresse.sql
```

Connexion par défaut : `jdbc:postgresql://localhost:5432/rh`, utilisateur `postgres`, mot de passe `postgres`.
Pour changer, utilisez les variables d'environnement `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

Hibernate est en mode `validate` : il **ne modifie jamais** la base, il vérifie seulement que les entités
correspondent au schéma. Toute évolution du modèle passe par `schema.sql`.

### Lancer l'application

```bash
./mvnw spring-boot:run      # puis ouvrir http://localhost:8080
./mvnw test                 # tests unitaires
```

---

## 2. Comptes de démonstration

Employés (connexion avec le **matricule**, mot de passe `1234`) :

| Matricule | Personne        | Poste                  | Rôles dans l'application          |
|-----------|-----------------|------------------------|-----------------------------------|
| `DIR001`  | Jean Rakoto     | Directeur général      | Directeur (peut tout voir et tout faire) |
| `RH001`   | Hery Rabe       | Manager RH             | Manager + RH                      |
| `RH002`   | Miora Rasoa     | Chargé de recrutement  | RH                                |
| `FIN001`  | Fara Randria    | Manager Finance        | Manager + Finance                 |
| `FIN002`  | Tiana Rajao     | Comptable              | Finance                           |
| `INF001`  | Andry Rakotobe  | Manager Informatique   | Manager                           |
| `INF002`… | …               | Chef de projet, etc.   | Employé                           |

Candidats : le compte (e-mail + mot de passe) est créé au moment de la candidature.

### Comment les rôles sont calculés

Les droits viennent du **poste actuel** de l'employé :

- `poste.est_directeur = true` → **DIRECTEUR** (reçoit aussi MANAGER, RH et FINANCE) ;
- `poste.est_manager = true` → **MANAGER** de son département ;
- poste dans le département de code `RH` → **RH** ; code `FIN` → **FINANCE**.

---

## 3. Structure du projet

```
src/main/java/com/mrtovo/
├── MrTovoApplication.java
├── config/            SecurityConfig (routes par rôle, login), ModeleGlobalAdvice (utilisateur + pastilles du menu)
├── security/          Roles, UtilisateurConnecte, UtilisateurDetailsService (matricule OU e-mail)
├── entity/            Entités JPA (une par table)
│   └── reference/     Tables de statuts et de types (+ constantes des libellés)
├── repository/        Interfaces Spring Data JPA
├── service/           Règles métier, une classe par étape du flux
│   ├── BesoinService        cree → envoie Finance → valide / refuse finance
│   ├── OffreService         enregistre → valide (directeur) → publie → fin
│   ├── CandidatureService   cree → selectionne → teste techniquement → entretien ok → succes / rejete
│   ├── PropositionService   enregistre → valide / refuse / en attente (négociation)
│   ├── ContratService       cree → signe → valide (embauche) / annule
│   ├── DecisionProposition  règle Oui/Non champ par champ
│   ├── BudgetService, BadgeService, NotificationService, TraceService, ReferenceService
├── controller/        Un contrôleur par domaine (public, candidat, besoins, offres, candidatures, propositions, contrats…)
├── dto/               Objets de formulaire (validation Jakarta)
└── exception/         RegleMetierException (message affiché à l'utilisateur), 404, gestionnaire global

src/main/resources/
├── application.properties
├── db/schema.sql      Schéma PostgreSQL (tables, contraintes, triggers date_modification)
├── db/data.sql        Statuts/types + entreprise de démo
├── static/css/style.css, static/js/app.js
└── templates/
    ├── fragments/     layout (head, menu, pastilles, statut, montant, historique), proposition
    ├── public/        accueil (offres), offre, candidater
    ├── auth/          connexion (onglets Employé / Candidat)
    ├── candidat/      espace candidat : candidatures, proposition (réponse Oui/Non), contrat
    ├── espace/        tableau de bord employé
    ├── besoin/ offre/ candidature/ proposition/ contrat/ rh/   écrans métier
    ├── commun/        notifications
    └── error/         403, 404, 500
```

### Conventions

- **Libellés de statut** : stockés dans les tables de référence et utilisés par le code via les constantes
  (`StatutBesoin.CREE`, `StatutOffre.PUBLIE`…). Ne pas les renommer en base.
- **Traçabilité** : chaque changement de statut passe par `TraceService.tracer(...)` (table `historique_statut`)
  et les colonnes `date_modification` sont mises à jour par des triggers PostgreSQL.
- **Règle métier refusée** : lever `RegleMetierException("message")` ; l'utilisateur est renvoyé sur sa page avec le message.
- **Pastilles du menu** : `BadgeService` calcule ce qui attend une action ; une entrée de menu avec quelque chose en attente est marquée.

---

## 4. Écarts par rapport au MCD d'origine (et pourquoi)

| Changement | Raison |
|------------|--------|
| MySQL → PostgreSQL, `ON UPDATE CURRENT_TIMESTAMP` → triggers | demandé ; PostgreSQL n'a pas `ON UPDATE` |
| `departement.code` | savoir qui est RH / Finance / Direction |
| `besoin.id_poste`, `id_type_contrat` (FK au lieu d'un texte), `salaire_mensuel`, `commentaire_rh`, `depense_annuelle`, `annee_budget`, `commentaire_finance` ; `id_services` → `id_service` | étapes RH (grille salariale) et Finance (dépense = nombre × salaire × 12) |
| table `budget_annuel` | afficher le budget restant à la Finance |
| `compte` : e-mail, nom, prénom, téléphone, date de naissance, sexe, adresse ; suppression de `compte.id_candidature` | se connecter et identifier le candidat ; un compte peut avoir plusieurs candidatures (`candidature.id_compte`) |
| `candidature` : diplôme, expérience, lettre de motivation, commentaire RH | contenu de la candidature |
| `proposition.id_candidature`, `proposition.id_poste`, statut `enregistre` | savoir à qui et pour quel poste ; le flux crée la proposition au statut « enregistré » |
| tables `reponse_proposition`, `message_proposition` | réponse Oui/Non champ par champ + discussion RH ↔ candidat |
| `contrat` : `id_employe` facultatif, `id_candidature`, poste, salaire, dates, dates de signature | l'employé n'existe qu'après validation du contrat |
| tables `historique_statut`, `notification` | traçabilité de chaque statut, messages au candidat / à l'employé |
