# Conception d'un logiciel de gestion RH

> **Du recrutement à la rupture du contrat : concevoir un SIRH autour du cycle de vie du salarié**

```
Besoin → Recrutement → Embauche → Vie du salarié → Évolution → Rupture
```

---

## Bloc 1 : Expression du besoin en personnel

Le **manager** exprime un besoin :
- création d'un nouveau poste ;
- remplacement d'un poste vacant.

### Informations à saisir
- Poste
- Département
- Service
- Section
- Nombre
- Motif *(à préciser)*

### Workflow de validation

```
Manager → RH → Finance → Direction → Ouverture du recrutement
```

### Objets métier
- `Departement`
- `Poste`
- `DemandeRecrutement`
- `Validation`

### Point de conception
> Un **Poste** existe indépendamment de l'employé.

---

## Bloc 2 : Recrutement

```
Demande validée → Offre d'emploi
```

- Publier l'offre et recevoir plusieurs candidatures.
- Présélectionner, organiser les entretiens et les tests (sélection de dossier, entretien, test, …).
- Conserver les évaluations et la décision finale.

### Objets métier
- `OffreEmploi`
- `Candidat`
- `Candidature`
- `Entretien`
- `EvaluationCandidat`

### Point de conception
> On doit prendre en compte la **traçabilité** (historique).

---

## Bloc 3 : Sélection et embauche

- Réception d'une proposition d'embauche par le candidat.
- Après acceptation : création du **dossier salarié** et du **matricule**.
- Création du **contrat**, du **poste** et de l'**affectation**.

### Informations du contrat
- Montant du salaire
- Date et horaires de travail
- Lieu de travail
- *TODO : rechercher sur Internet les mentions habituelles d'un contrat de travail.*

### Génération du contrat
- Après acceptation → **génération automatique du PDF** du contrat de travail.

### Points de conception
> - **Candidat ≠ Employé**
> - Une personne peut avoir **plusieurs candidatures** avant de devenir salarié.

---

## Bloc 4 : Contrat et onboarding

### Contrats
- Un salarié peut avoir **plusieurs contrats successifs** : CDD, renouvellement, CDI, …
- Ne pas écraser l'historique en stockant seulement `employe.type_contrat`.

#### États du contrat

```
Créé → Signé → Validé
```

> Une fois le contrat signé, le **matricule** est généré.

### Onboarding
Étapes d'intégration :
- Contrat signé
- Badge
- Compte
- Matériel
- Formation
- Inscription à la paie

➡️ Utiliser une **checklist** et un **workflow d'intégration**.

### Objets métier
- `Contrat`
- `Affectation`
- `Onboarding`
- `OnboardingTache`
- `Document`
