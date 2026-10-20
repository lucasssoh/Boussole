---
id: g4
objectives:
  - Dire comment Boussole repère un document nouveau ou modifié.
  - Savoir quoi demander à ton agent quand un document arrive.
terms: [registre, validateur, agent]
---

:::::warmup
::ref{id=g3-a1}
:::::

## Le registre des documents {#registre}

Tu n'as pas tous tes documents le premier jour : les transparents arrivent cours après cours,
une version corrigée remplace l'ancienne. Le :term[registre]{id=registre}
(`cursus/sources.yaml`) note ce que le cours a fait de chaque document, avec son **empreinte**,
un code qui change dès que le fichier change.

Le :term[validateur]{id=validateur} compare le registre au dossier, sous-dossiers compris, et
prévient : document **nouveau** (`F033`), **modifié** (`F034`), **disparu** (`F035`), ou **en
double** à deux endroits (`F036`). La commande `boussole sources ~/cours/Optimisation` en donne la
liste.

::::exercise{#g4-a1 type=match}
Associe chaque avertissement à sa cause.

:::answer
F033 => un document nouveau, pas encore pris en compte
F034 => un document modifié depuis que le cours l'a utilisé
F035 => un document noté au registre mais disparu du dossier
F036 => le même document à deux endroits
:::

:::hint
Les numéros suivent l'ordre du texte : nouveau, modifié, disparu, en double.
:::

:::solution
`F033` nouveau, `F034` modifié, `F035` disparu, `F036` en double. Les quatre se règlent avec
l'agent.
:::
::::

## Ajouter un document {#ajouter}

Ajoute simplement le fichier dans le dossier du cours, puis lance ton
:term[agent]{id=agent} comme d'habitude. Il commence toujours par le validateur : il voit le
nouveau document, le lit, et l'intègre. Il **ajoute** des séances avec de nouveaux numéros, sans
renuméroter ni réécrire ce que tu as déjà étudié, et te demande ton accord si le plan change.

::::mcq{#g4-a2}
Tu ajoutes les transparents du cours 7. Que se passe-t-il pour tes séances déjà faites ?

- [ ] Elles sont réécrites pour intégrer le cours 7
  > Non : les séances déjà écrites restent ; seule une version corrigée d'un document les ferait corriger.
- [x] Elles restent telles quelles ; le cours 7 donne de nouvelles séances, à la suite
- [ ] Elles sont renumérotées pour garder l'ordre
  > Jamais : ta progression est attachée à leurs numéros.

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

- [x] `cours3.pdf` a changé depuis que le cours l'a utilisé
- [ ] `cours3.pdf` est nouveau
  > Un document nouveau donne `F033`.
- [ ] `cours3.pdf` est en double
  > Un doublon donne `F036`.

:::hint
Relis la liste des quatre avertissements.
:::

:::solution
`F034` : le document a **changé** (son empreinte n'est plus la même). L'agent cherche ce qui a
changé et corrige seulement les séances concernées.
:::
::::
:::::
