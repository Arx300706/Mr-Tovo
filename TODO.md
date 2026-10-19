# TODO — Mr Tovo : application de gestion RH

**Délai : du lundi 19 au vendredi 23 octobre.** Trois équipes : **Design**, **Back** et **Front**.

Le travail est découpé en **pistes** indépendantes : chaque personne prend une piste et peut avancer **en même temps que les autres**, sans attendre personne.
Pour que ce soit possible, on fixe **lundi matin** tout ce qui est partagé : modèle de données, statuts, droits, URL des pages, propriétaires des fichiers. Après ça, chacun travaille dans son coin et on intègre tous les jours.

> Rappel de la stack (voir `README.md`) : Java 21, Spring Boot 4, Thymeleaf, Maven.
> Le **Back** s'occupe des données, des règles métier, des contrôleurs et de la sécurité.
> Le **Front** s'occupe des templates Thymeleaf, du CSS et du JavaScript.
> Le **Design** s'occupe des parcours, des maquettes et de la charte graphique.

---

## Sommaire

0. [Comment utiliser ce fichier](#0-comment-utiliser-ce-fichier)
1. [Vue d'ensemble du projet](#1-vue-densemble-du-projet)
2. [Modèle de données de référence](#2-modèle-de-données-de-référence)
3. [Statuts et workflows](#3-statuts-et-workflows)
4. [Rôles et droits](#4-rôles-et-droits)
5. [Organisation en pistes parallèles](#5-organisation-en-pistes-parallèles)
6. [Lundi matin : le lancement commun](#6-lundi-matin--le-lancement-commun)
7. [Contrat d'interface Back ↔ Front](#7-contrat-dinterface-back--front)
8. [Équipe Design](#8-équipe-design)
9. [Équipe Back](#9-équipe-back)
10. [Équipe Front](#10-équipe-front)
11. [Planning jour par jour](#11-planning-jour-par-jour)
12. [Priorités : ce qu'on coupe si on manque de temps](#12-priorités--ce-quon-coupe-si-on-manque-de-temps)
13. [Définition de « terminé »](#13-définition-de--terminé-)
14. [Glossaire](#14-glossaire)

---

## 0. Comment utiliser ce fichier

- [ ] Chaque personne choisit **une piste** (section 5) et met son prénom à côté : `Piste BK2 — Bloc 2 (@prénom)`.
- [ ] Chaque tâche indique : **priorité** (P1 / P2 / P3, voir section 12), **jour prévu**, **dépend de** (ce qu'il faut attendre, souvent « rien »).
- [ ] Toujours faire les tâches **P1 d'abord**, dans l'ordre de la piste.
- [ ] Une tâche finie est cochée `- [x]`. Une tâche bloquée est marquée `⛔` avec la raison.
- [ ] Une branche Git par tâche ou petit groupe de tâches, nommée avec l'identifiant (ex. `BK2-04-candidature-publique`). Messages de commit qui commencent par l'identifiant.
- [ ] On ne modifie **que les fichiers de sa piste** (section 5.3). Pour un fichier partagé, on demande à son propriétaire.
- [ ] En cas de doute sur une règle métier : sections 2, 3 et 4. Si la réponse n'y est pas, on décide vite à deux ou trois et **on l'écrit ici**.

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

## 5. Organisation en pistes parallèles

### 5.1 Les 9 pistes

| Piste | Équipe | Sujet | Écrans / objets couverts |
|---|---|---|---|
| **DS1** | Design | Système visuel + Blocs 1 et 4 + transverse | charte, composants, gabarit, connexion, demandes, postes, contrats, onboarding, tableau de bord |
| **DS2** | Design | Parcours + Blocs 2 et 3 + soutenance | personas, parcours, offres, page publique, candidatures, entretiens, proposition, dossier salarié, support de présentation |
| **BK0** | Back | Socle, sécurité, workflow, services transverses | configuration, base, connexion, rôles, moteur de statuts, fichiers, notifications, audit |
| **BK1** | Back | Bloc 1 — Besoin en personnel | Département, Poste, DemandeRecrutement, Validation |
| **BK2** | Back | Bloc 2 — Recrutement | OffreEmploi, Candidat, Candidature, Entretien, ÉvaluationCandidat |
| **BK3** | Back | Blocs 3 et 4 — Embauche, contrat, onboarding | PropositionEmbauche, Employé, Contrat, Affectation, Onboarding, OnboardingTâche, ModèleTâche |
| **FR0** | Front | Socle + Bloc 1 + transverse | gabarit, CSS commun, fragments, connexion, erreurs, pages du bloc 1, notifications, tableau de bord |
| **FR1** | Front | Bloc 2 + pages publiques | offres, page carrières publique, candidatures, entretiens, évaluations |
| **FR2** | Front | Blocs 3 et 4 | proposition, employés, dossier salarié, contrats, onboarding |

**Moins de 9 personnes ?** Une personne prend plusieurs pistes de la même équipe, en suivant l'ordre de chacune. Regroupements conseillés : BK0 + BK1, BK2 + BK3, FR0 + FR1, DS1 + DS2.
**Plus de 9 personnes ?** Découper BK3 en « Bloc 3 » et « Bloc 4 », et FR2 pareil.

### 5.2 Les 5 règles qui permettent de travailler en parallèle

1. **Tout ce qui est partagé est décidé lundi matin** (section 6) et écrit dans ce fichier : modèle, statuts, droits, URL, noms des templates, noms des données.
2. **Lundi 16h, toutes les entités existent** dans la branche principale (avec leurs champs et leurs liens, même sans aucune règle métier). Après ça, chaque piste Back peut utiliser les objets des autres pistes sans les attendre.
3. **Le Front n'attend pas le Back.** Les templates Thymeleaf peuvent s'ouvrir directement dans le navigateur s'ils contiennent du texte d'exemple (on parle de « templates naturels ») : on construit chaque page avec des données d'exemple, puis on la branche quand le contrôleur arrive. Les noms des données suivent la convention de la section 7.
4. **Le Front n'attend pas le Design.** Lundi, on choisit un framework CSS (Bootstrap conseillé : rapide et déjà responsive). Le Front monte les pages avec ses composants et la description des écrans de ce fichier ; les maquettes du Design arrivent ensuite et on ajuste le style.
5. **On intègre tous les jours à 17h30** : chacun fusionne son travail du jour dans la branche principale, qui doit toujours démarrer et passer les tests. Chaque matin, chacun récupère la branche principale avant de commencer.

### 5.3 Qui est propriétaire de quels fichiers

Pour éviter les conflits Git, chaque fichier a **un seul propriétaire**. Les autres peuvent proposer une modification, mais c'est le propriétaire qui la fait (ou qui valide la pull request).

| Fichiers / dossiers | Propriétaire |
|---|---|
| `pom.xml`, `application.properties` et profils, configuration de sécurité, classes de base communes aux entités | **BK0** |
| Package Java du domaine « commun » (utilisateurs, rôles, notifications, audit, documents, workflow) | **BK0** |
| Package Java du domaine « besoin » (départements, postes, demandes, validations) | **BK1** |
| Package Java du domaine « recrutement » (offres, candidats, candidatures, entretiens, évaluations) | **BK2** |
| Packages Java des domaines « embauche » et « onboarding » (propositions, employés, contrats, affectations, onboarding) | **BK3** |
| Données de démo : **un fichier par domaine**, chacun remplit le sien | chaque piste Back |
| `templates/fragments/` (gabarit, composants réutilisables), `static/css/style.css`, `static/js/app.js` | **FR0** |
| `templates/auth/`, `templates/erreurs/`, `templates/dashboard/`, `templates/notifications/`, `templates/departements/`, `templates/postes/`, `templates/demandes/` | **FR0** |
| `templates/public/`, `templates/offres/`, `templates/candidatures/`, `templates/candidats/`, `templates/entretiens/`, `templates/evaluations/` + `static/css/recrutement.css` | **FR1** |
| `templates/propositions/`, `templates/employes/`, `templates/contrats/`, `templates/onboarding/` + `static/css/rh.css` | **FR2** |
| Maquettes Figma : une page par bloc | **DS1** (charte, composants, blocs 1 et 4), **DS2** (parcours, blocs 2 et 3) |
| `TODO.md` | tout le monde (on ne modifie que les cases et les prénoms de sa piste) |

### 5.4 Rituels de l'équipe (courts !)

- **9h00 — point du matin (10 min, debout)** : chacun dit ce qu'il a fini, ce qu'il fait aujourd'hui, ce qui le bloque.
- **13h30 — point rapide entre pistes jumelles (5 min)** : BK1 ↔ FR0, BK2 ↔ FR1, BK3 ↔ FR2, DS1 ↔ FR0, DS2 ↔ FR1/FR2. On vérifie qu'on parle des mêmes données.
- **17h30 — intégration** : tout le monde fusionne, une personne (tournante) lance l'application et clique sur le parcours pour vérifier que rien n'est cassé.
- **Bloqué plus de 30 minutes ?** On le dit tout de suite sur le groupe, on n'attend pas le lendemain.

---

## 6. Lundi matin : le lancement commun

**Lundi 9h00 → 10h30, toute l'équipe ensemble.** C'est la seule étape où tout le monde doit être présent. Elle débloque toute la semaine.

- [ ] **T-01** (15 min) Tout le monde a cloné le projet et l'a lancé une fois en local (voir `README.md`).
- [ ] **T-02** (20 min) Relire et valider le **modèle de données** (section 2). Toute modification est écrite dans ce fichier tout de suite.
- [ ] **T-03** (10 min) Valider les **statuts et transitions** (section 3) et la **matrice des droits** (section 4).
- [ ] **T-04** (10 min) Valider le **contrat d'interface** (section 7) : URL, noms des templates, convention des noms de données.
- [ ] **T-05** (10 min) Décisions techniques, à écrire dans ce fichier :
  - [ ] base de données en développement : **H2 en fichier** conseillé (rien à installer, personne n'est bloqué) ; **PostgreSQL** pour la démo finale si le temps le permet ;
  - [ ] schéma de base généré automatiquement depuis les entités pendant la semaine (plus simple que des migrations versionnées pour 5 jours) ;
  - [ ] framework CSS : Bootstrap conseillé ;
  - [ ] format du matricule (ex. année + numéro sur 4 chiffres) ;
  - [ ] comptes de démo : un par rôle, même mot de passe simple pour tous en développement.
- [ ] **T-06** (10 min) Chacun choisit sa piste (section 5.1) et écrit son prénom.
- [ ] **T-07** (5 min) Règles Git : branche principale protégée, pull request courte, relue par **une** autre personne (de préférence la piste jumelle), fusion au plus tard à 17h30.
- [ ] **T-08** (5 min) Créer le groupe de discussion de l'équipe et un tableau de suivi si besoin (ou utiliser ce fichier).

**Ensuite, tâches communes de fin de semaine :**

- [ ] **T-09** Jeudi 16h : **gel des fonctionnalités**. Après, on ne fait plus que corriger et peaufiner.
- [ ] **T-10** Jeudi 16h → 18h : tout le monde teste le **scénario complet** (section 11, jeudi) et note les bugs dans ce fichier.
- [ ] **T-11** Vendredi matin : jeu de données de démo final, répétition de la démo.
- [ ] **T-12** Vendredi : rendu et soutenance.

---

## 7. Contrat d'interface Back ↔ Front

Ce tableau est **l'accord entre Back et Front**. Le Back crée le contrôleur de chaque URL et renvoie vers le template indiqué ; le Front crée le template à cet endroit. Les deux peuvent avancer en même temps.

### 7.1 Convention des noms de données passées aux pages

Pour ne pas avoir à se mettre d'accord écran par écran :

- une **liste** d'objets porte le nom de l'objet **au pluriel**, en minuscules : `demandes`, `offres`, `candidatures`, `employes`… ;
- **un** objet porte le nom **au singulier** : `demande`, `offre`, `candidature`, `employe`… ;
- l'objet d'un **formulaire** s'appelle toujours `formulaire` ;
- les **actions autorisées** pour l'utilisateur sur l'objet affiché s'appellent toujours `actionsPossibles` (liste de codes : `SOUMETTRE`, `VALIDER`, `REFUSER`…) ;
- les **listes pour les menus déroulants** portent le nom de l'objet au pluriel (`departements`, `postes`, `typesContrat`…) ;
- l'**utilisateur connecté** et ses rôles sont disponibles sur **toutes** les pages (fourni par BK0) ;
- le **message flash** après une action s'appelle `succes` ou `erreur` ;
- les champs d'un objet portent **exactement** les noms de la section 2 (sans accents, première lettre en minuscule : `dateSouhaitee`, `effectif`, `motif`…).

### 7.2 Liste des écrans

Les actions (boutons) sont des envois de formulaire vers l'URL indiquée, puis retour sur la page de détail avec un message flash.

#### Commun (BK0 ↔ FR0)

| Écran | URL | Template | Prio |
|---|---|---|---|
| Connexion | `/connexion` | `auth/connexion` | P1 |
| Accueil (tableau de bord par rôle) | `/` | `dashboard/accueil` | P1 simple / P2 complet |
| Erreurs | — | `erreurs/403`, `erreurs/404`, `erreurs/500` | P1 |
| Téléchargement d'un document | `/documents/{id}` | — (fichier) | P1 |
| Notifications | `/notifications` | `notifications/liste` | P2 |
| Journal d'audit | `/admin/audit` | `admin/audit` | P3 |
| Utilisateurs | `/admin/utilisateurs` | `admin/utilisateurs` | P3 |

#### Bloc 1 (BK1 ↔ FR0)

| Écran | URL | Template | Prio |
|---|---|---|---|
| Liste des demandes (filtre « mes demandes » / « à valider » selon le rôle) | `/demandes` | `demandes/liste` | P1 |
| Nouvelle demande / modifier un brouillon | `/demandes/nouvelle`, `/demandes/{id}/modifier` | `demandes/formulaire` | P1 |
| Détail d'une demande | `/demandes/{id}` | `demandes/detail` | P1 |
| Actions | `/demandes/{id}/soumettre`, `…/valider`, `…/refuser`, `…/completer`, `…/annuler`, `…/ouvrir-recrutement` | — | P1 |
| Liste des postes | `/postes` | `postes/liste` | P1 |
| Fiche poste (avec historique des titulaires) | `/postes/{id}` | `postes/detail` | P2 |
| Créer / modifier un poste | `/postes/nouveau`, `/postes/{id}/modifier` | `postes/formulaire` | P2 |
| Liste et fiche des départements | `/departements`, `/departements/{id}` | `departements/liste`, `departements/detail` | P2 |

#### Bloc 2 (BK2 ↔ FR1)

| Écran | URL | Template | Prio |
|---|---|---|---|
| Liste des offres (RH) | `/offres` | `offres/liste` | P1 |
| Détail d'une offre | `/offres/{id}` | `offres/detail` | P1 |
| Modifier une offre (brouillon) | `/offres/{id}/modifier` | `offres/formulaire` | P1 |
| Actions offre | `/offres/{id}/publier`, `…/cloturer`, `…/annuler` | — | P1 |
| **Public** : liste des offres | `/carrieres` | `public/offres` | P1 |
| **Public** : détail + formulaire « Postuler » | `/carrieres/{id}` (envoi vers `/carrieres/{id}/postuler`) | `public/offre-detail` | P1 |
| **Public** : confirmation | `/carrieres/merci` | `public/confirmation` | P1 |
| Candidatures d'une offre (tableau) | `/offres/{id}/candidatures` | `candidatures/liste` | P1 |
| Candidatures en vue Kanban | `/offres/{id}/candidatures?vue=kanban` | `candidatures/kanban` | P3 |
| Fiche candidature | `/candidatures/{id}` | `candidatures/detail` | P1 |
| Actions candidature | `/candidatures/{id}/preselectionner`, `…/rejeter`, `…/retenir` | — | P1 |
| Planifier un entretien | `/candidatures/{id}/entretiens/nouveau` | `entretiens/formulaire` | P1 |
| Saisir une évaluation | `/entretiens/{id}/evaluation` | `evaluations/formulaire` | P1 |
| Fiche candidat (toutes ses candidatures) | `/candidats/{id}` | `candidats/detail` | P2 |
| Comparaison des candidats d'une offre | `/offres/{id}/comparaison` | `evaluations/comparaison` | P2 |
| Agenda des entretiens | `/entretiens` | `entretiens/agenda` | P3 |

#### Blocs 3 et 4 (BK3 ↔ FR2)

| Écran | URL | Template | Prio |
|---|---|---|---|
| Créer une proposition d'embauche | `/candidatures/{id}/proposition` | `propositions/formulaire` | P1 |
| Détail d'une proposition (+ boutons réponse du candidat) | `/propositions/{id}` | `propositions/detail` | P1 |
| Actions proposition | `/propositions/{id}/envoyer`, `…/accepter`, `…/refuser` | — | P1 |
| Récapitulatif après acceptation | `/propositions/{id}/recapitulatif` | `propositions/recapitulatif` | P2 (en P1 : redirection vers le dossier salarié) |
| Liste des employés | `/employes` | `employes/liste` | P1 |
| Dossier salarié (onglet choisi par `?onglet=identite|contrats|affectations|documents|onboarding`) | `/employes/{id}` | `employes/dossier` | P1 |
| Nouveau contrat / renouvellement / passage en CDI (`?type=nouveau|renouvellement|cdi`) | `/employes/{id}/contrats/nouveau` | `contrats/formulaire` | P1 |
| Actions contrat | `/contrats/{id}/signer`, `…/terminer` | — | P1 |
| Checklist d'onboarding | `/onboardings/{id}` | `onboarding/checklist` | P1 |
| Changer le statut d'une tâche | `/onboarding-taches/{id}/statut` | — | P1 |
| Changer de poste (nouvelle affectation) | `/employes/{id}/affectations/nouvelle` | `contrats/affectation-formulaire` | P2 |
| Mes tâches d'onboarding | `/mes-taches` | `onboarding/mes-taches` | P2 |
| Modèles de tâches | `/admin/modeles-onboarding` | `onboarding/modeles` | P3 |

---

## 8. Équipe Design

> **Principe pour la semaine :** des maquettes **basse fidélité de tous les écrans P1 dès lundi soir** (même rapides), pour que le Front sache quoi construire dès mardi. La haute fidélité vient ensuite, écran par écran, dans le même ordre que le Front.
> Outil conseillé : Figma (gratuit), une page par bloc. Partager le lien dans le groupe dès lundi.

### Piste DS1 — Système visuel + Blocs 1 et 4 + transverse

- [ ] **DS1-01** · P1 · lundi 10h30-12h · dépend de : rien
  **Mini-charte** livrée à FR0 **avant midi** : palette (principale, secondaire, fond, texte, succès, alerte, erreur, info) avec contraste suffisant, typographie (titres / texte), échelle d'espacements, bibliothèque d'icônes choisie. Rester compatible avec Bootstrap (on change ses couleurs, on ne refait pas tout).
- [ ] **DS1-02** · P1 · lundi avant midi · dépend de : rien
  **Code couleur des statuts**, le même partout : brouillon (gris), soumis / en attente (bleu), à compléter (orange), validé / accepté / fait (vert), refusé / rejeté (rouge), clôturé / terminé (gris foncé). Le livrer sous forme de tableau « statut → couleur → libellé affiché ».
- [ ] **DS1-03** · P1 · lundi après-midi · dépend de : DS1-01
  **Gabarit de page** : en-tête (logo « Mr Tovo », nom et rôle de l'utilisateur, cloche, déconnexion), menu latéral par rôle, zone de contenu. Version ordinateur et mobile (menu repliable). Plus l'écran de **connexion**.
- [ ] **DS1-04** · P1 · lundi après-midi · dépend de : rien
  **Basse fidélité** des écrans P1 du **bloc 1** : liste des demandes, formulaire de demande (la partie poste change entre création et remplacement), détail d'une demande (stepper Manager → RH → Direction → Ouverture, historique des validations, boutons d'action), fenêtre de refus avec commentaire obligatoire, liste des postes.
- [ ] **DS1-05** · P1 · lundi fin d'après-midi · dépend de : rien
  **Basse fidélité** des écrans P1 du **bloc 4** : onglet contrats du dossier salarié (frise des contrats, contrat actif mis en avant), formulaire contrat (date de fin masquée pour un CDI), checklist d'onboarding (tâches par catégorie, responsable, échéance, barre de progression, retards en rouge).
- [ ] **DS1-06** · P1 · mardi matin · dépend de : DS1-01
  **Composants** (chaque état : normal, survol, désactivé, erreur) : boutons, champs de formulaire avec message d'erreur, badge de statut, tableau avec filtres et pagination, fenêtre de confirmation, messages flash, fil d'Ariane, frise / historique, stepper, barre de progression, état vide.
- [ ] **DS1-07** · P1 · mardi après-midi · dépend de : DS1-04, DS1-06
  **Haute fidélité** des écrans du bloc 1.
- [ ] **DS1-08** · P1 · mercredi matin · dépend de : DS1-05, DS1-06
  **Haute fidélité** des écrans du bloc 4.
- [ ] **DS1-09** · P2 · mercredi après-midi · dépend de : DS1-06
  **Tableau de bord** par rôle : Manager (mes demandes, mes arrivées prévues), RH (demandes à valider, offres ouvertes, candidatures à traiter, onboardings en retard, CDD qui finissent), Direction (demandes à valider, nombre d'embauches). Plus le panneau des **notifications**, les pages d'**erreur** 403 / 404 / 500.
- [ ] **DS1-10** · P1 · jeudi · dépend de : écrans développés
  **Revue des écrans réels** des pistes FR0 et FR2 : lister les écarts avec les maquettes **par ordre d'importance**, directement dans ce fichier sous la tâche, pour que le Front les corrige jeudi après-midi.
- [ ] **DS1-11** · P2 · vendredi matin · dépend de : rien
  Schéma de l'**architecture technique** et du **modèle de données** pour la soutenance (version propre et lisible des sections 1 et 2).

### Piste DS2 — Parcours + Blocs 2 et 3 + soutenance

- [ ] **DS2-01** · P1 · lundi 10h30-12h · dépend de : rien
  **Personas** (Manager, RH, Direction, nouvel employé, candidat externe) en une fiche courte chacun, et **schéma du parcours global** : besoin → validation → offre → candidature → entretiens → proposition → embauche → contrat → onboarding, avec le rôle qui agit à chaque étape. Ce schéma servira aussi pour la soutenance.
- [ ] **DS2-02** · P1 · lundi après-midi · dépend de : rien
  **Basse fidélité** des écrans P1 du **bloc 2** : liste des offres (RH), détail / modification d'une offre, **page carrières publique** (liste, détail, formulaire « Postuler » avec CV, confirmation), liste des candidatures d'une offre, fiche candidature (CV, statut, entretiens, évaluations, boutons), formulaire d'entretien, formulaire d'évaluation.
- [ ] **DS2-03** · P1 · lundi fin d'après-midi · dépend de : rien
  **Basse fidélité** des écrans P1 du **bloc 3** : formulaire de proposition d'embauche, détail de la proposition avec les boutons « le candidat accepte / refuse », liste des employés, **dossier salarié en onglets** (Identité · Contrats · Affectations · Documents · Onboarding · Origine).
- [ ] **DS2-04** · P1 · mardi · dépend de : DS1-06, DS2-02
  **Haute fidélité** du bloc 2, en commençant par la **page carrières publique** (c'est la vitrine, elle doit être très soignée et parfaite sur mobile).
- [ ] **DS2-05** · P1 · mercredi matin · dépend de : DS1-06, DS2-03
  **Haute fidélité** du bloc 3.
- [ ] **DS2-06** · P2 · mercredi après-midi · dépend de : DS2-04
  Écrans P2 : fiche candidat (toutes ses candidatures), comparaison des évaluations des candidats d'une offre, récapitulatif après acceptation (matricule, contrat, affectation, onboarding créés).
- [ ] **DS2-07** · P1 · jeudi · dépend de : écrans développés
  **Revue des écrans réels** des pistes FR1 et FR2 (comme DS1-10) **et test utilisateur** : faire faire le scénario complet à 2 personnes extérieures, noter où elles hésitent.
- [ ] **DS2-08** · P1 · jeudi après-midi → vendredi · dépend de : DS2-01
  **Support de présentation** de la soutenance : problème, parcours, architecture, captures d'écran, démo, difficultés rencontrées, suite possible.

---

## 9. Équipe Back

> **Principe pour la semaine :** **lundi 16h, toutes les entités sont dans la branche principale.** Ensuite, chaque piste fait ses règles métier et ses contrôleurs sans attendre les autres.
> Toujours garder la branche principale qui démarre : une pull request ne doit jamais casser le lancement.

### Piste BK0 — Socle, sécurité, workflow, services transverses

- [ ] **BK0-01** · P1 · lundi 10h30-12h · dépend de : T-05
  **Socle** poussé avant midi pour débloquer les autres pistes :
  - [ ] dépendances dans `pom.xml` : accès aux données JPA, base H2 (et pilote PostgreSQL), Spring Security, intégration Thymeleaf + Spring Security ;
  - [ ] configuration de la base (profil `dev` en H2 fichier, profil `test` en mémoire) et génération automatique du schéma ;
  - [ ] organisation des packages par domaine (section 5.3), avec un dossier vide par domaine pour que chaque piste sache où travailler ;
  - [ ] classe de base commune aux entités (identifiant, dates de création / modification, auteur) ;
  - [ ] **sécurité ouverte temporairement** (tout est accessible) pour que personne ne soit bloqué par la connexion lundi.
- [ ] **BK0-02** · P1 · lundi 13h30-16h · dépend de : BK0-01
  Entités **Utilisateur**, **Rôle**, **Notification**, **Document**, **JournalAudit** + mécanisme de **données de démo** (un fichier par domaine, chargé au démarrage en `dev` uniquement) + les comptes de démo (un par rôle : admin, rh, direction, manager1, manager2, employe).
- [ ] **BK0-03** · P1 · lundi 16h-18h · dépend de : BK0-01
  **Moteur de statuts commun** : une façon unique, utilisée par BK1, BK2 et BK3, de déclarer pour un objet « depuis tel statut, vers tel statut, par tel rôle, à telle condition », de refuser toute transition non prévue avec un message clair, et de donner la liste des **actions possibles** pour l'utilisateur connecté (`actionsPossibles`, section 7.1). Expliquer en 5 lignes dans le README comment l'utiliser. **À livrer lundi soir**, c'est ce qui permet aux autres pistes de coder leurs workflows dès mardi.
- [ ] **BK0-04** · P1 · mardi matin · dépend de : BK0-02
  **Connexion / déconnexion** avec la page `/connexion`, mots de passe chiffrés, utilisateur connecté et rôles disponibles sur toutes les pages. Puis **fermer la sécurité** : protéger les URL par rôle (section 4), laisser publics `/connexion`, `/carrieres/**`, le CSS, le JS et les images. Prévenir tout le monde au moment où la sécurité est activée.
- [ ] **BK0-05** · P1 · mardi après-midi · dépend de : BK0-02
  **Stockage des fichiers** (GED) : enregistrement dans un dossier configuré (hors du dépôt Git), nom de fichier unique, formats autorisés (PDF, images, Word), taille max 5 Mo, téléchargement par `/documents/{id}` réservé aux personnes autorisées. Service simple que BK2 et BK3 appellent.
- [ ] **BK0-06** · P1 · mardi après-midi · dépend de : BK0-01
  **Gestion des erreurs** : pages 403 / 404 / 500 (templates de FR0), messages de validation en français, aucune trace technique affichée à l'utilisateur.
- [ ] **BK0-07** · P2 · mercredi matin · dépend de : BK0-02
  **Notifications** : service que les autres pistes appellent en une ligne (« notifier tel rôle / tel utilisateur de tel événement avec lien »), nombre de non lues disponible sur toutes les pages, page `/notifications`, marquer comme lu.
- [ ] **BK0-08** · P2 · mercredi après-midi · dépend de : BK0-03
  **Journal d'audit** automatique à chaque changement de statut (branché dans le moteur de statuts, donc rien à faire pour les autres pistes).
- [ ] **BK0-09** · P1 simple / P2 complet · jeudi matin · dépend de : entités des autres pistes
  **Tableau de bord** `/` : chiffres clés par rôle (demandes par statut, offres ouvertes, candidatures à traiter, onboardings en cours / en retard, CDD qui finissent dans 30 jours).
- [ ] **BK0-10** · P1 · jeudi après-midi · dépend de : tout
  **Relecture sécurité** : chaque URL testée avec chaque rôle, upload de fichiers, aucune donnée sensible dans les logs. Puis aide à la correction des bugs du scénario (T-10).
- [ ] **BK0-11** · P3 · si temps · Utilisateurs (`/admin/utilisateurs`), page d'audit, envoi d'e-mails simulé dans les logs, tâche quotidienne (clôture des offres expirées, propositions expirées, alertes fin de CDD).
- [ ] **BK0-12** · P1 · vendredi matin · Mettre à jour le `README.md` : lancement, comptes de démo, profils.

### Piste BK1 — Bloc 1 : Besoin en personnel

- [ ] **BK1-01** · P1 · lundi 13h30-16h · dépend de : BK0-01
  Entités **Département**, **Poste**, **DemandeRecrutement**, **Validation** avec tous les champs et liens de la section 2 et leurs statuts. Poussées **avant 16h**.
- [ ] **BK1-02** · P1 · lundi 16h-18h · dépend de : BK1-01
  **Données de démo** du domaine : 4 départements (Finance, Informatique, Commercial, RH), une quinzaine de postes (certains vacants, certains occupés), 4 demandes à des statuts différents.
- [ ] **BK1-03** · P1 · mardi matin · dépend de : BK1-01
  Pages **postes** (liste avec filtre département / statut) et contrôleurs **demandes** en lecture : liste (le manager voit les demandes de son département, RH et Direction voient tout) et détail avec l'historique des validations.
- [ ] **BK1-04** · P1 · mardi après-midi · dépend de : BK0-03
  **Créer et modifier une demande** : brouillon, validation du formulaire (effectif ≥ 1, date souhaitée dans le futur, budget positif, motif obligatoire). Création de poste : le poste est créé au statut « gelé ». Remplacement : le poste choisi doit exister.
- [ ] **BK1-05** · P1 · mercredi matin · dépend de : BK0-03
  **Workflow de la demande** (section 3) avec le moteur de statuts : soumettre, valider RH, valider Direction, refuser (commentaire obligatoire), demander un complément, annuler. Chaque décision crée une **Validation**. `actionsPossibles` fourni à la page de détail.
- [ ] **BK1-06** · P1 · mercredi après-midi · dépend de : entités de BK2 (lundi 16h)
  **Ouverture du recrutement** : passe la demande à « recrutement ouvert », passe le poste à « vacant », crée une **OffreEmploi en brouillon** pré-remplie (titre = intitulé du poste, description, type de contrat). Se mettre d'accord avec BK2 sur ce pré-remplissage au point de 13h30.
- [ ] **BK1-07** · P2 · mercredi après-midi · dépend de : BK0-07
  Notifications : demande soumise → RH ; validée RH → Direction ; décision finale → manager auteur.
- [ ] **BK1-08** · P1 · jeudi matin · dépend de : BK1-05
  **Tests** : chaque transition autorisée fonctionne, chaque transition interdite est refusée (ex. la Direction ne peut pas valider avant les RH, un manager ne peut pas valider), un manager ne voit pas les demandes d'un autre département.
- [ ] **BK1-09** · P2 · jeudi · Fiche poste avec historique des titulaires (via les affectations de BK3), CRUD postes et départements.

### Piste BK2 — Bloc 2 : Recrutement

- [ ] **BK2-01** · P1 · lundi 13h30-16h · dépend de : BK0-01
  Entités **OffreEmploi**, **Candidat**, **Candidature**, **Entretien**, **ÉvaluationCandidat** avec leurs statuts. Poussées **avant 16h**.
- [ ] **BK2-02** · P1 · lundi 16h-18h · dépend de : BK2-01, BK1-01
  **Données de démo** : 2 offres publiées (liées à des demandes ouvertes de BK1), une dizaine de candidats dont **un qui a postulé à deux offres**, des candidatures à différents statuts, quelques entretiens et évaluations.
- [ ] **BK2-03** · P1 · mardi matin · dépend de : BK2-01
  **Offres côté RH** : liste, détail, modification d'un brouillon, publier, clôturer, annuler (avec le moteur de statuts dès qu'il est livré).
- [ ] **BK2-04** · P1 · mardi après-midi · dépend de : BK0-05 (fichiers) — en attendant, enregistrer la candidature sans le fichier
  **Candidature publique** `/carrieres` : seules les offres publiées et non expirées sont visibles ; formulaire « Postuler » avec CV ; **si l'e-mail existe déjà, on réutilise le même Candidat** (une personne = un candidat, plusieurs candidatures) ; on refuse une deuxième candidature à la même offre ; redirection vers `/carrieres/merci`.
- [ ] **BK2-05** · P1 · mercredi matin · dépend de : BK0-03
  **Workflow de la candidature** : présélectionner, rejeter (motif obligatoire), passer en entretien (automatique au premier entretien planifié), retenir. Liste des candidatures d'une offre et fiche candidature avec `actionsPossibles`.
- [ ] **BK2-06** · P1 · mercredi après-midi · dépend de : BK2-05
  **Entretiens** : planifier (date dans le futur, au moins un évaluateur, pas deux entretiens au même créneau pour un évaluateur), marquer réalisé, annuler. **Évaluations** : seul un évaluateur assigné peut évaluer, une évaluation par évaluateur et par entretien, note moyenne visible sur la candidature.
- [ ] **BK2-07** · P1 · jeudi matin · dépend de : BK2-04, BK2-06
  **Tests** : réutilisation du candidat existant, double candidature refusée, offre expirée invisible, évaluateur non assigné refusé.
- [ ] **BK2-08** · P2 · jeudi · Fiche candidat (toutes ses candidatures), données de la comparaison des candidats d'une offre, notifications (candidature reçue → RH, entretien planifié → évaluateurs).
- [ ] **BK2-09** · P3 · si temps · Vue Kanban (candidatures regroupées par statut), agenda des entretiens, limite anti-spam sur le formulaire public.

### Piste BK3 — Blocs 3 et 4 : Embauche, contrat, onboarding

- [ ] **BK3-01** · P1 · lundi 13h30-16h · dépend de : BK0-01
  Entités **PropositionEmbauche**, **Employé**, **Contrat**, **Affectation**, **Onboarding**, **OnboardingTâche**, **ModèleTâcheOnboarding** avec leurs statuts. Le contrat peut pointer vers le contrat précédent. **Le type de contrat n'est pas un champ de l'employé.** Poussées **avant 16h**.
- [ ] **BK3-02** · P1 · lundi 16h-18h · dépend de : BK3-01
  **Données de démo** : les 6 modèles de tâches par défaut (contrat signé, badge, compte informatique, matériel, formation d'accueil, inscription paie) ; 3 employés dont **un avec l'historique CDD → renouvellement → CDI** ; un onboarding en cours à moitié fait.
- [ ] **BK3-03** · P1 · mardi matin · dépend de : BK3-01
  **Employés** : liste (recherche par nom ou matricule) et **dossier salarié** avec les données de chaque onglet (identité, contrats, affectations, documents, onboarding, candidature d'origine). Le « contrat actuel » est **calculé** à partir du contrat actif.
- [ ] **BK3-04** · P1 · mardi après-midi · dépend de : BK3-01
  **Contrats** :
  - [ ] nouveau contrat, signer, terminer (motif) ;
  - [ ] **renouvellement** et **passage en CDI** = un **nouveau** contrat pré-rempli et lié au précédent, l'ancien passe à « terminé » ;
  - [ ] un contrat signé n'est plus modifiable ;
  - [ ] un seul contrat actif par employé, pas de chevauchement de dates ;
  - [ ] CDD : date de fin obligatoire ; CDI : pas de date de fin.
- [ ] **BK3-05** · P1 · mercredi matin · dépend de : BK0-03, entités BK2
  **Proposition d'embauche** : créer pour une candidature **retenue** uniquement, envoyer, enregistrer la réponse (accepte / refuse avec motif). Génération du **matricule** (format décidé en T-05, unique, jamais réutilisé).
- [ ] **BK3-06** · P1 · mercredi après-midi · dépend de : BK3-04, BK3-05
  **Processus d'embauche** déclenché par l'acceptation, **tout ou rien** (si une étape échoue, rien n'est créé) :
  - [ ] création de l'**Employé** avec matricule et lien vers le Candidat d'origine ;
  - [ ] **Contrat** en brouillon (type, salaire, date de début de la proposition) ;
  - [ ] **Affectation** sur le poste de l'offre, poste → « occupé » ;
  - [ ] **Onboarding** avec les tâches générées depuis les modèles (échéance = date d'arrivée + délai du modèle) ;
  - [ ] candidature → « embauché », offre → « pourvue » si l'effectif est atteint ;
  - [ ] redirection vers le dossier salarié (ou le récapitulatif en P2).
- [ ] **BK3-07** · P1 · jeudi matin · dépend de : BK3-06
  **Onboarding** : changer le statut d'une tâche (seuls le responsable et les RH), calcul de la progression, **fin automatique** quand toutes les tâches obligatoires sont « fait » ou « non applicable ». La tâche « contrat signé » ne peut être cochée que si le contrat est signé.
- [ ] **BK3-08** · P1 · jeudi matin · dépend de : BK3-04, BK3-06
  **Tests** : le scénario d'acceptation crée bien tous les objets ; un échec au milieu ne laisse rien à moitié ; CDD → renouvellement → CDI garde les 3 contrats ; chevauchement refusé ; matricule unique ; un employé ne voit que son propre dossier.
- [ ] **BK3-09** · P2 · jeudi · Récapitulatif après acceptation, changement de poste (clôture de l'affectation en cours + nouvelle), justificatifs sur les tâches (via BK0-05), page « mes tâches d'onboarding », notifications (proposition acceptée → RH et manager, tâche assignée → responsable).
- [ ] **BK3-10** · P3 · si temps · Gestion des modèles de tâches par les RH, alerte de fin de CDD.

---

## 10. Équipe Front

> **Principe pour la semaine :** on ne reste **jamais** à attendre.
> - Pas encore de maquette ? On construit la page avec les composants Bootstrap et la description de l'écran (sections 7 et 8), on ajustera le style après.
> - Pas encore de contrôleur ? On écrit le template avec des données d'exemple visibles et on l'ouvre directement dans le navigateur. On le branche dès que la piste Back jumelle annonce que l'URL est prête.
> - Toujours respecter les noms du contrat d'interface (section 7) : c'est ce qui rend le branchement immédiat.

### Piste FR0 — Socle + Bloc 1 + transverse

- [ ] **FR0-01** · P1 · lundi 10h30-12h · dépend de : T-05
  **Gabarit commun** poussé **avant midi** pour débloquer FR1 et FR2 : intégration de Bootstrap, en-tête, menu latéral, zone de contenu, pied de page, version mobile avec menu repliable. Le menu est d'abord en dur, il sera filtré par rôle mardi.
- [ ] **FR0-02** · P1 · lundi après-midi · dépend de : DS1-01, DS1-02
  **Variables de la charte** dans le CSS commun (couleurs, polices, espacements) et **fragments réutilisables** : badge de statut (avec le code couleur de DS1-02), messages flash `succes` / `erreur`, champ de formulaire avec affichage de l'erreur, pagination, fil d'Ariane, état vide, fenêtre de confirmation avec commentaire. **Écrire en 2 lignes dans le README comment utiliser chaque fragment**, pour FR1 et FR2.
- [ ] **FR0-03** · P1 · lundi fin d'après-midi · dépend de : FR0-02
  Fragments **frise / historique**, **stepper** et **barre de progression** (utilisés par FR0, FR1 et FR2).
- [ ] **FR0-04** · P1 · mardi matin · dépend de : rien
  Page de **connexion** (erreur si identifiants faux, message après déconnexion) et pages d'**erreur** 403 / 404 / 500.
- [ ] **FR0-05** · P1 · mardi · dépend de : DS1-04 (basse fidélité)
  **Pages du bloc 1** : liste des demandes avec filtres et badges, formulaire de demande (la partie poste s'affiche selon création / remplacement, petit script), détail avec stepper et historique des validations, fenêtre de refus / complément avec commentaire obligatoire, liste des postes.
- [ ] **FR0-06** · P1 · mardi fin de journée · dépend de : BK0-04
  **Menu par rôle** (chaque rôle ne voit que ses entrées, section 4), nom et rôle de l'utilisateur dans l'en-tête, déconnexion.
- [ ] **FR0-07** · P1 · mercredi matin · dépend de : BK1-03, BK1-04
  **Brancher** les pages du bloc 1 sur les vrais contrôleurs. Les boutons d'action n'apparaissent **que** s'ils sont dans `actionsPossibles`.
- [ ] **FR0-08** · P1 · mercredi · dépend de : FR0-02
  **Formats français** partout (dates `jj/mm/aaaa`, montants `1 234,56 €`) via un fragment ou un utilitaire commun ; script commun : confirmation avant action irréversible, bouton désactivé après un clic (pas de double envoi).
- [ ] **FR0-09** · P1 simple / P2 complet · mercredi après-midi → jeudi matin · dépend de : DS1-09, BK0-09
  **Tableau de bord** par rôle (cartes de chiffres clés) et **cloche de notifications** avec compteur (P2).
- [ ] **FR0-10** · P1 · jeudi après-midi · dépend de : DS1-10
  Corriger les écarts listés par le Design, **test responsive** de toutes les pages du bloc 1 et du gabarit sur téléphone.
- [ ] **FR0-11** · P1 · jeudi · Vérifier que **tous les formulaires** de l'application (y compris FR1 et FR2) envoient le jeton de sécurité CSRF ; aucun message d'erreur dans la console du navigateur.
- [ ] **FR0-12** · P2 · si temps · Fiche poste avec historique des titulaires, formulaires poste et département, page notifications.

### Piste FR1 — Bloc 2 + pages publiques

- [ ] **FR1-01** · P1 · lundi 10h30-12h · dépend de : rien
  Pendant que FR0 fait le gabarit : **structure HTML des pages publiques** (liste des offres, détail + formulaire « Postuler », confirmation) avec des données d'exemple. Ces pages ont leur **propre gabarit public** (logo, sans menu interne) dans le dossier `public/`.
- [ ] **FR1-02** · P1 · lundi après-midi · dépend de : FR0-01
  **Pages RH des offres** : liste avec statut et nombre de candidatures, détail, formulaire de modification.
- [ ] **FR1-03** · P1 · mardi matin · dépend de : FR0-02, DS2-02
  **Candidatures** : liste des candidatures d'une offre (tableau, badges, filtres par statut), fiche candidature (infos, lien vers le CV, entretiens, évaluations, note moyenne, boutons d'action, fenêtre de rejet avec motif).
- [ ] **FR1-04** · P1 · mardi après-midi · dépend de : FR0-02
  **Formulaire d'entretien** (type, date, heure, lieu ou lien visio, évaluateurs) et **formulaire d'évaluation** (critères notés, avis, points forts / faibles, commentaire).
- [ ] **FR1-05** · P1 · mardi fin de journée · dépend de : DS2-04
  Appliquer la **haute fidélité à la page carrières publique** : elle doit être impeccable sur téléphone. Vérification du format et de la taille du CV dans le navigateur pour un retour immédiat.
- [ ] **FR1-06** · P1 · mercredi · dépend de : BK2-03, BK2-04, BK2-05
  **Brancher** les pages offres, carrières et candidatures. Les boutons suivent `actionsPossibles`.
- [ ] **FR1-07** · P1 · jeudi matin · dépend de : BK2-06
  **Brancher** entretiens et évaluations, afficher les erreurs de validation renvoyées par le Back.
- [ ] **FR1-08** · P1 · jeudi après-midi · dépend de : DS2-07
  Corrections du Design, test responsive des pages du bloc 2.
- [ ] **FR1-09** · P2 · si temps · Fiche candidat (toutes ses candidatures), page de comparaison des candidats d'une offre.
- [ ] **FR1-10** · P3 · si temps · Vue Kanban des candidatures, agenda des entretiens.

### Piste FR2 — Blocs 3 et 4

- [ ] **FR2-01** · P1 · lundi 10h30-12h · dépend de : rien
  Pendant que FR0 fait le gabarit : **structure HTML du dossier salarié** en onglets (Identité · Contrats · Affectations · Documents · Onboarding · Origine) avec des données d'exemple ; l'onglet affiché dépend du paramètre `onglet` de l'URL.
- [ ] **FR2-02** · P1 · lundi après-midi · dépend de : FR0-01
  **Liste des employés** (recherche par nom ou matricule, filtres département / statut).
- [ ] **FR2-03** · P1 · mardi matin · dépend de : FR0-03, DS1-05
  **Onglet Contrats** : frise de tous les contrats (CDD → renouvellement → CDI), contrat actif mis en avant, badge de statut. **Formulaire contrat** pour les 3 cas (nouveau, renouvellement, CDI) : pré-rempli depuis le contrat précédent, date de fin masquée pour un CDI et obligatoire pour un CDD.
- [ ] **FR2-04** · P1 · mardi après-midi · dépend de : FR0-03, DS1-05
  **Checklist d'onboarding** : tâches groupées par catégorie, responsable, échéance, statut modifiable, barre de progression, tâches en retard en rouge. Plus l'onglet **Affectations** (historique des postes).
- [ ] **FR2-05** · P1 · mardi fin de journée · dépend de : DS2-03
  **Proposition d'embauche** : formulaire (salaire, date de début, type de contrat, date limite de réponse) et page de détail avec les boutons « le candidat accepte » / « le candidat refuse » (motif).
- [ ] **FR2-06** · P1 · mercredi · dépend de : BK3-03, BK3-04
  **Brancher** la liste des employés, le dossier salarié et les contrats.
- [ ] **FR2-07** · P1 · jeudi matin · dépend de : BK3-05, BK3-06, BK3-07
  **Brancher** la proposition, l'acceptation (arrivée sur le dossier du nouvel employé) et la checklist d'onboarding.
- [ ] **FR2-08** · P1 · jeudi après-midi · dépend de : DS1-10, DS2-07
  Corrections du Design, test responsive (les frises et tableaux larges défilent ou passent en cartes sur téléphone).
- [ ] **FR2-09** · P2 · si temps · Récapitulatif après acceptation, onglet Documents avec upload, formulaire de changement de poste, page « mes tâches d'onboarding ».
- [ ] **FR2-10** · P3 · si temps · Page de gestion des modèles de tâches d'onboarding.

---

## 11. Planning jour par jour

> Chaque colonne avance **en même temps**. Les cases en gras sont des **rendez-vous** : ce qui doit être poussé à cette heure-là pour débloquer les autres.

| | Design (DS1 · DS2) | Back (BK0 · BK1 · BK2 · BK3) | Front (FR0 · FR1 · FR2) |
|---|---|---|---|
| **Lundi 19** | 9h-10h30 lancement commun (section 6) · DS1 : **mini-charte + couleurs des statuts avant midi**, gabarit, basse fidélité blocs 1 et 4 · DS2 : personas + parcours, basse fidélité blocs 2 et 3 · **18h : basse fidélité de tous les écrans P1 partagée** | 9h-10h30 lancement · BK0 : **socle avant midi**, utilisateurs + données de démo, **moteur de statuts le soir** · BK1/BK2/BK3 : **entités avant 16h**, puis données de démo | 9h-10h30 lancement · FR0 : **gabarit avant midi**, charte + fragments · FR1 : pages publiques en statique, puis pages offres · FR2 : dossier salarié en statique, puis liste des employés |
| **Mardi 20** | DS1 : composants, haute fidélité bloc 1 · DS2 : haute fidélité bloc 2 (page carrières d'abord) | BK0 : **connexion + sécurité activée (prévenir tout le monde)**, fichiers, erreurs · BK1 : postes, demandes (lecture, création) · BK2 : offres, candidature publique · BK3 : employés, dossier, contrats | FR0 : connexion, erreurs, pages bloc 1, menu par rôle · FR1 : candidatures, entretiens, évaluations, page carrières finale · FR2 : contrats, onboarding, proposition |
| **Mercredi 21** | DS1 : haute fidélité bloc 4, tableau de bord · DS2 : haute fidélité bloc 3, écrans P2 | BK0 : notifications, audit · BK1 : workflow de la demande, ouverture du recrutement → offre · BK2 : workflow candidature, entretiens, évaluations · BK3 : proposition, matricule, **processus d'embauche** | **Branchement** de toutes les pages déjà prêtes · FR0 : formats français, tableau de bord |
| **Jeudi 22** | Matin : revue des écrans réels, liste des écarts · test utilisateur · début du support de soutenance | Matin : tests de chaque piste, onboarding (BK3), tableau de bord (BK0) · **16h : gel des fonctionnalités** · 16h-18h : scénario complet et corrections | Matin : derniers branchements · après-midi : corrections Design, responsive, CSRF · **16h : gel** · 16h-18h : scénario complet |
| **Vendredi 23** | Support de soutenance, schémas, captures d'écran | Corrections des bugs du scénario, README, données de démo finales | Corrections visuelles, vérification sur Chrome / Firefox / Edge |
| | **Répétition de la démo, puis rendu** | | |

### Le scénario complet à tester jeudi (et à montrer vendredi)

1. **manager1** se connecte, crée une demande de remplacement pour un poste de son département, la soumet.
2. **rh** voit la demande « à valider », la valide. **direction** la valide à son tour.
3. **rh** ouvre le recrutement : une offre en brouillon apparaît, il la complète et la publie.
4. Sans être connecté, on va sur `/carrieres`, on postule avec un CV. On postule une deuxième fois avec le même e-mail à une autre offre : c'est bien le **même candidat**.
5. **rh** présélectionne la candidature, planifie un entretien avec **manager1** comme évaluateur.
6. **manager1** saisit son évaluation. **rh** retient la candidature.
7. **rh** crée et envoie la proposition d'embauche, puis enregistre que le candidat **accepte**.
8. Le dossier salarié apparaît : **matricule**, contrat CDD en brouillon, affectation sur le poste (qui passe à « occupé »), onboarding avec ses 6 tâches.
9. **rh** signe le contrat et coche les tâches : l'onboarding passe à « terminé ».
10. Plus tard : **rh** renouvelle le CDD puis passe l'employé en **CDI** : l'onglet Contrats montre les **3 contrats**, rien n'a été écrasé.

---

## 12. Priorités : ce qu'on coupe si on manque de temps

| Priorité | Signification | Contenu |
|---|---|---|
| **P1** | Indispensable : sans ça, pas de démo | Connexion et rôles · bloc 1 : demande + validation RH + Direction + ouverture · bloc 2 : offre publiée, page carrières, candidature avec CV, présélection, entretien, évaluation, retenue · bloc 3 : proposition acceptée → employé + matricule + contrat + affectation + onboarding · bloc 4 : historique des contrats (renouvellement, CDI), checklist d'onboarding · tableau de bord simple · les tests du scénario |
| **P2** | Important, à faire si les P1 de sa piste sont finies | Notifications · journal d'audit · fiches poste / département / candidat · comparaison des candidats · récapitulatif d'embauche · changement de poste · documents sur les tâches · « mes tâches d'onboarding » · tableau de bord complet |
| **P3** | Bonus | Vue Kanban · agenda des entretiens · administration des utilisateurs (on utilise les comptes de démo) · gestion des modèles de tâches · e-mails · tâche planifiée quotidienne · API REST pour le mobile · export CSV · PostgreSQL au lieu de H2 |

**Règle :** mercredi soir, si une piste a du retard sur ses P1, elle abandonne ses P2 et les autres pistes l'aident (une piste Back aide une piste Back, un Front aide un Front) **sans toucher aux fichiers de l'autre** : on prend une tâche entière, pas la moitié d'un fichier.

Ce qu'on **ne sacrifie jamais**, même en P1 réduit : les 3 règles de conception (le poste existe sans l'employé, candidat ≠ employé, l'historique des contrats n'est jamais écrasé). C'est le cœur du sujet.

---

## 13. Définition de « terminé »

Une tâche est terminée seulement si **tout** est vrai :

- [ ] Elle fait ce qui est décrit dans ce fichier (et dans la maquette pour un écran).
- [ ] Le projet démarre et `./mvnw test` passe.
- [ ] Les règles des sections 3 et 4 sont respectées (statuts et droits).
- [ ] Les messages affichés sont compréhensibles et en français.
- [ ] Une autre personne a relu la pull request.
- [ ] Le travail est fusionné dans la branche principale (au plus tard à l'intégration de 17h30).
- [ ] La case est cochée dans ce fichier.

---

## 14. Glossaire

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