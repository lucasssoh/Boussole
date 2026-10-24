---
id: f1
objectives:
  - Dire ce que contient chaque fichier d'un parcours.
  - Créer le plus petit parcours valide et le vérifier avec le validateur.
terms: [track, syllabus, session, lexicon, document-tracking, identifier, validator]
written_by:
  - model: claude-opus-5-5
    tool: Claude Code
    date: 2026-10-21
---

**Envie d'écrire un parcours toi-même, sans agent ?** Un éditeur de texte suffit : un
:term[parcours]{id=track} n'est qu'un dossier de fichiers Markdown et YAML.

Tout au long de ce parcours, tu en construis un, pièce par pièce : **« Les bases de Git »**, deux
séances et une révision express. Chaque séance ajoute une pièce.

:::tip
Garde un terminal ouvert à côté : tu peux taper chaque exemple et le vérifier aussitôt.
:::

## Un dossier, quelques fichiers {#folder}

**Par où commence un parcours ?** Par un dossier. Voici celui du fil rouge :

```text
git-bases/
├── subject.yaml            ce qu'est le parcours
├── learner.yaml            ta façon d'apprendre (facultatif)
├── sources/                tes documents, jamais modifiés par Boussole
└── cursus/
    ├── syllabus.yaml       le plan
    ├── lexique.yaml        les termes et leur définition
    ├── sources.yaml        le suivi des documents
    ├── assets/             les images des séances
    └── 01-premiers-pas/    un dossier par chapitre
        └── s1-creer-un-depot.md
```

| Fichier | Ce qu'il contient | Obligatoire |
|---|---|---|
| `subject.yaml` | le nom, l'identifiant et la langue du parcours | oui |
| `cursus/syllabus.yaml` | le :term[plan]{id=syllabus} : chapitres et séances, dans l'ordre | oui |
| `cursus/<chapitre>/<séance>.md` | une :term[séance]{id=session} : texte et questions | une par séance du plan |
| `cursus/lexique.yaml` | le :term[lexique]{id=lexicon} | non |
| `cursus/sources.yaml` | le :term[suivi des documents]{id=document-tracking} | dès qu'il y a des documents |
| `learner.yaml` | ta façon d'apprendre | non |

Boussole ignore tous les autres fichiers : tes brouillons peuvent rester à côté.

:::example[Le subject.yaml des bases de Git]
```yaml
format: 1
id: git-bases
name: Les bases de Git
lang: fr
level: discovery
domain: information-technology
description: Créer un dépôt, enregistrer ses changements et lire l'historique.
```

- `format` : la version du format, toujours `1` pour l'instant.
- `id` : l':term[identifiant]{id=identifier} du parcours, en minuscules, chiffres et `-`.
- `name` : le nom affiché.
- `lang` : la langue du contenu (`fr`, `en`, `de`…).
- `level` : ce qu'il faut pour suivre le parcours, `discovery`, `secondary`, `bachelor`, `master`
  ou `research`. Plus il est exigeant, plus ses questions rapportent.
- `domain` : son domaine, dans la liste du format (`information-technology`, `mathematics`,
  `philosophy-and-ethics`…).
- `description` : une ou deux phrases, facultative.
- `authors` et `contributors` : qui fait et signe le parcours (une personne, une université avec
  `organisation: true`), et qui y a contribué, avec son `role`. Facultatifs, affichés sur la page
  du parcours.
- `sources`, facultatif : le dossier des documents, `sources` par défaut.
:::

