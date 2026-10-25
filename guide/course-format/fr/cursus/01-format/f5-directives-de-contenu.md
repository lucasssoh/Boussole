---
id: f5
objectives:
  - Choisir entre exemple, rappel, encadré et terme pour expliquer une notion.
  - Ouvrir un document, une vidéo ou une page web à côté du texte.
  - Dessiner un schéma avec `graph` ou `mermaid`, et écrire des maths.
terms: [worked-example, directive, lexicon, notion]
prerequisites: [boussole-format.format.f4]
written_by:
  - model: claude-opus-5-5
    tool: Claude Code
    date: 2026-10-21
---

:::::warmup
::ref{id=f4-a2}
:::::

## Exemples, rappels et encadrés {#callouts}

**Comment faire ressortir ce qui compte dans une explication ?** Avec quatre
:term[directives]{id=directive} :

| Directive | Rôle | Combien |
|---|---|---|
| `:::example[titre]` | un :term[exemple résolu]{id=worked-example} : un cas traité pas à pas, solution visible | un par notion |
| `:::recall` | un rappel d'un terme ou d'un prérequis, juste avant d'en avoir besoin | à volonté |
| `:::note`, `:::tip`, `:::warning` | une remarque, une astuce, une erreur à éviter | à volonté |
| `:::beyond` | ce qui ne vient pas des documents, et d'où ça vient | à volonté |
| `:term[texte]{id=…}` | un mot dont la définition, tirée du :term[lexique]{id=lexicon}, s'affiche au survol | à volonté |

Pour `:term`, `{id=…}` est facultatif quand le texte est exactement le terme ou l'un de ses
alias. Chaque terme cité se déclare aussi dans `terms`, dans l'en-tête (`P005` sinon).

:::example[La notion « Créer un dépôt » de s1]
````markdown
## Créer un dépôt {#init}

:::recall
Un :term[dépôt]{id=depot} est le dossier où Git range tout l'historique d'un projet.
:::

Dans le dossier du projet, `git init` crée un dossier caché, `.git` : c'est le dépôt.

:::example[Un dépôt pour ton site]
```
cd ~/site
git init
```
Git répond `Initialized empty Git repository in /home/lea/site/.git/`.
:::

:::warning
Ne lance pas `git init` dans ton dossier personnel : Git suivrait tous tes fichiers.
:::
````

Le rappel vient avant l'explication, l'exemple après, l'avertissement à la fin. Puis vient la
question de la notion.
:::

