---
id: f2
objectives:
  - Écrire un plan avec ses chapitres et ses séances.
  - Choisir le type, la durée et l'objectif d'une séance, et le statut du plan.
terms: [syllabus, chapter, session, identifier, quick-review, mock-exam]
---

:::::warmup
::ref{id=f1-a2}
:::::

## Chapitres et séances {#chapters}

**Comment Boussole sait-il quelles séances présenter, et dans quel ordre ?** Par le
:term[plan]{id=syllabus}, `cursus/syllabus.yaml`. L'ordre du fichier est l'ordre du parcours.

Le plan est une liste de :term[chapitres]{id=chapter}, et chaque chapitre une liste de séances :

:::graph[Le plan des bases de Git]{layout=layers}
p "syllabus.yaml"
p -> c
c "premiers-pas"
c -> s1
c -> s2
c -> r9
:::

| Champ du chapitre | Obligatoire | Sens |
|---|---|---|
| `id` | oui | identifiant, unique dans le parcours |
| `title` | oui | titre affiché |
| `dir` | oui | dossier du chapitre, sous `cursus/` |
| `sources` | non | les documents de `sources/` dont il est tiré |

| Champ de la séance | Obligatoire | Sens |
|---|---|---|
| `id` | oui | identifiant, unique dans le chapitre |
| `title` | oui | titre affiché ; le fichier de la séance n'a pas de titre `#` à lui |
| `duration` | oui | durée en minutes |
| `goal` | oui | une phrase : ce que l'apprenant sait faire à la fin |
| `file` | oui | le fichier de la séance, dans le dossier du chapitre |
| `kind` | non | le type de séance (notion suivante) |

:::example[Le plan des bases de Git]
```yaml
status: proposed
chapters:
  - id: premiers-pas
    title: Premiers pas avec Git
    dir: 01-premiers-pas
    sessions:
      - id: s1
        title: Créer un dépôt
        duration: 15
        goal: Créer un dépôt Git et dire ce qu'il contient.
        file: s1-creer-un-depot.md
      - id: s2
        title: Le premier commit
        duration: 15
        goal: Enregistrer un changement avec git add et git commit.
        file: s2-premier-commit.md
```

- La séance `s1` s'écrit dans `cursus/01-premiers-pas/s1-creer-un-depot.md`.
- Son identifiant complet est `git-bases.premiers-pas.s1` : le parcours, le chapitre, la séance.
- Son titre, « Créer un dépôt », vient d'ici : pas de titre `#` dans son fichier.
:::

:::warning
La progression de l'apprenant est attachée aux :term[identifiants]{id=identifier}, jamais aux
noms de fichiers. Renommer un fichier ne perd rien si `file` suit ; changer un `id` perd la
progression enregistrée dessus.
:::

::::exercise{#f2-a1 type=text}
Dans le parcours `git-bases`, le chapitre `premiers-pas` contient la séance `s2`, écrite dans
`s2-premier-commit.md`. Quel est l'identifiant complet de cette séance ?

:::answer
git-bases.premiers-pas.s2
:::

:::hint
Un identifiant complet a trois morceaux, séparés par des points.
:::

:::hint
Parcours, puis chapitre, puis séance : le nom du fichier n'en fait pas partie.
:::

:::solution
**`git-bases.premiers-pas.s2`** : l'identifiant du parcours, celui du chapitre, puis celui de la
séance. Le nom du fichier n'y entre pas.
:::
::::

::::mcq{#f2-a2}
Où Boussole cherche-t-il le fichier de la séance `s1` ?

- [x] `cursus/01-premiers-pas/s1-creer-un-depot.md`
- [ ] `cursus/premiers-pas/s1.md`
  > Le dossier vient de `dir`, pas de l'`id` du chapitre ; le fichier vient de `file`.
- [ ] `01-premiers-pas/s1-creer-un-depot.md`
  > Les dossiers des chapitres sont rangés dans `cursus/`.

:::hint
Regarde les champs `dir` et `file` du plan.
:::

:::solution
`cursus/`, puis le `dir` du chapitre, puis le `file` de la séance :
**`cursus/01-premiers-pas/s1-creer-un-depot.md`**.
:::
::::

## Type, durée et statut {#status}

**Comment dire qu'une séance est une révision, et combien de temps elle prend ?** Avec `kind` et
`duration`.

| `kind` | La séance | `duration` |
|---|---|---|
| `lesson` (par défaut) | une leçon : des notions et des questions | de 15 à 120, une estimation du travail, pas une limite |
| `review` | une :term[révision express]{id=quick-review}, chronométrée | de 5 à 120, le temps accordé |
| `exam` | un :term[examen blanc]{id=mock-exam}, chronométré, corrigé à la fin | de 5 à 240, le temps accordé |

**Et le `status` en tête du plan ?** Il dit où en est le plan :

- `proposed` : une proposition, à relire. C'est le statut d'un plan neuf.
- `approved` : le plan est validé ; les séances s'écrivent. Le validateur signale alors chaque
  séance dont le fichier manque encore (`F011`, un avertissement).

::::example[Ajouter une révision express]
À la fin du chapitre, une séance de plus :

```yaml
      - id: r9
        title: Révision express — premiers pas
        kind: review
        duration: 5
        goal: Retrouver sans notes les commandes de base, en temps limité.
        file: r9-revision.md
```

Le plan relu, la première ligne devient :

```yaml
status: approved
```

:::tip
L'objectif (`goal`) commence par un verbe et se vérifie : « Enregistrer un changement », plutôt
que « Comprendre Git ».
:::
::::

::::exercise{#f2-a3 type=fill}
Complète.

Pour un examen blanc, le champ `kind` vaut [[1]] et sa durée peut aller jusqu'à [[2]] minutes.
Sans champ `kind`, la séance est [[3]]. Un plan relu et validé a le statut [[4]].

:::answer
1: choice *exam | review | test
2: number 240
3: choice *une leçon | une révision express | un examen blanc
4: choice *approved | proposed | ready
:::

:::hint
Relis le tableau des trois types de séance.
:::

:::hint
`lesson` est la valeur par défaut ; un examen blanc a la plus longue durée possible.
:::

:::solution
`kind: exam`, jusqu'à **240** minutes. Sans `kind`, c'est une **leçon** (`lesson`). Un plan
validé est **`approved`**.
:::
::::

:::::checkpoint
::::mcq{#f2-c1}
Une leçon a `duration: 15`. L'apprenant y passe 25 minutes. Que se passe-t-il ?

- [x] Rien : pour une leçon, la durée est une estimation
- [ ] La séance s'arrête au bout de 15 minutes
  > Seules les révisions express et les examens blancs sont chronométrés.
- [ ] Le validateur signale une erreur
  > Le validateur lit les fichiers ; il ne voit pas le temps passé par l'apprenant.

:::hint
Relis la colonne `duration` pour `lesson`.
:::

:::solution
**Rien.** La durée d'une leçon estime le travail, à un rythme normal ; une leçon se termine par
son bilan, pas par une minuterie.
:::
::::

::::exercise{#f2-c2 type=numeric unit=minutes}
Quelle est la durée la plus courte permise pour une leçon ?

:::answer
15
:::

:::hint
Les révisions express peuvent descendre à 5 minutes ; les leçons, non.
:::

:::solution
**15** minutes : une leçon dure de 15 à 120 minutes.
:::
::::
:::::
