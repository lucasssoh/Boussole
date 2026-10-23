---
id: f9
objectives:
  - Écrire une révision express ou un examen blanc, avec `::ref` et des questions neuves.
  - Ajuster le nombre de questions au temps accordé.
terms: [quick-review, mock-exam, nudge, notion, worked-example]
written_by:
  - model: claude-opus-5-5
    tool: Claude Code
    date: 2026-10-21
---

:::::warmup
::ref{id=f8-a4}
:::::

## Des séances chronométrées {#timed}

**Comment faire réviser un chapitre en temps limité ?** Avec une séance `kind: review` ou
`kind: exam` dans le plan. Son fichier ne fait que poser des questions : une introduction
facultative, puis des exercices, des QCM et des `::ref`. Ni :term[notion]{id=notion}, ni
:term[exemple résolu]{id=worked-example} (`P009` sinon).

| | :term[Révision express]{id=quick-review} (`review`) | :term[Examen blanc]{id=mock-exam} (`exam`) |
|---|---|---|
| Coups de pouce | disponibles avant chaque réponse | pas affichés |
| Correction | juste après chaque réponse | quand le temps est écoulé, ou quand l'apprenant termine |
| `duration` | de 5 à 120 minutes | de 5 à 240 minutes |

**Comment reposer une question d'une leçon sans la recopier ?** Avec `::ref{id=…}` : la question,
définie ailleurs dans le parcours, s'affiche ici telle quelle. C'est aussi ainsi qu'un échauffement
revient sur les séances d'avant. Un `::ref` vers un identifiant qui n'existe pas est une erreur
(`F030`).

:::example[La révision express des bases de Git]
```markdown
---
id: r9
objectives:
  - Retrouver sans notes les commandes de base, en temps limité.
terms: [index]
---

Cinq minutes, sans relire : les commandes des deux séances.

::ref{id=s1-a1}

::ref{id=s2-a2}

::::mcq{#r9-q1}
Que fait `git add index.html` ?

- [x] Il place `index.html` dans l'index, pour le prochain commit
- [ ] Il enregistre `index.html` dans l'historique
  > C'est le rôle de `git commit` ; `git add` prépare seulement.

:::hint
Pense à l':term[index]{id=index}.
:::

:::solution
**`git add`** place le fichier dans l'index ; le prochain `git commit` l'enregistrera.
:::
::::
```

Deux questions reprises des leçons, une neuve, et pas un seul titre `##`.
:::

::::mcq{#f9-a1}
Ta révision express contient un titre `## Les commandes {#commandes}`. Que dit le validateur ?

- [x] Un avertissement : une révision ne contient pas de notion
- [ ] Rien : le titre a bien son identifiant
  > Le titre est bien écrit, mais une révision ne fait que poser des questions.
- [ ] Une erreur : un titre sans identifiant
  > `{#commandes}` est bien là.

:::hint
Dans une séance, que commence un titre `##` ?
:::

:::solution
**Un avertissement** (`P009`) : un titre `##` ouvre une notion, et une séance chronométrée n'en a
pas. Une phrase d'introduction suffit.
:::
::::

::::mcq{#f9-a2 multiple=true}
Qu'est-ce qui est vrai dans un examen blanc ? (plusieurs réponses)

- [x] Les coups de pouce ne s'affichent pas
- [x] La correction arrive à la fin
- [ ] Un `:::example` peut y présenter la méthode
  > Une séance chronométrée ne contient que des questions (`P009`).
- [ ] Sa durée est une simple estimation
  > C'est le temps accordé : l'examen se termine quand il est écoulé.

:::hint
Un examen blanc se passe comme un vrai examen.
:::

:::solution
Pas de :term[coups de pouce]{id=nudge}, et la correction à la fin, comme à un vrai examen. Pas
d'exemple, et une durée qui est un vrai temps limite.
:::
::::

## Le bon nombre de questions {#minutes}

**Combien de questions pour cinq minutes ?** Boussole estime le temps de chaque question :

| Question | Minutes estimées |
|---|---|
| `mcq` | 1, ou 1,5 avec `multiple=true` |
| `order`, `match` | 2 |
| `numeric`, `text` | 3 |
| `fill` | 1 par trou à taper, 0,5 par trou à liste, 1 au moins |
| `free` | 8 |

Le total doit tomber entre 80 % et 110 % de la durée (`P008` sinon). Quand une question prend
plus ou moins de temps que son type, `minutes=N` le dit : `::::mcq{#r9-q2 minutes=2}`.

:::example[Compter les minutes de r9]
| Question | Type | Minutes |
|---|---|---|
| `s1-a1` | `mcq` | 1 |
| `s2-a2` | `order` | 2 |
| `r9-q1` | `mcq` | 1 |
| **Total** | | **4** |

La durée est de 5 minutes : il faut entre 4 (80 %) et 5,5 (110 %). Avec 4, `r9` passe. Une
question `text` de plus (3 minutes) ferait 7 : trop, le validateur signalerait `P008`.
:::

::::exercise{#f9-a3 type=numeric unit=minutes}
Une révision contient 4 QCM simples, 2 exercices `order` et un `fill` à 2 trous à liste. Combien
de minutes Boussole estime-t-il ?

:::answer
9
:::

:::hint
Compte type par type : les QCM, puis les `order`, puis le `fill`.
:::

:::hint
4 × 1, puis 2 × 2, puis 2 × 0,5 (et au moins 1 pour un `fill`).
:::

:::solution
4 × 1 + 2 × 2 + 2 × 0,5 = 4 + 4 + 1 = **9** minutes.
:::
::::

::::exercise{#f9-a4 type=fill}
Complète.

Pour une révision de 10 minutes, les questions doivent totaliser entre [[1]] et [[2]] minutes ;
sinon, le validateur signale [[3]].

:::answer
1: number 8
2: number 11
3: choice *P008 | P009 | F011
:::

:::hint
80 % et 110 % de 10.
:::

:::solution
Entre **8** et **11** minutes ; en dehors, **`P008`**.
:::
::::

:::::checkpoint
::::mcq{#f9-c1}
Ta révision de 5 minutes compte trois exercices `text`. Que fais-tu ?

- [x] Tu gardes un exercice `text` et tu ajoutes deux QCM
- [ ] Tu gardes les trois
  > 3 × 3 = 9 minutes : bien plus que 5,5, le maximum pour 5 minutes.
- [ ] Tu passes la durée à 6 minutes
  > 9 minutes dépassent encore 6,6, le maximum pour 6 minutes.

:::hint
Un exercice `text` compte 3 minutes, un QCM 1.
:::

:::solution
**Un `text` et deux QCM** : 3 + 1 + 1 = 5 minutes, pile la durée.
:::
::::
:::::
