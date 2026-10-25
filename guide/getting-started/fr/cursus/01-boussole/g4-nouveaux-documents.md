---
id: g4
objectives:
  - Dire comment Boussole repère un document nouveau ou modifié.
  - Savoir quoi demander à ton agent quand un document arrive.
terms: [registre, validateur, agent]
prerequisites: [boussole-guide.boussole.g2]
written_by:
  - model: claude-opus-5-5
    tool: Claude Code
    date: 2026-10-21
---

:::::warmup
::ref{id=g3-a1}
:::::

## Le suivi des documents {#record}

**« Un nouveau poly est arrivé : qu'est-ce qui change ? »** Tu n'as pas tout le premier jour :

- les transparents arrivent semaine après semaine ;
- une version corrigée remplace l'ancienne.

Le :term[suivi des documents]{id=registre} (`cursus/sources.yaml`) note ce que le parcours a
fait de chaque document, avec son **empreinte** : un code qui change dès que le fichier change.

Le :term[validateur]{id=validateur} compare ce suivi au dossier, sous-dossiers compris, et
prévient quand un document est :

| Code | Le document est… |
|---|---|
| `F033` | nouveau |
| `F034` | modifié |
| `F035` | disparu |
| `F036` | en double, à deux endroits |

Pour voir la liste : `boussole sources ~/cours/Optimisation`.

::::exercise{#g4-a1 type=match}
Associe chaque avertissement à sa cause.

:::answer
F033 => un document nouveau, pas encore pris en compte
F034 => un document modifié depuis que le parcours l'a utilisé
F035 => un document suivi mais disparu du dossier
F036 => le même document à deux endroits
:::

:::hint
Les numéros suivent l'ordre du tableau : nouveau, modifié, disparu, en double.
:::

:::solution
`F033` nouveau, `F034` modifié, `F035` disparu, `F036` en double. Les quatre se règlent avec
l'agent.
:::
::::

## Ajouter un document {#adding}

Deux gestes suffisent :

1. **Ajoute le fichier** dans le dossier du parcours.
2. **Lance ton :term[agent]{id=agent}** comme d'habitude.

L'agent commence par le validateur. Il voit le nouveau document, le lit et l'intègre :

- il **ajoute** des séances, avec de nouveaux numéros ;
- il garde intact ce que tu as déjà étudié ;
- il te demande ton accord si le plan change.

::::mcq{#g4-a2}
Tu ajoutes les transparents du cours 7. Que deviennent tes séances déjà faites ?

- [ ] Elles sont réécrites pour intégrer le cours 7
  > Les séances déjà écrites restent ; seule une version corrigée d'un document les ferait corriger.
- [x] Elles restent telles quelles ; le cours 7 donne de nouvelles séances, à la suite
- [ ] Elles sont renumérotées pour garder l'ordre
  > Ta progression est attachée à leurs numéros : ils ne bougent pas.

:::hint
À quoi est attachée ta progression ?
:::

:::solution
Elles restent : ta progression est attachée à leurs numéros. Le cours 7 ajoute des séances à la
suite, avec de nouveaux numéros, et l'agent te montre ce qui change dans le plan.
:::
::::

:::::checkpoint
::::mcq{#g4-c1}
Le validateur signale `F034` pour `cours3.pdf`. Que s'est-il passé ?

- [x] `cours3.pdf` a changé depuis que le parcours l'a utilisé
- [ ] `cours3.pdf` est nouveau
  > Un document nouveau donne `F033`.
- [ ] `cours3.pdf` est en double
  > Un doublon donne `F036`.

:::hint
Relis le tableau des quatre avertissements.
:::

:::solution
`F034` : le document a **changé** (son empreinte n'est plus la même). L'agent cherche ce qui a
changé et corrige seulement les séances concernées.
:::
::::
:::::
