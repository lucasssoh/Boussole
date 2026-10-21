---
id: f7
objectives:
  - Écrire un texte à trous, avec des trous à taper, des nombres et des listes.
  - Écrire un exercice à remettre dans l'ordre, à associer ou à réponse libre.
terms: [nudge, notion]
---

:::::warmup
::ref{id=f6-a2}
:::::

## Le texte à trous {#fill}

**Comment faire compléter une phrase, une commande ou un raisonnement ?** Avec `type=fill`. Dans
l'énoncé, `[[1]]`, `[[2]]`… marquent les trous ; dans `:::answer`, une ligne par trou :

| Trou | Ligne de réponse | L'apprenant |
|---|---|---|
| à taper | `1: add` ou, avec d'autres réponses acceptées, `1: add \| a` | tape un mot |
| un nombre | `2: number 40`, ou avec une marge, `2: number 3/4 ±0.01` | tape une valeur |
| une liste | `3: choice *-m \| -a \| --init` | choisit ; l'option juste porte une `*` |

- Les réponses acceptées d'un trou à taper sont séparées par une barre `|` entourée d'espaces.
- Les trous de l'énoncé et ceux de la réponse correspondent un à un (`F023` sinon).
- Un trou peut se placer dans du code, en ligne ou dans un bloc, pour compléter une commande ou
  une ligne de programme : `` `ip [[1]] show` ``. Hors de l'énoncé d'un exercice à trous,
  `[[1]]` s'affiche tel quel.
- Une liste ne sait pas afficher de maths : écris ses options en texte simple, avec `≤` ou `x²`
  plutôt que `$…$`.

:::tip
Pour une démonstration ou un raisonnement, préfère des trous à **liste** : l'apprenant choisit
chaque étape parmi des erreurs plausibles, au lieu de taper une formule.
:::

:::example[Compléter les commandes, dans s2]
```markdown
::::exercise{#s2-a1 type=fill}
Complète.

Pour placer `index.html` dans l'index, on tape `git` suivi de [[1]]. Pour enregistrer le
commit avec son message, on tape `git commit` suivi de [[2]]. L'identifiant du commit
compte [[3]] caractères.

:::answer
1: add
2: choice *-m | -a | --init
3: number 40
:::

:::hint
La première commande « ajoute » un fichier à l'index.
:::

:::hint
L'option du message est la première lettre de *message*.
:::

:::solution
`git add index.html`, puis `git commit -m "…"`. L'identifiant compte **40** caractères
hexadécimaux.
:::
::::
```

Trois trous dans l'énoncé, trois lignes dans la réponse : un à taper, une liste, un nombre.
:::