::::exercise{#f1-a1 type=match}
Associe chaque fichier à ce qu'il contient.

:::answer
`subject.yaml` => le nom, l'identifiant et la langue du parcours
`cursus/syllabus.yaml` => les chapitres et les séances, dans l'ordre
`cursus/lexique.yaml` => les termes et leur définition
`cursus/sources.yaml` => ce que le parcours a fait de chaque document
:::

:::hint
Un seul de ces fichiers est à la racine du dossier : c'est lui qui dit ce qu'est le parcours.
:::

:::hint
`syllabus` veut dire « plan de cours » ; `sources.yaml` parle des documents du dossier `sources/`.
:::

:::solution
`subject.yaml` décrit le parcours ; dans `cursus/`, `syllabus.yaml` est le plan, `lexique.yaml`
définit les termes et `sources.yaml` suit les documents.
:::
::::

::::mcq{#f1-a2}
Quel identifiant de parcours est valide ?

- [x] `git-bases`
- [ ] `Git-Bases`
  > Les majuscules sont refusées : un identifiant s'écrit en minuscules.
- [ ] `git_bases`
  > Le tiret bas est refusé : seul le tiret `-` relie les mots.
- [ ] `bases de git`
  > Les espaces sont refusés : relie les mots par des tirets.

:::hint
Un identifiant n'a que des minuscules, des chiffres et des tirets.
:::

:::solution
**`git-bases`** : des minuscules et un tiret. Le validateur refuse les majuscules, le tiret bas et
les espaces (code `F004`).
:::
::::

## Le plus petit parcours valide {#minimal}

**De quoi Boussole a-t-il besoin, au minimum ?** De deux fichiers : `subject.yaml` et
`cursus/syllabus.yaml`. Pas besoin de les taper, une commande les crée :

```
boussole init ~/parcours/git-bases --id git-bases --name "Les bases de Git" --lang fr
```

| `boussole init` crée | Pour quoi |
|---|---|
| `subject.yaml` | avec l'identifiant, le nom et la langue donnés |
| `cursus/syllabus.yaml` | un plan vide : `status: proposed` et `chapters: []` |
| `AGENTS.md`, `CLAUDE.md`, `.boussole/` | les fiches méthode des agents, et une copie du format |

Il ne remplace jamais un fichier qui existe déjà.

:::note
`.boussole/FORMAT.md` est la référence complète du format. Garde-le sous la main : ce parcours
en présente l'essentiel, le format en donne chaque détail.
:::

:::example[Vérifier le parcours vide]
Dans le dossier du parcours, lance le :term[validateur]{id=validator} :

```
cd ~/parcours/git-bases
boussole validate .
```

Il répond :

```
.: no problem, 0 of 0 sessions written.
```

`no problem` : aucune erreur, aucun avertissement. Le parcours est valide, même sans séance.
:::

::::mcq{#f1-a3 multiple=true}
Quels fichiers faut-il, au minimum, pour que le validateur accepte un parcours ? (plusieurs
réponses)

- [x] `subject.yaml`
- [x] `cursus/syllabus.yaml`
- [ ] `cursus/lexique.yaml`
  > Le lexique est facultatif : un parcours peut ne pas en avoir encore.
- [ ] `learner.yaml`
  > Ta façon d'apprendre est facultative ; sans elle, des valeurs par défaut s'appliquent.

:::hint
Ce sont les deux fichiers que crée `boussole init`, en dehors des fiches méthode.
:::

:::solution
**`subject.yaml` et `cursus/syllabus.yaml`.** Le lexique, le suivi des documents et `learner.yaml`
viennent ensuite, si besoin.
:::
::::

## Un parcours en plusieurs langues {#languages}

**« Et si je veux le même parcours en anglais ? »** Tu ajoutes une **version** : un dossier à part,
avec le **même `id`** et son propre `lang`.

```
git-bases/
├── fr/   subject.yaml : id: git-bases, lang: fr
└── en/   subject.yaml : id: git-bases, lang: en
```

- Boussole montre les deux dossiers comme **un seul parcours** : on choisit la langue sur sa page,
  ou on la change en pleine séance, au même endroit.
- La progression est la même dans toutes les langues.
- **Seul le texte se traduit.** Les identifiants (séances, notions `{#…}`, questions) restent les
  mêmes, et chaque question demande la même chose : mêmes choix, mêmes trous.
- Les documents ne se copient pas : la version pointe vers ceux de l'original,
  `sources: "../fr/sources"`.

Le validateur compare les versions entre elles : `F040` quand elles diffèrent, `F041` pour une
séance pas encore traduite.

::::mcq{#f1-a4}
Tu traduis la séance s1 de « Les bases de Git » en anglais. Que gardes-tu tel quel ?

- [ ] Le titre de la notion `## Créer un dépôt`
  > Le titre se traduit ; c'est son identifiant `{#init}` qui reste.
- [x] Les identifiants : `{#init}`, `s1-a1`, et le `id` du parcours
- [ ] Les réponses acceptées d'un exercice `text`
  > Elles sont dans la langue de la version : on écrit les réponses anglaises.

:::hint
Qu'est-ce qui permet à Boussole de retrouver la même place dans l'autre langue ?
:::

:::solution
**Les identifiants.** Le texte se traduit, titres et réponses compris ; les identifiants et ce que
chaque question demande restent les mêmes, pour que la progression et la place dans la séance
suivent d'une langue à l'autre.
:::
::::

:::::checkpoint
::::exercise{#f1-c1 type=fill}
Complète.

Dans `subject.yaml`, le champ [[1]] donne la langue du contenu et le champ [[2]] vaut toujours 1.
Le plan que crée `boussole init` a le statut [[3]].

:::answer
1: choice *lang | name | id
2: choice *format | id | description
3: choice *proposed | approved | empty
:::

:::hint
Relis l'exemple du `subject.yaml`, puis le tableau de `boussole init`.
:::

:::solution
`lang` donne la langue, `format` la version du format (`1`). Un plan neuf est `proposed` : une
proposition, à relire avant d'écrire les séances.
:::
::::

::::mcq{#f1-c2}
Le validateur répond `no problem`. Qu'est-ce que cela veut dire ?

- [x] Le parcours est bien écrit : aucune erreur, aucun avertissement
- [ ] Les réponses de tes exercices sont justes
  > Le validateur vérifie la forme et le contrat pédagogique, pas l'exactitude de tes réponses.
- [ ] Le parcours a au moins une séance
  > Un parcours vide donne aussi `no problem` : `0 of 0 sessions written`.

:::hint
Relis la réponse du validateur sur le parcours vide.
:::

:::solution
**Le parcours est bien écrit.** Que tes réponses soient justes, c'est à toi de le vérifier : le
validateur ne connaît pas Git.
:::
::::
:::::
