---
id: g2
objectives:
  - Lancer `boussole init` sur un dossier de documents.
  - Suivre les étapes de l'agent jusqu'à la première séance.
terms: [agent, plan, validateur]
---

:::::warmup
::ref{id=g1-a1}
:::::

## Préparer le dossier {#dossier}

Un cours, c'est **un dossier**. Tu y ranges tes documents comme tu veux, en sous-dossiers si tu
préfères.

Pas forcément une matière d'école : ça marche aussi pour

- un manuel de logiciel ;
- un guide d'entreprise ;
- un langage de programmation.

Puis, dans un terminal :

```
boussole init ~/cours/Optimisation
```

Boussole ajoute dans le dossier :

| Fichier | Rôle |
|---|---|
| `subject.yaml` | le nom et la langue du cours |
| `cursus/` | le cours, que l'agent va écrire |
| `AGENTS.md`, `.boussole/` | les instructions pour ton :term[agent]{id=agent} |

Tes documents restent à leur place.

:::tip
Ouvre ensuite ton agent **dans ce dossier** et demande-lui simplement : « Construis le cours. »
:::

::::exercise{#g2-a1 type=order}
Remets les étapes dans l'ordre, du dossier vide au cours ouvert.

:::answer
1. Ranger tes documents dans un dossier
2. Lancer `boussole init` sur ce dossier
3. Demander à ton agent de construire le cours
4. Approuver le plan qu'il te propose
5. Ouvrir le cours avec `boussole open`
:::

:::hint
On ne peut approuver un plan qu'une fois que l'agent l'a proposé.
:::

:::solution
Documents d'abord, puis `boussole init`, puis l'agent, qui propose le plan ; tu l'approuves ; et
`boussole open` t'ouvre le cours dans le navigateur.
:::
::::

## Le plan, puis les séances {#plan}

L'agent avance en trois temps :

1. **Il te pose quelques questions à choix** : un QCM après chaque notion ? un exemple d'abord ?
   des séances de combien de temps ?
2. **Il te propose un :term[plan]{id=plan}** : chapitres, séances, durées, objectifs, révisions
   et examen blanc.
3. **Il écrit les séances une à une**, et lance le :term[validateur]{id=validateur} après
   chacune.

:::note
Tant que tu ne l'as pas approuvé, le plan reste **proposé** : l'agent n'écrit aucune séance.
:::

Tu peux étudier la première séance pendant qu'il écrit la suivante : Boussole affiche les
nouveautés dès qu'elles sont écrites.

::::exercise{#g2-a2 type=fill}
Complète.

L'agent propose le plan avec le statut [[1]]. Il passe à [[2]] seulement quand [[3]] l'acceptes.
Pour ouvrir ensuite le cours, la commande est [[4]].

:::answer
1: choice *proposed | approved | written
2: choice *approved | proposed | finished
3: choice *tu | l'agent | Boussole
4: choice *boussole open | boussole init | boussole validate
:::

:::hint
`init` prépare le dossier, `validate` vérifie le cours, une seule commande l'affiche.
:::

:::solution
`proposed`, puis `approved` quand **tu** l'acceptes ; `boussole open` ouvre le cours. `boussole
init` prépare un dossier, `boussole validate` vérifie le format.
:::
::::

:::::checkpoint
::::mcq{#g2-c1}
Ton agent veut écrire des séances alors que le plan est encore `proposed`. Que fais-tu ?

- [ ] Tu le laisses faire, il sait ce qu'il fait
  > Les skills lui interdisent d'écrire une séance avant ton accord : un plan non validé peut encore changer.
- [x] Tu relis le plan, puis tu l'approuves ou tu demandes des changements
- [ ] Tu supprimes `cursus/` et tu recommences
  > Inutile : il suffit de répondre à sa demande d'approbation.

:::hint
Qui décide du plan, d'après la séance précédente ?
:::

:::solution
Relis le plan qu'il te montre et **approuve-le** ou demande des changements. Les séances ne
s'écrivent qu'après ton accord.
:::
::::
:::::