::::mcq{#f7-a1}
Quelle ligne de réponse propose une liste dont l'option juste est `-m` ?

- [x] `2: choice *-m | -a | --init`
- [ ] `2: choice -m | -a | --init`
  > Aucune option ne porte d'étoile : Boussole ne sait pas laquelle est juste.
- [ ] `2: -m | -a | --init`
  > Sans `choice`, c'est un trou à taper, où les trois réponses seraient acceptées.
- [ ] `2: number -m`
  > `number` attend une valeur numérique, pas une option.

:::hint
Une liste commence par `choice`, et l'option juste porte une marque.
:::

:::solution
**`2: choice *-m | -a | --init`** : le mot `choice`, puis les options, l'étoile devant la juste.
:::
::::

::::exercise{#f7-a2 type=fill}
Complète.

La ligne `3: number 40` décrit un trou [[1]]. La ligne `2: choice *-m | -a` décrit un trou
[[2]], dont l'option juste porte [[3]]. Une ligne sans mot-clé, comme `1: add | a`, décrit un
trou [[4]].

:::answer
1: choice *à nombre | à liste | à taper
2: choice *à liste | à nombre | à taper
3: choice *une étoile | un dièse | un tiret
4: choice *à taper | à liste | à nombre
:::

:::hint
Deux mots-clés existent : `number` et `choice`.
:::

:::hint
Sans mot-clé, l'apprenant tape sa réponse.
:::

:::solution
`number` : un trou **à nombre** ; `choice` : un trou **à liste**, l'option juste marquée d'**une
étoile** ; sans mot-clé : un trou **à taper**, où `add` et `a` sont acceptés.
:::
::::

## Ordre, paires et réponse libre {#order-match}

**Et pour remettre des étapes dans l'ordre, ou relier deux listes ?** Trois autres types :

| `type` | Ce que contient `:::answer` | L'apprenant |
|---|---|---|
| `order` | une liste (`1.` ou `-`) dans le bon ordre, deux éléments au moins | remet dans l'ordre les éléments mélangés |
| `match` | une ligne par paire, `gauche => droite`, deux paires au moins | associe à chaque gauche une droite, choisie dans une liste |
| `free` | pas de `:::answer` | écrit, compare à la solution, puis s'évalue : réussi, en partie, pas encore |

Comme pour les trous à liste, les côtés droits d'un `match` s'affichent dans une liste : pas de
`$…$`. `free` sert à ce que Boussole ne peut pas vérifier : expliquer, concevoir, démontrer.

:::example[Remettre dans l'ordre, dans s2]
```markdown
::::exercise{#s2-a2 type=order}
Remets dans l'ordre les commandes du premier commit.

:::answer
1. `git init`
2. `git add index.html`
3. `git commit -m "Ajoute la page d'accueil"`
4. `git log`
:::

:::hint
On n'ajoute un fichier qu'à un dépôt qui existe.
:::

:::solution
On crée le dépôt, on place le fichier dans l'index, on enregistre le commit, puis on le voit
dans l'historique.
:::
::::
```

Pour un `match`, seule la réponse change :

```markdown
:::answer
`git init` => crée le dépôt
`git add` => prépare un fichier pour le prochain commit
`git commit` => enregistre ce qui est préparé
:::
```
:::

::::exercise{#f7-a3 type=match}
Associe chaque type d'exercice à ce qu'il fait faire.

:::answer
`order` => remettre des étapes dans l'ordre
`match` => relier deux listes
`fill` => compléter une phrase ou un raisonnement
`free` => expliquer, sans vérification automatique
:::

:::hint
Traduis chaque mot : ordre, associer, remplir, libre.
:::

:::solution
`order` remet dans l'ordre, `match` associe, `fill` fait compléter, `free` laisse écrire
librement, puis l'apprenant se compare à la solution.
:::
::::

::::mcq{#f7-a4}
Quel type d'exercice n'a pas de partie `:::answer` ?

- [x] `free`
- [ ] `order`
  > Sa réponse est la liste dans le bon ordre.
- [ ] `match`
  > Sa réponse est la liste des paires `gauche => droite`.

:::hint
Lequel Boussole ne peut-il pas corriger tout seul ?
:::

:::solution
**`free`** : l'apprenant lit la solution et s'évalue lui-même. Tous les autres types ont
exactement un `:::answer`.
:::
::::

:::::checkpoint
::::mcq{#f7-c1}
Pourquoi écrire `x²` plutôt que `$x^2$` dans une option de liste ?

- [x] Une liste déroulante n'affiche pas les maths
- [ ] Pour que l'apprenant tape plus vite
  > Dans une liste, l'apprenant ne tape rien : il choisit.
- [ ] Les maths sont interdites partout dans un exercice
  > Les énoncés, les choix d'un QCM, les éléments d'un `order` et les côtés gauches d'un `match`
  > peuvent en contenir.

:::hint
Où s'affichent les options d'un trou à liste ?
:::

:::solution
**Une liste déroulante n'affiche pas les maths.** Ses options, comme les côtés droits d'un
`match`, s'écrivent en texte simple, avec des symboles comme `≤`, `x₁` ou `u²`.
:::
::::
:::::
