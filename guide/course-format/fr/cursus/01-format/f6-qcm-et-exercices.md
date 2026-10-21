---
id: f6
objectives:
  - Écrire un QCM avec ses retours, ses coups de pouce et sa solution.
  - Écrire un exercice à réponse numérique ou textuelle.
terms: [nudge, identifier, notion]
---

:::::warmup
::ref{id=f5-a3}
:::::

## Le QCM {#mcq}

**Comment savoir si l'apprenant a compris ?** En lui posant une question, juste après l'exemple de
la :term[notion]{id=notion}. La plus simple est le QCM, un bloc `::::mcq{#id}` :

| Partie | Combien | Rôle |
|---|---|---|
| l'énoncé | un, en premier | la question |
| les choix `- [x]` et `- [ ]` | deux ou plus, à la fin de l'énoncé | coché : juste ; vide : faux |
| un retour `  > …` | sous chaque mauvais choix | dit pourquoi ce choix est faux |
| `:::hint` | un ou plus | les :term[coups de pouce]{id=nudge}, dévoilés un à un avant de répondre |
| `:::solution` | exactement un | pourquoi la bonne réponse est la bonne |

- Un seul choix coché, sauf avec `multiple=true` : plusieurs choix justes, et l'interface le dit.
- Pas de `:::answer` : les cases cochées sont la réponse.
- L':term[identifiant]{id=identifier} est unique dans tout le parcours et ne resservira jamais.
  L'usage : `<séance>-<lettre><numéro>`, avec `a` pour une question de notion, `w` pour
  l'échauffement, `c` pour le point de contrôle, `p` pour l'entraînement.

:::example[Le premier QCM de s1]
```markdown
::::mcq{#s1-a1}
Que contient le dossier `.git` créé par `git init` ?

- [x] Tout l'historique du projet
- [ ] Une copie de sauvegarde de tes fichiers
  > Git ne copie pas ton dossier : il enregistre des versions successives.
- [ ] Les réglages de ton éditeur
  > Ton éditeur range ses réglages ailleurs ; `.git` n'appartient qu'à Git.

:::hint
Le dossier `.git` est le dépôt lui-même.
:::

:::solution
**Tout l'historique** : chaque version enregistrée, les branches et les réglages du dépôt
vivent dans `.git`.
:::
::::
```

Les retours sont indentés de deux espaces, sous leur choix. Le choix juste n'en a pas besoin,
mais peut en avoir un.
:::

:::tip
Un bon mauvais choix est une vraie erreur d'apprenant. Son retour dit où regarder, sans donner la
bonne réponse.
:::

Ce QCM, lui, passe le validateur avec des avertissements :

```markdown
::::mcq{#s1-a3}
Quelle commande crée un dépôt ?

- [x] `git init`
- [ ] `git start`

:::solution
`git init`.
:::
::::
```