::::exercise{#f5-a1 type=match}
Associe chaque directive à son rôle.

:::answer
`:::example` => un cas traité pas à pas, solution visible
`:::recall` => un terme ou un prérequis, juste avant d'en avoir besoin
`:::warning` => une erreur à éviter
`:term` => une définition au survol
:::

:::hint
`recall` veut dire « rappeler » ; `warning`, « avertissement ».
:::

:::hint
Une seule de ces directives s'écrit dans une phrase.
:::

:::solution
`example` montre un cas résolu, `recall` rappelle ce qu'il faut savoir, `warning` met en garde, et
`term` affiche une définition du lexique au survol.
:::
::::

::::mcq{#f5-a2}
Ta notion a déjà un `:::example`. Tu veux montrer un deuxième cas. Que fais-tu ?

- [x] Tu en fais une question, juste après l'exemple
- [ ] Tu ajoutes un second `:::example`
  > Une notion n'a qu'un exemple (`P002`) : l'apprenant comprend en essayant le cas suivant.
- [ ] Tu le mets dans un `:::recall`
  > Un rappel remet en mémoire ce qui est déjà connu ; il ne montre pas un cas nouveau.

:::hint
Que fait l'apprenant juste après l'exemple d'une notion ?
:::

:::solution
**Une question.** Un seul exemple par notion ; le cas suivant, l'apprenant le résout lui-même, et
la solution de la question lui montre la méthode.
:::
::::

::::mcq{#f5-a6}
Ton parcours est construit sur les transparents de ton prof. Tu veux ajouter une analogie à toi,
que les transparents ne donnent pas. Où va-t-elle ?

- [x] Dans un `:::beyond`, qui dit qu'elle ne vient pas des transparents
- [ ] Dans l'explication, avec le reste
  > L'apprenant la prendrait pour ce que le prof a dit : ce qui vient d'ailleurs est mis à part.
- [ ] Nulle part : un parcours ne peut que répéter ses documents
  > Une analogie peut aider ; elle est simplement montrée pour ce qu'elle est.

:::hint
L'apprenant doit toujours savoir ce que le prof a dit et ce qui a été ajouté.
:::

:::solution
Chaque notion renvoie à la page sur laquelle elle s'appuie (`:pdf`) ; ce qui vient d'ailleurs, une
analogie, un fait de culture générale, une source web, va dans un `:::beyond` qui dit d'où ça vient.
:::
::::

## Documents, vidéos et liens {#media}

**Comment renvoyer vers une page du manuel, une vidéo ou une page web ?** Trois directives les
ouvrent à côté du texte. L'étiquette est la légende, ou les mots de la phrase.

| Directive | Forme | Attributs |
|---|---|---|
| `pdf` | ligne ou en ligne | `src=` un fichier de `sources/` (obligatoire), `page=N` |
| `video` | ligne | `youtube=` l'identifiant de 11 caractères **ou** `src=` un fichier vidéo de `sources/` ; `start` et `end` en secondes, `m:ss` ou `h:mm:ss` |
| `link` | ligne ou en ligne | `url=` une adresse complète, `https://…` (obligatoire) |

Une page web s'affiche dans Boussole quand son site le permet, et sinon dans une fenêtre à part.

:::example[Trois renvois dans s1]
```markdown
Le manuel le montre :pdf[à la page 32]{src=progit.pdf page=32}.

::video[Git en 100 secondes]{youtube=hwP7WQkmECE start=0:15}

::link[La documentation de git init]{url=https://git-scm.com/docs/git-init}
```

- `:pdf` est en ligne : il tient dans la phrase.
- `::video` est une ligne seule ; il commence à 15 secondes.
- `::link` est une ligne seule : une référence à lire en entier.
:::

::::mcq{#f5-a3}
Quelle ligne est valide ?

- [x] `::video[Git en 100 secondes]{youtube=hwP7WQkmECE start=0:15}`
- [ ] `:::video[Git en 100 secondes]{youtube=hwP7WQkmECE}`
  > `video` est une ligne seule : deux deux-points, pas trois.
- [ ] `::video[Git]{youtube=hwP7WQkmECE src=git.mp4}`
  > Une vidéo vient de YouTube ou d'un fichier, jamais des deux.
- [ ] `::link[La doc]{url=git-scm.com/docs}`
  > L'adresse doit être complète : `https://git-scm.com/docs`.

:::hint
Vérifie le nombre de deux-points, puis chaque attribut.
:::

:::solution
**`::video[…]{youtube=hwP7WQkmECE start=0:15}`** : une ligne, un identifiant YouTube, un début à
15 secondes.
:::
::::

::::exercise{#f5-a4 type=fill}
Complète.

Une vidéo de YouTube se donne avec l'attribut [[1]], un fichier du dossier `sources/` avec
[[2]]. Pour commencer à 2 minutes 10, on écrit `start=` suivi de [[3]].

:::answer
1: choice *youtube | url | src
2: choice *src | file | url
3: choice *2:10 | 210 | 2h10
:::

:::hint
Relis la ligne `video` du tableau.
:::

:::hint
`start` accepte des secondes, `m:ss` ou `h:mm:ss`.
:::

:::solution
`youtube=…` ou `src=…`, et `start=2:10` (ou `start=130`, en secondes). `210` voudrait dire 210
secondes, soit 3 minutes 30.
:::
::::

## Schémas et maths {#diagrams}

**Comment dessiner un schéma sans fichier image ?** Deux moyens :

- `:::graph` : des nœuds et des flèches, une ligne chacun ; Boussole le dessine et le vérifie ;
- un bloc de code `mermaid` : un organigramme ou une séquence.

| Ligne de `graph` | Sens |
|---|---|
| `a -> b` | une flèche de `a` vers `b` |
| `a -- b` | un lien sans sens |
| `a -> b : 3` | une flèche avec une étiquette |
| `a "Mon nœud"` | le nœud `a` affiché avec ce texte |
| `… !` | à la fin d'une ligne : cet élément ressort |

Le placement se choisit avec `{layout=circle}` (par défaut), `row` ou `layers`. Au-delà de 12
nœuds, le validateur prévient (`P011`).

Les maths s'écrivent `$…$` dans une phrase et `$$…$$` sur leurs propres lignes : Git peut
nommer $2^{160}$ commits différents. Les images, en Markdown : `![légende](../assets/branches.png)`.

::::example[L'historique de s2, en graphe]
Tu écris :

```markdown
:::graph[Trois commits, du plus ancien au plus récent]{layout=row}
c1 "Ajoute README"
c2 "Ajoute index.html"
c3 "Corrige le titre" !
c1 -> c2
c2 -> c3
:::
```

Boussole dessine :

:::graph[Trois commits, du plus ancien au plus récent]{layout=row}
c1 "Ajoute README"
c2 "Ajoute index.html"
c3 "Corrige le titre" !
c1 -> c2
c2 -> c3
:::

Le `!` fait ressortir le dernier commit.
::::

::::mcq{#f5-a5}
Quelle ligne de `graph` le validateur accepte-t-il ?

- [x] `a -> b : 3`
- [ ] `a -> a`
  > Une flèche d'un nœud vers lui-même n'est pas dessinée : le validateur la refuse.
- [ ] `a => b`
  > Une flèche s'écrit `->`, un lien sans sens `--`.

:::hint
Relis le tableau des lignes de `graph`.
:::

:::solution
**`a -> b : 3`** : une flèche de `a` vers `b`, avec l'étiquette `3`.
:::
::::

:::::checkpoint
::::exercise{#f5-c1 type=match}
Associe chaque besoin à sa directive.

:::answer
Montrer un cas traité pas à pas => example
Ouvrir une page du manuel => pdf
Citer une page web => link
Dessiner un petit réseau => graph
:::

:::hint
Les noms de directives sont des mots anglais : « exemple », « lien », « graphe »…
:::

:::solution
`example` pour un cas résolu, `pdf` pour un document de `sources/`, `link` pour une page web,
`graph` pour un schéma de quelques nœuds.
:::
::::
:::::
