---
id: f8
objectives:
  - Écrire le lexique d'un parcours et y renvoyer avec `:term`.
  - Tenir le suivi des documents d'un parcours.
terms: [lexicon, document-tracking, identifier, validator]
written_by:
  - model: claude-opus-5-5
    tool: Claude Code
    date: 2026-10-21
---

:::::warmup
::ref{id=f7-a4}
:::::

## Le lexique {#lexicon}

**Comment donner la définition d'un mot sans couper l'explication ?** Tu l'écris une fois dans le
:term[lexique]{id=lexicon}, `cursus/lexique.yaml`, et chaque séance y renvoie avec `:term` :
l'apprenant lit la définition au survol.

| Champ | Obligatoire | Sens |
|---|---|---|
| `id` | oui | identifiant, unique dans le parcours |
| `term` | oui | le terme tel qu'il s'affiche |
| `aliases` | non | d'autres façons de l'écrire, pour la recherche |
| `definition` | oui | deux ou trois phrases de Markdown, pour qui n'a jamais vu le terme |
| `see` | non | les identifiants des termes voisins |

Le lien se fait en deux endroits de la séance :

1. dans l'en-tête, `terms` liste les identifiants employés ;
2. dans le texte, `:term[texte]{id=…}` affiche la définition au survol.

Un identifiant absent du lexique, dans `terms`, `:term` ou `see`, est une erreur (`F030`). Un
`:term` absent de `terms` donne un avertissement (`P005`).

:::example[Le lexique des bases de Git]
```yaml
- id: depot
  term: Dépôt
  aliases: [dépôts, repository]
  definition: >
    Le dossier `.git` où Git range tout l'historique d'un projet. `git init` le crée.
  see: [commit]

- id: commit
  term: Commit
  aliases: [commits]
  definition: >
    Une version enregistrée du projet, avec son auteur, sa date et son message.
  see: [depot, index]

- id: index
  term: Index
  aliases: [zone de préparation, staging area]
  definition: >
    Ce qui partira dans le prochain commit. `git add` y place un fichier.
  see: [commit]
```

Dans la séance `s2` : `terms: [commit, index]` dans l'en-tête, et dans le texte
`git add place le fichier dans l':term[index]{id=index}`. Comme « index » est exactement le terme,
`:term[index]` suffit aussi.
:::

::::mcq{#f8-a1}
Dans `s2`, tu écris `:term[l'index]{id=staging}`. Que dit le validateur ?

- [x] Une erreur : `staging` n'est pas l'identifiant d'un terme
- [ ] Rien : `staging area` est un alias de l'index
  > `id=` attend l'identifiant du terme, `index`, pas un alias ni un morceau d'alias.
- [ ] Un avertissement : le terme manque dans `terms`
  > C'est `P005`, pour un terme qui existe ; ici, l'identifiant lui-même n'existe pas.

:::hint
Que contient le champ `id` de ce terme, dans le lexique ?
:::

:::solution
**Une erreur** (`F030`) : `id=` renvoie à un identifiant du lexique. Ici, `:term[l'index]{id=index}`.
:::
::::

::::exercise{#f8-a2 type=fill}
Complète.

Dans `lexique.yaml`, le champ [[1]] donne le mot affiché, [[2]] d'autres façons de l'écrire et
[[3]] les identifiants des termes voisins. Dans une séance, la liste [[4]] de l'en-tête reprend
les termes employés.

:::answer
1: choice *term | id | definition
2: choice *aliases | see | term
3: choice *see | aliases | terms
4: choice *terms | objectives | sources
:::

:::hint
Relis le tableau des champs du lexique.
:::

:::hint
`see` veut dire « voir aussi ».
:::

:::solution
`term` est le mot affiché, `aliases` ses autres écritures, `see` ses voisins ; l'en-tête liste les
termes de la séance dans `terms`.
:::
::::

## Le suivi des documents {#documents}

**Comment savoir, dans six mois, quel document a servi à quoi ?** Par le
:term[suivi des documents]{id=document-tracking}, `cursus/sources.yaml` : une entrée par document
du dossier `sources/`.

| Champ | Sens |
|---|---|
| `file` | le document, depuis `sources/`, ou un motif comme `Annales/**` (obligatoire) |
| `sha256` | l'empreinte du document quand le parcours l'a utilisé |
| `status` | `used` (par défaut), `ignored` (laissé de côté) ou `pending` (à prendre plus tard) |
| `chapters` | les identifiants des chapitres qui s'en servent |
| `note` | ce qu'il est, ou pourquoi il attend |
| `url`, `title`, `licence`, `retrieved` | pour un document pris sur le web : son adresse, son titre, sa licence et le jour où tu l'as pris |

Sans aucun document, pas besoin de ce fichier : c'est le cas de ce parcours.

L'empreinte, tu ne la calcules pas à la main : `boussole sources ~/parcours/git-bases` liste les
documents nouveaux, modifiés, en attente ou disparus, avec les entrées à ajouter et leur empreinte.

:::example[Le suivi des documents des bases de Git]
```yaml
- file: progit.pdf
  sha256: 2c7d0f…            # donnée par boussole sources
  status: used
  chapters: [premiers-pas]
  url: https://git-scm.com/book/fr/v2
  title: Pro Git
  licence: CC BY-NC-SA 3.0
  retrieved: 2026-10-21
- file: "brouillons/**"
  status: ignored
  note: Mes notes de préparation.
```

Le :term[validateur]{id=validator} compare ce fichier au dossier `sources/` et prévient :

| Code | Le document… |
|---|---|
| `F033` | est nouveau, ou `pending` |
| `F034` | a changé depuis que le parcours l'a utilisé |
| `F035` | est listé mais a disparu |
| `F036` | est en double, à deux endroits |
| `F039` | a une `url` mais pas de `licence` |
:::

::::exercise{#f8-a3 type=match}
Associe chaque avertissement à sa cause.

:::answer
`F033` => un document nouveau, pas encore suivi
`F034` => un document modifié depuis que le parcours l'a utilisé
`F035` => un document suivi mais disparu du dossier
`F039` => une adresse web sans licence
:::

:::hint
Les trois premiers suivent la vie d'un document : il arrive, il change, il part.
:::

:::solution
`F033` nouveau, `F034` modifié, `F035` disparu, `F039` une `url` sans `licence`.
:::
::::

::::mcq{#f8-a4}
Tu ranges dans `sources/` un sujet d'examen que tu traiteras plus tard. Quel statut lui
donnes-tu ?

- [x] `pending`
- [ ] `ignored`
  > `ignored` laisse un document de côté pour de bon ; celui-ci servira plus tard.
- [ ] `used`
  > Il n'est pas encore pris dans le parcours.

:::hint
Lequel des trois veut dire « en attente » ?
:::

:::solution
**`pending`** : vu, à prendre plus tard. Ajoute une `note` qui dit pourquoi il attend.
:::
::::

:::::checkpoint
::::mcq{#f8-c1}
Une entrée du suivi des documents a une `url`. Quel champ faut-il ajouter pour éviter
l'avertissement `F039` ?

- [x] `licence`
- [ ] `sha256`
  > L'empreinte est conseillée pour tout document, mais `F039` parle d'autre chose.
- [ ] `retrieved`
  > La date est facultative.

:::hint
Avant de reprendre un document du web, que faut-il savoir de ses droits ?
:::

:::solution
**`licence`** : la licence sous laquelle la page est partagée, comme `CC BY-SA 4.0`.
:::
::::
:::::