::::mcq{#f6-a1 multiple=true}
Que manque-t-il au QCM `s1-a3` ci-dessus ? (plusieurs réponses)

- [x] Un coup de pouce, `:::hint`
- [x] Un retour sous le choix `git start`
- [ ] Une partie `:::answer`
  > Un QCM n'en a pas : les cases cochées sont la réponse.
- [ ] Un second choix coché
  > Un seul choix juste suffit ; il en faudrait plusieurs avec `multiple=true` seulement.

:::hint
Relis le tableau : quelles parties sont attendues, et combien ?
:::

:::hint
Les deux manques donnent les avertissements `P004` et `P010`.
:::

:::solution
**Un coup de pouce** (`P004`) et **un retour sous le mauvais choix** (`P010`), par exemple :
« `git start` n'existe pas : la commande prépare, elle “initialise”. »
:::
::::

::::exercise{#f6-a2 type=text}
Un QCM a deux bonnes réponses. Quel attribut ajoutes-tu à son ouverture ?

:::answer
multiple=true
multiple
{multiple=true}
:::

:::hint
L'attribut dit qu'il y a « plusieurs » bonnes réponses, en anglais.
:::

:::solution
**`multiple=true`** (ou `multiple` seul). Sans lui, deux choix cochés sont une erreur (`F022`).
:::
::::

## L'exercice {#exercise}

**Et quand la réponse n'est pas un choix ?** Un bloc `::::exercise{#id type=…}` : l'énoncé, puis
exactement un `:::answer`, un ou plusieurs `:::hint` et un `:::solution`.

| `type` | Ce que contient `:::answer` | Ce qui est accepté |
|---|---|---|
| `numeric` | une valeur par ligne ; un point décimal, ou une fraction `3/4` | la valeur, à `tolerance` près (`0` par défaut) ; la virgule aussi |
| `text` | une réponse acceptée par ligne ; `re:` en début de ligne pour une expression régulière | sans tenir compte des espaces en trop ni de la casse ; `strict=true` garde la casse |

`unit=` affiche une unité à côté du champ ; l'apprenant ne la tape pas. Une réponse à taper reste
courte et précise : une valeur, un mot-clé, une commande.

:::example[Un exercice texte dans s1]
```markdown
::::exercise{#s1-a2 type=text}
Quelle commande crée un dépôt dans le dossier courant ?

:::answer
git init
:::

:::hint
Le verbe anglais pour « préparer au départ » : *initialize*.
:::

:::hint
La commande a deux mots : `git`, puis quatre lettres.
:::

:::solution
**`git init`** fait du dossier courant un dépôt, en y créant le dossier `.git`.
:::
::::
```

Et un exercice numérique, dans `s2` : « Combien de caractères a l'identifiant d'un commit ? »,
avec `type=numeric unit=caractères` et la réponse `40`.
:::

::::mcq{#f6-a3}
Un apprenant tape `Git Init` pour l'exercice `s1-a2`. Sa réponse est-elle acceptée ?

- [x] Oui : un exercice `text` ignore la casse
- [ ] Non : les majuscules comptent
  > Seulement avec `strict=true`, que `s1-a2` n'a pas.
- [ ] Non : il faut taper les accents graves autour de la commande
  > Les accents graves servent à la mise en forme du texte ; l'apprenant tape la commande seule.

:::hint
Relis la colonne « Ce qui est accepté » pour `text`.
:::

:::solution
**Oui.** Sans `strict=true`, la casse et les espaces en trop sont ignorés : `Git Init` vaut
`git init`.
:::
::::

::::exercise{#f6-a4 type=fill}
Complète.

Pour accepter 3,14 à un centième près, on ajoute l'attribut [[1]] ; pour afficher « Mo » à côté
du champ, l'attribut [[2]] ; pour que les majuscules comptent, l'attribut [[3]].

:::answer
1: choice *tolerance=0.01 | unit=0.01 | strict=0.01
2: choice *unit=Mo | tolerance=Mo | label=Mo
3: choice *strict=true | multiple=true | case=true
:::

:::hint
Les trois attributs sont dans le texte sous le tableau des types, et dans le tableau lui-même.
:::

:::solution
`tolerance=0.01` (un écart absolu), `unit=Mo` (affichée, pas tapée), `strict=true` (pour un
exercice `text`).
:::
::::

:::::checkpoint
::::mcq{#f6-c1}
Combien de parties `:::solution` a un exercice ou un QCM ?

- [x] Exactement une
- [ ] Une par coup de pouce
  > Les coups de pouce aident avant la réponse ; la solution, unique, explique après.
- [ ] Aucune pour un QCM
  > Un QCM a sa solution lui aussi : elle s'affiche à côté, une fois la réponse donnée.

:::hint
Combien d'explications l'apprenant lit-il après avoir répondu ?
:::

:::solution
**Exactement une**, pour tout exercice et tout QCM (`F021` sinon). Elle s'affiche dès que
l'apprenant a répondu.
:::
::::
:::::
