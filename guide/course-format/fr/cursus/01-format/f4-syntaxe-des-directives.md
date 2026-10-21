---
id: f4
objectives:
  - Reconnaître les trois formes de directive et écrire leurs attributs.
  - Imbriquer des blocs avec le bon nombre de deux-points.
terms: [directive, notion, nudge]
---

:::::warmup
::ref{id=f3-a3}
:::::

## Trois formes {#formes}

**Comment écrire autre chose que du texte : un encadré, une vidéo, une question ?** Avec une
:term[directive]{id=directive}. Son nombre de deux-points dit sa forme :

| Forme | Syntaxe | Exemple |
|---|---|---|
| Bloc, sur plusieurs lignes | `:::nom[étiquette]{attributs}` … `:::` | `:::tip` … `:::` |
| Ligne, seule sur sa ligne | `::nom[étiquette]{attributs}` | `::video[Git en 100 secondes]{youtube=hwP7WQkmECE}` |
| En ligne, dans une phrase | `:nom[texte]{attributs}` | `un :term[dépôt]{id=depot} vide` |

- Un bloc et une ligne commencent **à la première colonne**.
- L'étiquette `[…]` et les attributs `{…}` sont facultatifs, selon la directive.
- Les noms sont des mots anglais en minuscules, quelle que soit la langue du parcours :
  `:::example`, jamais `:::exemple`.
- Dans un bloc de code, rien n'est une directive : c'est ainsi que ce parcours te les montre.

**Et les attributs ?** Entre accolades, séparés par des espaces :

| Écriture | Sens |
|---|---|
| `{type=text}` | l'attribut `type` vaut `text` |
| `{unit="lignes de code"}` | des guillemets quand la valeur a des espaces |
| `{#s1-a1}` | raccourci pour `id=s1-a1` |
| `{multiple=true}` | l'attribut `multiple` vaut `true` ; `{multiple}` seul dit la même chose |

Chaque attribut ne s'écrit qu'une fois.

::::example[Une astuce dans la séance s1]
Tu écris :

```markdown
:::tip[Avant de commencer]
Vérifie que Git est installé : `git --version` affiche sa version.
:::
```

Boussole affiche :

:::tip[Avant de commencer]
Vérifie que Git est installé : `git --version` affiche sa version.
:::

Trois deux-points : c'est un bloc. L'étiquette `[Avant de commencer]` devient son titre.
::::

::::exercise{#f4-a1 type=match}
Associe chaque début de directive à sa forme.

:::answer
`:::note` => un bloc, fermé plus bas par une ligne de deux-points
`::video` => une ligne seule, sans fermeture
`:term` => quelques mots, au milieu d'une phrase
:::

:::hint
Compte les deux-points : trois, deux, un.
:::

:::solution
Trois deux-points : un **bloc**, qu'il faut fermer. Deux : une **ligne**. Un : une directive **en
ligne**, dans une phrase.
:::
::::

::::mcq{#f4-a2}
Quelle ouverture d'exercice est valide ?

- [x] `::::exercise{#s1-a2 type=text}`
- [ ] `::::exercise{id=s1-a2 #s1-a2 type=text}`
  > `#s1-a2` veut déjà dire `id=s1-a2` : l'attribut est donné deux fois.
- [ ] `::::exercice{#s1-a2 type=text}`
  > Les noms de directives sont en anglais : `exercise`.
- [ ] `::::exercise{#s1-a2 type = text}`
  > Pas d'espace autour du `=` : les espaces séparent les attributs.

:::hint
Vérifie le nom, puis chaque attribut : écrit une seule fois, sans espace autour du `=`.
:::

:::solution
**`::::exercise{#s1-a2 type=text}`** : le nom anglais, l'identifiant en raccourci `#`, puis
`type=text`.
:::
::::

## Imbriquer des blocs {#imbrication}

**Comment glisser un coup de pouce dans une question, et la question dans un point de
contrôle ?** En mettant **plus de deux-points à l'extérieur** : un de plus par niveau. Chaque
bloc se ferme avec le même nombre de deux-points que son ouverture.

| Niveau | Bloc | Deux-points |
|---|---|---|
| extérieur | `warmup`, `checkpoint`, `practice` | 5 |
| milieu | `mcq`, `exercise` | 4 |
| intérieur | `hint`, `solution`, `answer` | 3 |

Ces nombres suffisent pour presque tout un parcours. Une question posée dans une notion, hors de
tout bloc, garde ses quatre deux-points.

:::example[Le point de contrôle de s1]
```markdown
:::::checkpoint
::::mcq{#s1-c1}
Quelle commande crée un dépôt ?

- [x] `git init`
- [ ] `git commit`
  > Elle enregistre un changement dans un dépôt qui existe déjà.

:::hint
La commande prépare un dossier pour Git : elle l'« initialise ».
:::

:::solution
**`git init`** crée le dossier caché `.git`, qui contiendra tout l'historique.
:::
::::
:::::
```

- `:::hint` et `:::solution` (3) s'ouvrent et se ferment dans le QCM.
- `::::` (4) ferme le QCM.
- `:::::` (5) ferme le point de contrôle.
:::

::::exercise{#f4-a3 type=fill}
Complète.

Dans un `:::::warmup`, une question s'ouvre avec [[1]] deux-points et son coup de pouce avec
[[2]]. Le bloc `:::::warmup` se ferme par une ligne de [[3]] deux-points.

:::answer
1: choice *4 | 3 | 5
2: choice *3 | 4 | 2
3: choice *5 | 3 | 4
:::

:::hint
Un de plus à chaque niveau vers l'extérieur.
:::

:::hint
Le bloc le plus à l'intérieur a trois deux-points ; l'échauffement en a cinq.
:::

:::solution
La question : **4** ; son :term[coup de pouce]{id=nudge} : **3** ; l'échauffement se ferme comme il
s'ouvre : **5**.
:::
::::

:::::checkpoint
::::mcq{#f4-c1}
Le validateur refuse cet exercice. Quelle ligne manque ?

```markdown
::::exercise{#s1-a2 type=text}
Quelle commande crée un dépôt ?

:::answer
git init
:::

:::hint
Elle « initialise » un dossier pour Git.
:::

:::solution
**`git init`**
::::
```

- [x] Une ligne `:::` après la solution
- [ ] Une ligne `::::` à la fin
  > L'exercice a déjà sa ligne `::::` : c'est un bloc plus intérieur qui reste ouvert.
- [ ] Une ligne `:::` après la question
  > L'énoncé n'est pas un bloc : il n'a rien à fermer.

:::hint
Compte les blocs à trois deux-points ouverts, puis ceux qui sont fermés.
:::

:::solution
**`:::` après la solution** : `:::answer` et `:::hint` sont fermés, `:::solution` ne l'est pas. Le validateur le
signale avec le code `F026`.
:::
::::
:::::
