# TODO — Mr Tovo : application de gestion RH

Cette todolist découpe le projet en **3 équipes** : **Design**, **Back** et **Front**.
Chaque tâche a un identifiant (`D-xx`, `B-xx`, `F-xx`, `T-xx`) pour qu'on puisse s'y référer dans les discussions, les commits et les dépendances.

> Rappel de la stack (voir `README.md`) : Java 21, Spring Boot 4, Thymeleaf, Maven.
> Le **Back** s'occupe des données, des règles métier, des contrôleurs et de la sécurité.
> Le **Front** s'occupe des templates Thymeleaf, du CSS et du JavaScript.
> Le **Design** s'occupe des parcours utilisateurs, des maquettes et de la charte graphique.

---

## Sommaire

0. [Comment utiliser ce fichier](#0-comment-utiliser-ce-fichier)
1. [Vue d'ensemble du projet](#1-vue-densemble-du-projet)
2. [Modèle de données de référence](#2-modèle-de-données-de-référence)
3. [Statuts et workflows](#3-statuts-et-workflows)
4. [Rôles et droits](#4-rôles-et-droits)
5. [Tâches communes (toute l'équipe)](#5-tâches-communes-toute-léquipe)
6. [Équipe Design](#6-équipe-design)
7. [Équipe Back](#7-équipe-back)
8. [Équipe Front](#8-équipe-front)
9. [Planning conseillé et dépendances](#9-planning-conseillé-et-dépendances)
10. [Définition de « terminé »](#10-définition-de--terminé-)
11. [Glossaire](#11-glossaire)

---

## 0. Comment utiliser ce fichier

- [ ] Chaque membre met son prénom à côté de la tâche qu'il prend : `- [ ] B-12 … (@prénom)`.
- [ ] Quand une tâche est finie, cocher la case `- [x]` dans le même commit que le travail, ou juste après.
- [ ] Une tâche **bloquée** est marquée `⛔` avec la raison et l'identifiant de la tâche qui bloque.
- [ ] Une branche Git par tâche ou par petit groupe de tâches, nommée avec l'identifiant (ex. `B-12-demande-recrutement`).
- [ ] Les messages de commit commencent par l'identifiant (ex. `F-21 : liste des demandes`).
- [ ] Si une tâche est trop grosse (plus de 2 jours), la découper en sous-tâches dans ce fichier.
- [ ] En cas de doute sur une règle métier, se référer aux sections 2, 3 et 4. Si la réponse n'y est pas, la décider en équipe et **l'écrire ici**.

---

## 1. Vue d'ensemble du projet

L'application suit le parcours complet d'un recrutement, du besoin jusqu'à l'intégration du salarié.

| Bloc | Sujet | Ce qu'il faut pouvoir faire | Objets métier |
|---|---|---|---|
| **1** | Expression du besoin en personnel | Un manager demande la création ou le remplacement d'un poste. Circuit de validation : **Manager → RH → Direction → ouverture du recrutement**. | Département, Poste, DemandeRecrutement, Validation |
| **2** | Recrutement | Transformer une demande validée en offre d'emploi, la publier, recevoir des candidatures, présélectionner, organiser entretiens et tests, garder les évaluations et la décision finale. | OffreEmploi, Candidat, Candidature, Entretien, EvaluationCandidat |
| **3** | Sélection et embauche | Envoyer une proposition d'embauche au candidat retenu. S'il accepte : créer le dossier salarié et son matricule, puis le contrat, le poste et l'affectation. | PropositionEmbauche, Employé, Contrat, Affectation |
| **4** | Contrat et onboarding | Garder l'historique des contrats (CDD, renouvellement, CDI…). Suivre l'intégration avec une checklist : contrat signé, badge, compte, matériel, formation, inscription paie. | Contrat, Affectation, Onboarding, OnboardingTâche, Document |

### Les 3 règles de conception à ne jamais oublier

1. **Un POSTE existe indépendamment de l'EMPLOYÉ.** Un poste peut être vacant, occupé, puis libéré et réoccupé par quelqu'un d'autre. On ne supprime jamais un poste parce que son titulaire part.
2. **CANDIDAT ≠ EMPLOYÉ.** Une personne peut avoir plusieurs candidatures avant d'être embauchée. Le dossier employé n'est créé **qu'après acceptation** de la proposition d'embauche ; il garde un lien vers le candidat d'origine.
3. **On n'écrase pas l'historique.** Le type de contrat n'est **pas** une simple colonne de l'employé. Chaque contrat (CDD, renouvellement, CDI…) est une ligne distincte avec ses dates. Pareil pour les affectations.

### Architecture technique visée

| Couche applicative | Données et intégration |
|---|---|
| Front Web (Thymeleaf), mobile en responsive | Base de données PostgreSQL (ou MySQL) |
| API REST (pour le mobile ou d'autres outils, plus tard) | GED : stockage des documents (CV, contrats…) |
| Services métier RH | Notifications (e-mail et/ou dans l'application) |
| Moteur de workflow (validations, statuts) | Audit et logs |
| Authentification et rôles | Reporting / tableau de bord |

---

## 2. Modèle de données de référence

> Ce modèle est la **référence commune** des trois équipes. Le Back l'implémente, le Design et le Front s'en servent pour savoir quelles informations afficher et saisir.
> Toute modification doit être validée en équipe puis reportée ici.

Champs présents partout (pas répétés dans les tableaux) : identifiant, date de création, date de modification, créé par.

### Bloc 1 — Besoin

| Objet | Champs principaux | Liens |
|---|---|---|
| **Département** | nom, code, description, responsable | a plusieurs Postes ; a un responsable (Utilisateur) |
| **Poste** | intitulé, code, description des missions, niveau / catégorie, statut (vacant, occupé, gelé, supprimé) | appartient à un Département ; peut avoir plusieurs Affectations dans le temps |
| **DemandeRecrutement** | type (création ou remplacement), effectif demandé, motif, budget estimé, date d'embauche souhaitée, statut, commentaire | concerne un Poste (existant pour un remplacement, à créer pour une création) ; un Département ; créée par un Manager ; a plusieurs Validations |
| **Validation** | étape (RH, Direction), décision (approuvée, refusée, à compléter), commentaire, date | appartient à une DemandeRecrutement ; faite par un Utilisateur |

### Bloc 2 — Recrutement

| Objet | Champs principaux | Liens |
|---|---|---|
| **OffreEmploi** | titre, description, profil recherché, type de contrat proposé, lieu, fourchette de salaire (optionnelle), date de publication, date limite, statut | issue d'une DemandeRecrutement validée ; reçoit plusieurs Candidatures |
| **Candidat** | nom, prénom, e-mail (unique), téléphone, adresse, date de naissance (optionnelle) | a plusieurs Candidatures ; peut devenir un Employé |
| **Candidature** | date de dépôt, lettre de motivation, CV (Document), source (site, recommandation, salon…), statut, motif de rejet | relie un Candidat à une OffreEmploi ; a plusieurs Entretiens et Évaluations |
| **Entretien** | type (téléphonique, RH, technique, test écrit, final), date et heure, lieu ou lien visio, durée, statut (planifié, réalisé, annulé, reporté) | appartient à une Candidature ; un ou plusieurs évaluateurs (Utilisateurs) |
| **ÉvaluationCandidat** | note globale, critères notés (compétences, motivation, savoir-être…), points forts, points faibles, avis (favorable, réservé, défavorable), commentaire | liée à un Entretien (ou à la Candidature) ; écrite par un évaluateur |

### Bloc 3 — Embauche

| Objet | Champs principaux | Liens |
|---|---|---|
| **PropositionEmbauche** | salaire proposé, date de début proposée, type de contrat, date d'envoi, date limite de réponse, statut, motif de refus | liée à une Candidature retenue |
| **Employé** | matricule (unique, généré automatiquement), nom, prénom, e-mail professionnel, téléphone, date d'entrée, statut (actif, sorti) | issu d'un Candidat ; a plusieurs Contrats, Affectations, Documents, un ou plusieurs Onboardings |

### Bloc 4 — Contrat et onboarding

| Objet | Champs principaux | Liens |
|---|---|---|
| **Contrat** | type (CDD, CDI, stage, alternance…), date de début, date de fin (vide pour un CDI), salaire, temps de travail, période d'essai, statut (brouillon, signé, actif, terminé), motif de fin | appartient à un Employé ; peut pointer vers le contrat précédent (renouvellement, passage CDD → CDI) |
| **Affectation** | date de début, date de fin (vide si en cours), taux d'occupation, motif | relie un Employé à un Poste pendant une période |
| **Onboarding** | date de début, date de fin prévue, statut (en cours, terminé, abandonné), progression (%) | concerne un Employé ; a plusieurs OnboardingTâches |
| **OnboardingTâche** | libellé, catégorie (contrat, badge, compte informatique, matériel, formation, paie, autre), responsable, échéance, statut (à faire, en cours, fait, non applicable), commentaire, date de réalisation | appartient à un Onboarding ; peut avoir un Document justificatif |
| **Document** | nom, type (CV, lettre, contrat signé, pièce d'identité, RIB, diplôme…), nom du fichier stocké, format, taille, date d'ajout | rattaché à un Candidat, une Candidature, un Employé, un Contrat ou une OnboardingTâche |

### Transverse

| Objet | Champs principaux | Liens |
|---|---|---|
| **Utilisateur** | nom, prénom, e-mail (identifiant), mot de passe chiffré, actif oui/non | a un ou plusieurs Rôles ; rattaché à un Département (pour les managers) |
| **Rôle** | code (ADMIN, RH, DIRECTION, MANAGER, EMPLOYE), libellé | |
| **Notification** | destinataire, titre, message, lien vers l'objet concerné, lue oui/non, date | |
| **JournalAudit** | qui, quand, action (création, modification, changement de statut, suppression), objet concerné, ancienne valeur, nouvelle valeur | |
| **ModèleTâcheOnboarding** | libellé, catégorie, responsable par défaut (rôle), délai en jours après l'arrivée, obligatoire oui/non | sert à générer automatiquement les tâches d'un Onboarding |

---

## 3. Statuts et workflows

> Les transitions qui ne sont pas listées ici sont **interdites**. Le Back les bloque, le Front n'affiche que les boutons des transitions possibles.

### DemandeRecrutement

| De | Vers | Qui | Condition |
|---|---|---|---|
| Brouillon | Soumise | Manager auteur | tous les champs obligatoires remplis |
| Soumise | Validée RH | RH | |
| Soumise | Refusée | RH | commentaire obligatoire |
| Soumise | À compléter | RH | commentaire obligatoire ; retourne au manager |
| À compléter | Soumise | Manager auteur | |
| Validée RH | Validée Direction | Direction | |
| Validée RH | Refusée | Direction | commentaire obligatoire |
| Validée Direction | Recrutement ouvert | RH | crée l'OffreEmploi (brouillon) |
| Brouillon, Soumise, À compléter | Annulée | Manager auteur | |

Chaque décision RH ou Direction crée une ligne **Validation** : on garde tout l'historique, même en cas d'allers-retours.

### OffreEmploi

Brouillon → Publiée → Clôturée (date limite passée ou fermeture manuelle) → Pourvue (une proposition a été acceptée).
Publiée → Annulée possible par RH.

### Candidature

Reçue → Présélectionnée → En entretien → Retenue → Proposition envoyée → Embauché
Depuis n'importe quel statut avant « Embauché » : → Rejetée (motif obligatoire) ou → Retirée (par le candidat).
Proposition envoyée → Proposition refusée (le candidat refuse) : on peut alors retenir un autre candidat.

### PropositionEmbauche

Brouillon → Envoyée → Acceptée **ou** Refusée **ou** Expirée (date limite dépassée).
À l'acceptation, déclencher **automatiquement** dans cet ordre :
1. Création de l'Employé avec génération du matricule.
2. Création du Contrat (statut brouillon).
3. Création de l'Affectation sur le Poste ; le Poste passe à « occupé ».
4. Création de l'Onboarding avec ses tâches générées depuis les modèles.
5. L'Offre passe à « Pourvue », les autres candidatures encore ouvertes reçoivent une notification de clôture.

### Contrat

Brouillon → Signé → Actif → Terminé.
Un **renouvellement** ou un **passage CDD → CDI** = un **nouveau** contrat qui pointe vers le précédent ; l'ancien passe à « Terminé ». On ne modifie jamais un contrat signé.
Règle : un employé ne peut avoir **qu'un seul contrat actif** à une date donnée.

### Onboarding

En cours → Terminé (automatique quand toutes les tâches obligatoires sont « fait » ou « non applicable »).
En cours → Abandonné (ex. départ pendant la période d'essai).

---

## 4. Rôles et droits

| Action | Manager | RH | Direction | Employé | Admin |
|---|:-:|:-:|:-:|:-:|:-:|
| Créer / soumettre une demande de recrutement | ✅ (son département) | ✅ | | | |
| Valider une demande (étape RH) | | ✅ | | | |
| Valider une demande (étape Direction) | | | ✅ | | |
| Gérer départements et postes | lecture | ✅ | lecture | | ✅ |
| Créer / publier une offre | | ✅ | | | |
| Voir les candidatures | ses postes | ✅ | ✅ | | |
| Planifier un entretien | ✅ (ses postes) | ✅ | | | |
| Saisir une évaluation | ✅ si évaluateur | ✅ | ✅ si évaluateur | | |
| Envoyer une proposition d'embauche | | ✅ | | | |
| Gérer contrats et affectations | lecture (son équipe) | ✅ | lecture | lecture (les siens) | |
| Suivre l'onboarding | ses nouveaux arrivants | ✅ | lecture | sa propre checklist | |
| Cocher une tâche d'onboarding | si responsable | ✅ | | si responsable | |
| Gérer utilisateurs et rôles | | | | | ✅ |
| Voir le journal d'audit | | ✅ | | | ✅ |
| Tableau de bord / reporting | son département | ✅ | ✅ | | ✅ |

Les **candidats** n'ont pas de compte : ils postulent via une page publique des offres.

---

## 5. Tâches communes (toute l'équipe)

- [ ] **T-01** Lire ensemble ce fichier et le `README.md` ; tout le monde lance le projet en local une fois.
- [ ] **T-02** Valider ensemble le modèle de données (section 2) et les statuts (section 3). Noter les décisions prises.
- [ ] **T-03** Valider la matrice des droits (section 4).
- [ ] **T-04** Se mettre d'accord sur les règles Git : branche principale protégée, une pull request par tâche, au moins une relecture par une autre personne.
- [ ] **T-05** Créer un tableau de suivi (GitHub Projects, Trello…) avec les colonnes À faire / En cours / En relecture / Terminé, ou utiliser directement ce fichier.
- [ ] **T-06** Rédiger le **contrat d'interface Back ↔ Front** pour chaque écran : URL, données envoyées par le Back à la page, champs du formulaire, messages d'erreur. Le stocker dans `docs/contrat-interface.md`. Le Back et le Front le remplissent ensemble au début de chaque bloc.
- [ ] **T-07** Préparer un **jeu de données de démo** réaliste : 4 départements, une quinzaine de postes, un utilisateur par rôle, 3 demandes à différents statuts, 2 offres, une dizaine de candidats.
- [ ] **T-08** Point d'équipe court à la fin de chaque bloc : démo, problèmes, ajustements de ce fichier.
- [ ] **T-09** Préparer la soutenance / démo finale : scénario complet du besoin jusqu'à l'onboarding terminé.

---

## 6. Équipe Design

> Livrables attendus : parcours utilisateurs, wireframes (maquettes basse fidélité), maquettes finales, charte graphique, bibliothèque de composants.
> Outil conseillé : Figma (gratuit), avec une page par bloc.
> **Le Design doit toujours avoir au moins un bloc d'avance sur le Front.**

### 6.1 Fondations (avant tout le reste)

- [ ] **D-01** Lister les personas : Manager, RH, Direction, Employé nouvel arrivant, Candidat externe, Admin. Pour chacun : objectif, fréquence d'usage, ce qui l'inquiète.
- [ ] **D-02** Dessiner le **parcours global** (schéma) : besoin → validation → offre → candidature → entretiens → proposition → embauche → contrat → onboarding. Indiquer quel rôle agit à chaque étape.
- [ ] **D-03** Définir l'**arborescence** de l'application : menu principal par rôle (ce que chaque rôle voit dans la navigation).
- [ ] **D-04** Charte graphique :
  - [ ] palette de couleurs (principale, secondaire, fond, texte, succès, alerte, erreur, info) avec contraste suffisant (vérifier avec un outil de contraste, niveau AA) ;
  - [ ] typographie (une police pour les titres, une pour le texte, tailles de H1 à texte courant) ;
  - [ ] espacements (une échelle simple, par ex. 4 / 8 / 16 / 24 / 32) ;
  - [ ] icônes (choisir une seule bibliothèque d'icônes libre) ;
  - [ ] logo ou nom stylisé « Mr Tovo ».
- [ ] **D-05** **Code couleur des statuts**, le même partout : brouillon (gris), en attente / soumis (bleu), à compléter (orange), validé / accepté / fait (vert), refusé / rejeté (rouge), clôturé / terminé (gris foncé). Le livrer sous forme de tableau pour le Front.
- [ ] **D-06** Bibliothèque de composants (maquettes de chaque état : normal, survol, désactivé, erreur) :
  - [ ] boutons (principal, secondaire, danger, lien) ;
  - [ ] champs de formulaire (texte, nombre, date, liste déroulante, zone de texte, case à cocher, upload de fichier) avec message d'erreur ;
  - [ ] badge de statut ;
  - [ ] tableau avec tri, filtres et pagination ;
  - [ ] carte (pour les tableaux de bord) ;
  - [ ] fenêtre de confirmation (ex. « Refuser cette demande ? » avec commentaire) ;
  - [ ] messages flash (succès, erreur, info) ;
  - [ ] fil d'Ariane ;
  - [ ] **timeline / historique** (pour l'historique des validations, des contrats, des affectations) ;
  - [ ] **stepper** (étapes d'un workflow avec l'étape courante mise en avant) ;
  - [ ] barre de progression (onboarding) ;
  - [ ] état vide (« Aucune demande pour l'instant » + bouton d'action).
- [ ] **D-07** Gabarit de page commun : en-tête (logo, nom de l'utilisateur, rôle, cloche de notifications, déconnexion), menu latéral, zone de contenu, pied de page. Version **ordinateur** et version **mobile** (menu repliable).
- [ ] **D-08** Écran de **connexion** et page d'**erreur** (403 accès refusé, 404 page introuvable, 500 erreur serveur).

### 6.2 Bloc 1 — Besoin en personnel

- [ ] **D-11** Liste des **départements** et fiche département (avec la liste de ses postes).
- [ ] **D-12** Liste des **postes** avec filtres (département, statut) et fiche poste. La fiche montre l'**historique des titulaires** (affectations passées) pour illustrer que le poste existe sans l'employé.
- [ ] **D-13** Formulaire **nouvelle demande de recrutement** :
  - [ ] choix création / remplacement (le formulaire s'adapte : choisir un poste existant pour un remplacement, décrire le nouveau poste pour une création) ;
  - [ ] champs : département, effectif, motif, budget, date souhaitée ;
  - [ ] boutons « Enregistrer le brouillon » et « Soumettre ».
- [ ] **D-14** Liste « **Mes demandes** » (manager) et liste « **Demandes à valider** » (RH, Direction), avec badge de statut et filtres.
- [ ] **D-15** Fiche **détail d'une demande** : informations, stepper Manager → RH → Direction → Ouverture, historique des validations (timeline), boutons d'action selon le rôle (Valider / Refuser / Demander un complément).
- [ ] **D-16** Fenêtre de décision avec commentaire obligatoire pour un refus ou une demande de complément.

### 6.3 Bloc 2 — Recrutement

- [ ] **D-21** Formulaire **création d'offre** pré-rempli depuis la demande validée.
- [ ] **D-22** Liste des offres (côté RH) avec statut et nombre de candidatures.
- [ ] **D-23** **Page publique des offres** (sans connexion) et page publique **détail d'une offre** avec formulaire **« Postuler »** (identité, contact, CV, lettre). Prévoir la page de confirmation après envoi. Elle doit être très soignée, c'est la vitrine.
- [ ] **D-24** Liste des **candidatures d'une offre** : vue tableau **et** vue « tableau Kanban » par statut (Reçue / Présélectionnée / En entretien / Retenue / Rejetée).
- [ ] **D-25** **Fiche candidat** : identité, toutes ses candidatures (y compris sur d'autres offres), documents, historique.
- [ ] **D-26** **Fiche candidature** : CV consultable, statut, entretiens planifiés, évaluations, actions (présélectionner, rejeter avec motif, planifier un entretien, retenir).
- [ ] **D-27** Formulaire **planifier un entretien** (type, date, heure, lieu ou lien, évaluateurs).
- [ ] **D-28** Formulaire **évaluation** (critères notés, avis, commentaires) et vue **comparaison** des évaluations de plusieurs candidats pour une même offre.
- [ ] **D-29** Vue **agenda** des entretiens à venir (liste par jour suffit).

### 6.4 Bloc 3 — Sélection et embauche

- [ ] **D-31** Formulaire **proposition d'embauche** (salaire, date de début, type de contrat, date limite de réponse).
- [ ] **D-32** Écran d'enregistrement de la **réponse du candidat** (accepte / refuse + motif).
- [ ] **D-33** Écran de **récapitulatif après acceptation** : montre ce qui vient d'être créé (matricule, contrat brouillon, affectation, onboarding) avec liens vers chaque élément.
- [ ] **D-34** Liste des **employés** (recherche par nom ou matricule, filtres département / statut).
- [ ] **D-35** **Dossier salarié** organisé en onglets : Identité · Contrats · Affectations · Documents · Onboarding · Origine (lien vers sa candidature).

### 6.5 Bloc 4 — Contrat et onboarding

- [ ] **D-41** Onglet **Contrats** : timeline de tous les contrats (CDD → renouvellement → CDI), le contrat actif mis en avant, alerte visuelle pour un CDD qui se termine bientôt.
- [ ] **D-42** Formulaires **nouveau contrat**, **renouvellement** et **passage en CDI** (pré-remplis depuis le contrat précédent).
- [ ] **D-43** Onglet **Affectations** : historique des postes occupés.
- [ ] **D-44** **Checklist d'onboarding** : tâches groupées par catégorie (contrat signé, badge, compte, matériel, formation, inscription paie), responsable, échéance, barre de progression, tâches en retard en rouge.
- [ ] **D-45** Vue « **Mes tâches d'onboarding** » pour chaque responsable (ex. l'informaticien voit tous les comptes à créer).
- [ ] **D-46** Écran de **gestion des modèles de tâches** d'onboarding (RH).
- [ ] **D-47** Zone d'**upload et de consultation de documents** (glisser-déposer, liste, aperçu, téléchargement).

### 6.6 Transverse

- [ ] **D-51** Panneau des **notifications** (liste, non lues en gras, lien vers l'objet).
- [ ] **D-52** **Tableau de bord** par rôle :
  - [ ] Manager : mes demandes en cours, entretiens à venir, arrivées prévues dans mon équipe ;
  - [ ] RH : demandes à valider, offres ouvertes, candidatures à traiter, onboardings en retard, CDD qui se terminent ;
  - [ ] Direction : demandes à valider, budget demandé par département, nombre d'embauches.
- [ ] **D-53** Écran **administration** des utilisateurs et rôles.
- [ ] **D-54** Écran **journal d'audit** (filtres par date, utilisateur, type d'objet).
- [ ] **D-55** Faire **tester les maquettes** par 2 ou 3 personnes extérieures à l'équipe sur le parcours complet ; noter les difficultés et corriger.
- [ ] **D-56** Relire les écrans développés par le Front et lister les écarts avec les maquettes (une fois par bloc).

---

## 7. Équipe Back

> Livrables attendus : base de données, entités, règles métier, contrôleurs, sécurité, tests.
> **Le Back commence chaque bloc par le modèle et les services, et fournit tôt au Front des pages avec des données de démo**, pour que le Front ne soit jamais bloqué.

### 7.1 Socle technique

- [ ] **B-01** Organiser les packages du projet par couche **et** par domaine : entités, dépôts (accès aux données), services (règles métier), contrôleurs web (pages), contrôleurs API (REST), sécurité, configuration. Écrire la convention dans le README.
- [ ] **B-02** Ajouter les dépendances nécessaires dans le `pom.xml` : accès aux données JPA, pilote PostgreSQL, outil de migration de base (Flyway ou Liquibase), Spring Security, intégration Thymeleaf + Spring Security, envoi d'e-mails, base H2 pour les tests.
- [ ] **B-03** Base de données :
  - [ ] installer PostgreSQL en local (ou via Docker, avec un fichier de lancement partagé à toute l'équipe) ;
  - [ ] créer des profils de configuration : `dev` (base locale), `test` (base en mémoire), `prod` ;
  - [ ] ne **jamais** mettre de mot de passe en dur dans le dépôt (variables d'environnement).
- [ ] **B-04** Mettre en place les **migrations de base** versionnées (un fichier par évolution du schéma, jamais modifier une migration déjà partagée).
- [ ] **B-05** Champs communs à toutes les entités (création, modification, auteur) remplis automatiquement.
- [ ] **B-06** Gestion des erreurs centralisée : page d'erreur propre pour le web, réponse structurée pour l'API, messages en français.
- [ ] **B-07** Validation des formulaires côté serveur (champs obligatoires, formats, dates cohérentes) avec messages clairs en français. **Ne jamais faire confiance uniquement à la validation du Front.**
- [ ] **B-08** Chargement du **jeu de données de démo** (T-07) au démarrage en profil `dev` uniquement.
- [ ] **B-09** Logs applicatifs : niveau INFO pour les actions métier importantes, pas de données sensibles (mot de passe, salaire) dans les logs.

### 7.2 Authentification et droits

- [ ] **B-11** Entités Utilisateur et Rôle, avec mots de passe **chiffrés** (jamais en clair).
- [ ] **B-12** Connexion / déconnexion par formulaire, page de connexion fournie par le Front (F-04).
- [ ] **B-13** Protection des URL par rôle selon la matrice (section 4). Les pages publiques (offres, postuler, connexion, CSS/JS) restent accessibles sans connexion.
- [ ] **B-14** Contrôle **au niveau des données** : un manager ne voit que les demandes et candidatures de son département ; un employé ne voit que son propre dossier. Tester ces cas.
- [ ] **B-15** Rendre disponibles aux templates l'utilisateur connecté et ses rôles (pour que le Front adapte menu et boutons).
- [ ] **B-16** Protection CSRF active sur tous les formulaires (vérifier avec le Front que chaque formulaire l'envoie).
- [ ] **B-17** Écrans d'administration des utilisateurs : créer, désactiver, changer les rôles.

### 7.3 Moteur de workflow et services transverses

- [ ] **B-21** Concevoir un mécanisme **générique de transitions de statut** : pour chaque objet, la liste des transitions autorisées (section 3), qui peut les faire, et les conditions. Toute transition non prévue est refusée avec un message clair.
- [ ] **B-22** Fournir au Front, pour un objet et un utilisateur donnés, la **liste des actions possibles** (pour n'afficher que les bons boutons).
- [ ] **B-23** **Journal d'audit** : enregistrer automatiquement chaque création, modification et changement de statut (qui, quand, avant, après).
- [ ] **B-24** **Notifications** dans l'application : créer une notification à chaque étape importante (demande soumise → RH ; validée RH → Direction ; validée → manager ; entretien planifié → évaluateurs ; proposition acceptée → RH et manager ; tâche d'onboarding assignée → responsable). Compteur de non lues, marquer comme lu.
- [ ] **B-25** **E-mails** (optionnel au début, simulé en dev en écrivant dans les logs) : accusé de réception de candidature, convocation à un entretien, proposition d'embauche, rejet.
- [ ] **B-26** **Stockage des documents (GED)** :
  - [ ] enregistrer les fichiers dans un dossier configuré (pas dans la base, pas dans le dépôt Git) ;
  - [ ] renommer les fichiers stockés avec un identifiant unique ;
  - [ ] limiter les formats (PDF, images, Word) et la taille (ex. 5 Mo) ;
  - [ ] téléchargement réservé aux personnes autorisées.
- [ ] **B-27** Tâche planifiée quotidienne : clôturer les offres dont la date limite est passée, faire expirer les propositions sans réponse, alerter sur les CDD qui finissent dans 30 jours et les tâches d'onboarding en retard.

### 7.4 Bloc 1 — Besoin en personnel

- [ ] **B-31** Entités, migrations et dépôts : Département, Poste, DemandeRecrutement, Validation.
- [ ] **B-32** CRUD **Départements** (création, modification, liste, détail ; pas de suppression s'il a des postes).
- [ ] **B-33** CRUD **Postes** avec statut. Règle : on ne supprime pas un poste qui a eu des affectations, on le passe à « supprimé » ou « gelé ».
- [ ] **B-34** Service **DemandeRecrutement** :
  - [ ] création en brouillon par un manager (département du manager par défaut) ;
  - [ ] pour une **création** de poste : le poste est créé au statut « gelé » et devient « vacant » à l'ouverture du recrutement ;
  - [ ] pour un **remplacement** : vérifier que le poste existe ;
  - [ ] soumission, validation RH, validation Direction, refus, demande de complément, annulation, en passant par le moteur de workflow (B-21) ;
  - [ ] chaque décision crée une Validation ;
  - [ ] à l'ouverture du recrutement : création automatique d'une OffreEmploi en brouillon.
- [ ] **B-35** Contrôleurs des pages du bloc 1 (listes avec filtres et pagination, détail, formulaires) selon le contrat d'interface (T-06).
- [ ] **B-36** Tests du bloc 1 : chaque transition autorisée, chaque transition interdite, droits (un manager ne peut pas valider, la Direction ne peut pas valider avant les RH…).

### 7.5 Bloc 2 — Recrutement

- [ ] **B-41** Entités, migrations et dépôts : OffreEmploi, Candidat, Candidature, Entretien, ÉvaluationCandidat.
- [ ] **B-42** Service **OffreEmploi** : création depuis une demande validée (pré-remplissage), modification en brouillon, publication, clôture, annulation.
- [ ] **B-43** **Candidature publique** :
  - [ ] page publique avec uniquement les offres publiées et non expirées ;
  - [ ] dépôt de candidature avec CV (via B-26) ;
  - [ ] si l'e-mail du candidat existe déjà, **réutiliser le même Candidat** (une personne = un candidat, plusieurs candidatures) ;
  - [ ] empêcher de postuler deux fois à la même offre ;
  - [ ] protection simple contre les envois massifs (limite par adresse e-mail / délai).
- [ ] **B-44** Service **Candidature** : présélection, rejet (motif obligatoire), passage en entretien, retenue, retrait ; via le moteur de workflow.
- [ ] **B-45** Service **Entretien** : planification (vérifier que la date est dans le futur et que l'évaluateur n'a pas déjà un entretien au même créneau), report, annulation, marquer comme réalisé.
- [ ] **B-46** Service **Évaluation** : un évaluateur ne peut évaluer qu'un entretien auquel il est assigné ; une évaluation par évaluateur et par entretien ; calcul de la note moyenne d'une candidature.
- [ ] **B-47** Données pour la vue **comparaison** des candidats d'une offre (notes moyennes, avis, statut).
- [ ] **B-48** Contrôleurs des pages du bloc 2 (y compris la vue Kanban : candidatures regroupées par statut).
- [ ] **B-49** Tests du bloc 2 : réutilisation du candidat existant, double candidature refusée, offre expirée non visible, droits des évaluateurs.

### 7.6 Bloc 3 — Sélection et embauche

- [ ] **B-51** Entités, migrations et dépôts : PropositionEmbauche, Employé.
- [ ] **B-52** Service **PropositionEmbauche** : création pour une candidature retenue uniquement, envoi, enregistrement de la réponse, expiration.
- [ ] **B-53** **Génération du matricule** : format à décider en équipe (ex. année + numéro séquentiel sur 4 chiffres), unique, jamais réutilisé, jamais modifié.
- [ ] **B-54** **Processus d'embauche** déclenché par l'acceptation (section 3), dans une **seule transaction** : si une étape échoue, rien n'est créé.
  - [ ] Employé (lien vers le Candidat d'origine) ;
  - [ ] Contrat en brouillon ;
  - [ ] Affectation sur le Poste, Poste → « occupé » ;
  - [ ] Onboarding avec tâches générées (B-64) ;
  - [ ] Offre → « Pourvue » si l'effectif est atteint ; notifications.
- [ ] **B-55** Vérifications : un candidat déjà employé actif ne peut pas être réembauché sans que son dossier soit d'abord clôturé ; un poste déjà occupé ne peut pas recevoir une nouvelle affectation à plein temps sur la même période.
- [ ] **B-56** Pages **liste des employés** (recherche, filtres) et **dossier salarié** avec toutes ses sections.
- [ ] **B-57** Tests du bloc 3 : le scénario complet d'acceptation crée bien tous les objets ; un échec au milieu ne laisse rien à moitié créé ; unicité du matricule.

### 7.7 Bloc 4 — Contrat et onboarding

- [ ] **B-61** Entités, migrations et dépôts : Contrat, Affectation, Onboarding, OnboardingTâche, Document, ModèleTâcheOnboarding.
- [ ] **B-62** Service **Contrat** :
  - [ ] signature, activation, fin de contrat (motif) ;
  - [ ] **renouvellement** et **passage en CDI** = nouveau contrat lié au précédent, l'ancien passe à « terminé » ;
  - [ ] un contrat signé n'est plus modifiable ;
  - [ ] un seul contrat actif à la fois par employé ; dates de contrats successifs sans chevauchement ;
  - [ ] CDD : date de fin obligatoire ; CDI : pas de date de fin ;
  - [ ] le « type de contrat actuel » d'un employé est **calculé** à partir de son contrat actif, jamais stocké sur l'employé.
- [ ] **B-63** Service **Affectation** : changement de poste = clôture de l'affectation en cours + nouvelle affectation ; mise à jour du statut des postes concernés ; à la sortie d'un employé, clôturer son affectation et repasser le poste en « vacant ».
- [ ] **B-64** **Modèles de tâches d'onboarding** : CRUD pour les RH ; jeu par défaut : contrat signé, badge, compte informatique, matériel, formation d'accueil, inscription paie. À la création d'un Onboarding, générer les tâches avec échéance = date d'arrivée + délai.
- [ ] **B-65** Service **Onboarding** : changer le statut d'une tâche (seul le responsable ou les RH), joindre un justificatif, calculer la progression, terminer automatiquement l'onboarding quand toutes les tâches obligatoires sont faites ou non applicables.
- [ ] **B-66** Lier la tâche « contrat signé » au contrat : cocher la tâche demande que le contrat soit au statut « signé » (et/ou qu'un document contrat signé soit joint).
- [ ] **B-67** Documents rattachés à l'employé, au contrat et aux tâches (via B-26).
- [ ] **B-68** Pages du bloc 4 : onglets contrats / affectations / documents / onboarding, « mes tâches d'onboarding », gestion des modèles.
- [ ] **B-69** Tests du bloc 4 : CDD → renouvellement → CDI conserve bien les 3 contrats ; refus d'un chevauchement ; fin automatique de l'onboarding ; un employé ne voit que son propre dossier.

### 7.8 Reporting, API et finitions

- [ ] **B-71** Données des **tableaux de bord** par rôle (D-52) : nombre de demandes par statut, délai moyen de validation, offres ouvertes, candidatures par offre, délai moyen de recrutement (demande → embauche), onboardings en retard, CDD qui se terminent.
- [ ] **B-72** **Export CSV** des listes principales (demandes, candidatures, employés).
- [ ] **B-73** **API REST** en lecture (au minimum : offres publiées, employés, tâches d'onboarding de l'utilisateur connecté) pour un futur front mobile ; protégée par l'authentification ; documentée (OpenAPI / Swagger).
- [ ] **B-74** Pagination et tri sur toutes les listes longues ; vérifier qu'aucune page ne charge des milliers de lignes d'un coup.
- [ ] **B-75** Tests de bout en bout du **scénario complet** : demande → validations → offre → candidature → entretien → évaluation → proposition → acceptation → contrat signé → onboarding terminé.
- [ ] **B-76** Relecture sécurité : droits sur chaque URL, upload de fichiers, aucune donnée sensible dans les logs ou les messages d'erreur.
- [ ] **B-77** Mettre à jour le `README.md` : installation de la base, profils, comptes de démo, lancement.

---

## 8. Équipe Front

> Livrables attendus : templates Thymeleaf, CSS, JavaScript, en suivant les maquettes du Design et le contrat d'interface avec le Back.
> **Le Front peut commencer chaque écran en HTML/CSS statique avec des données fictives**, puis le brancher sur les vraies données dès que le Back les fournit. Ne jamais rester bloqué à attendre.

### 8.1 Socle

- [ ] **F-01** Organiser les templates : un dossier par domaine (`departements/`, `postes/`, `demandes/`, `offres/`, `candidatures/`, `employes/`, `onboarding/`, `admin/`, `public/`, `erreurs/`) et un dossier `fragments/` pour les morceaux réutilisables.
- [ ] **F-02** Organiser le CSS : variables de la charte (couleurs, polices, espacements de D-04), styles de base, composants, pages. Décider en équipe si on utilise un framework CSS (Bootstrap, Tailwind…) ou du CSS maison, et s'y tenir.
- [ ] **F-03** **Gabarit commun** (D-07) dans le layout : en-tête, menu latéral, contenu, pied de page. Le **menu change selon le rôle** de l'utilisateur connecté (B-15). Version mobile avec menu repliable.
- [ ] **F-04** Page de **connexion** (avec message d'erreur si identifiants faux, message après déconnexion).
- [ ] **F-05** Pages d'**erreur** 403, 404, 500.
- [ ] **F-06** **Fragments réutilisables** (un fragment = un composant de D-06) :
  - [ ] badge de statut (prend un statut, affiche le libellé et la couleur de D-05) ;
  - [ ] messages flash (succès / erreur après une action) ;
  - [ ] champ de formulaire avec libellé et affichage de l'erreur de validation renvoyée par le Back ;
  - [ ] pagination ;
  - [ ] fil d'Ariane ;
  - [ ] timeline / historique ;
  - [ ] stepper de workflow ;
  - [ ] barre de progression ;
  - [ ] état vide ;
  - [ ] fenêtre de confirmation avec commentaire.
- [ ] **F-07** Petit JavaScript commun : ouverture/fermeture des fenêtres de confirmation, menu mobile, confirmation avant une action irréversible, désactivation du bouton après un clic (éviter le double envoi).
- [ ] **F-08** Formats français partout : dates `jj/mm/aaaa`, montants `1 234,56 €`, libellés et messages en français.
- [ ] **F-09** Vérifier que **chaque formulaire** envoie le jeton CSRF (B-16).
- [ ] **F-10** Accessibilité de base : chaque champ a un libellé, images avec texte alternatif, navigation au clavier possible, contraste respecté.

### 8.2 Bloc 1 — Besoin en personnel

- [ ] **F-11** Liste et fiche **département** (D-11).
- [ ] **F-12** Liste des **postes** avec filtres, fiche poste avec historique des titulaires (D-12) ; formulaires création / modification (RH).
- [ ] **F-13** Formulaire **demande de recrutement** (D-13) : la partie « poste » change selon création / remplacement (petit JavaScript pour afficher/masquer) ; boutons brouillon / soumettre.
- [ ] **F-14** Listes « Mes demandes » et « Demandes à valider » (D-14) avec filtres et badges.
- [ ] **F-15** Détail d'une demande (D-15) : stepper, timeline des validations, **uniquement les boutons d'action autorisés** (fournis par le Back, B-22).
- [ ] **F-16** Fenêtre de décision avec commentaire obligatoire (D-16), message d'erreur si vide.

### 8.3 Bloc 2 — Recrutement

- [ ] **F-21** Formulaire offre pré-rempli (D-21) et liste des offres côté RH (D-22).
- [ ] **F-22** **Pages publiques** (D-23) : liste des offres, détail, formulaire « Postuler » avec upload du CV (vérifier format et taille côté navigateur aussi, pour un retour immédiat), page de confirmation. Ces pages utilisent un gabarit public **sans** le menu interne.
- [ ] **F-23** Liste des candidatures d'une offre : vue tableau **et** vue Kanban (D-24). Le Kanban peut être en lecture seule au départ (changement de statut via les boutons de la fiche).
- [ ] **F-24** Fiche candidat (D-25) et fiche candidature (D-26) avec consultation / téléchargement du CV.
- [ ] **F-25** Formulaire de planification d'entretien (D-27) et vue agenda (D-29).
- [ ] **F-26** Formulaire d'évaluation (D-28) et vue comparaison des candidats.

### 8.4 Bloc 3 — Sélection et embauche

- [ ] **F-31** Formulaire proposition d'embauche (D-31) et enregistrement de la réponse (D-32).
- [ ] **F-32** Écran récapitulatif après acceptation (D-33) avec liens vers l'employé, le contrat, l'affectation, l'onboarding.
- [ ] **F-33** Liste des employés avec recherche et filtres (D-34).
- [ ] **F-34** Dossier salarié avec **onglets** (D-35) ; l'onglet actif reste sélectionné après un rechargement de page.

### 8.5 Bloc 4 — Contrat et onboarding

- [ ] **F-41** Onglet Contrats : timeline, contrat actif en avant, alerte fin de CDD (D-41).
- [ ] **F-42** Formulaires nouveau contrat / renouvellement / passage en CDI (D-42) : la date de fin est masquée pour un CDI et obligatoire pour un CDD.
- [ ] **F-43** Onglet Affectations (D-43).
- [ ] **F-44** **Checklist d'onboarding** (D-44) : tâches par catégorie, changement de statut, justificatif, barre de progression, retards en rouge.
- [ ] **F-45** Page « Mes tâches d'onboarding » (D-45).
- [ ] **F-46** Gestion des modèles de tâches (D-46).
- [ ] **F-47** Composant d'upload et liste de documents (D-47), réutilisé partout (candidature, employé, contrat, tâche).

### 8.6 Transverse et finitions

- [ ] **F-51** Cloche et panneau de **notifications** (D-51) avec compteur de non lues.
- [ ] **F-52** **Tableaux de bord** par rôle (D-52) : cartes de chiffres clés et quelques graphiques simples (bibliothèque de graphiques légère au choix de l'équipe).
- [ ] **F-53** Pages d'administration des utilisateurs (D-53) et journal d'audit (D-54).
- [ ] **F-54** Boutons d'**export CSV** sur les listes (B-72).
- [ ] **F-55** Test **responsive** de toutes les pages sur téléphone (outils développeur du navigateur) ; corriger les tableaux trop larges (défilement horizontal ou affichage en cartes).
- [ ] **F-56** Passe de cohérence avec les maquettes (avec D-56) : couleurs, espacements, libellés, icônes.
- [ ] **F-57** Vérifier sur tous les navigateurs courants (Chrome, Firefox, Edge) ; aucune erreur dans la console JavaScript.
- [ ] **F-58** Relire tous les textes affichés : orthographe, cohérence des termes (toujours « demande de recrutement », jamais un autre mot pour la même chose — voir glossaire).

---

## 9. Planning conseillé et dépendances

> Durées indicatives pour une équipe de 4 à 6 personnes. Adapter selon le temps disponible.
> Si le temps manque, livrer **un parcours complet simple** plutôt que trois blocs parfaits et un bloc absent.

| Étape | Design | Back | Front |
|---|---|---|---|
| **Semaine 1** — Démarrage | D-01 → D-08 | B-01 → B-09, B-11 → B-13 | F-01, F-02, puis F-03 → F-06 en statique à partir des premières maquettes |
| **Semaine 2** — Bloc 1 | D-11 → D-16, commence D-21 | B-14 → B-16, B-21 → B-23, B-31 → B-36 | F-07 → F-10, F-11 → F-16 |
| **Semaine 3** — Bloc 2 | D-21 → D-29, commence D-31 | B-24, B-26, B-41 → B-49 | F-21 → F-26 |
| **Semaine 4** — Bloc 3 | D-31 → D-35, commence D-41 | B-51 → B-57 | F-31 → F-34 |
| **Semaine 5** — Bloc 4 | D-41 → D-47 | B-61 → B-69, B-27 | F-41 → F-47 |
| **Semaine 6** — Transverse et finitions | D-51 → D-56 | B-17, B-25, B-71 → B-77 | F-51 → F-58 |
| **Fin** | | T-09 : démo du scénario complet | |

### Dépendances clés (qui attend qui)

- Le **Front** attend du **Design** : la charte (D-04, D-05) et le gabarit (D-07) avant F-02 / F-03 ; la maquette de chaque écran avant de le finaliser.
- Le **Front** attend du **Back** : la connexion (B-12) et l'utilisateur connecté (B-15) avant de brancher le menu par rôle ; la liste des actions possibles (B-22) avant de brancher les boutons de workflow ; le stockage de fichiers (B-26) avant les uploads.
- Le **Back** attend du **Design** : rien de bloquant, mais les maquettes aident à savoir quelles données chaque page affiche.
- Tout le monde attend **T-02** (modèle validé) et **T-06** (contrat d'interface du bloc en cours).
- Le **bloc 3** dépend des blocs 1 et 2 ; le **bloc 4** dépend du bloc 3 (B-54 crée contrat, affectation et onboarding).

### Comment ne jamais être bloqué

- Front en avance sur le Back : faire l'écran en statique avec des données inventées, le brancher plus tard.
- Back en avance sur le Front : faire une page minimale sans style pour tester, ou écrire les tests.
- Design en avance : faire les écrans du bloc suivant, puis les tests utilisateurs (D-55).
- Bloqué plus d'une demi-journée : le dire au point d'équipe ou sur le groupe, ne pas attendre.

---

## 10. Définition de « terminé »

Une tâche est terminée seulement si **tout** est vrai :

- [ ] Elle fait ce qui est décrit dans ce fichier (et dans la maquette pour un écran).
- [ ] Le projet démarre et `./mvnw test` passe.
- [ ] Les règles des sections 3 et 4 sont respectées (statuts et droits).
- [ ] Les messages d'erreur sont compréhensibles et en français.
- [ ] Une autre personne a relu la pull request.
- [ ] Le travail est fusionné dans la branche principale.
- [ ] La case est cochée dans ce fichier.

---

## 11. Glossaire

| Terme | Signification |
|---|---|
| **Poste** | Une place dans l'organisation (ex. « Comptable senior, département Finance »). Existe même sans titulaire. |
| **Employé / salarié** | Une personne embauchée, avec un matricule. |
| **Candidat** | Une personne qui a postulé au moins une fois. N'est pas un employé. |
| **Candidature** | Le fait qu'un candidat postule à une offre précise. |
| **Demande de recrutement** | La demande d'un manager pour créer ou remplacer un poste. |
| **Validation** | Une décision (RH ou Direction) sur une demande de recrutement. |
| **Offre d'emploi** | L'annonce publiée, issue d'une demande validée. |
| **Proposition d'embauche** | L'offre concrète faite au candidat retenu (salaire, date, type de contrat). |
| **Matricule** | Identifiant unique et définitif d'un employé. |
| **Contrat** | Un contrat de travail avec ses dates. Un employé en a plusieurs au fil du temps. |
| **Affectation** | Le fait qu'un employé occupe un poste pendant une période. |
| **Onboarding** | Le processus d'intégration d'un nouvel employé, suivi par une checklist. |
| **GED** | Gestion électronique des documents : stockage des fichiers (CV, contrats…). |
| **Workflow** | Suite d'étapes et de statuts avec des règles sur qui peut passer d'une étape à l'autre. |
| **CRUD** | Créer, lire, modifier, supprimer : les opérations de base sur un objet. |
| **Contrat d'interface** | Accord écrit entre Back et Front sur ce qu'une page reçoit et envoie. |
