---
id: g2
objectives:
  - Préparer un parcours avec `boussole init`.
  - Suivre ton agent du plan à la première séance.
terms: [agent, plan, validateur, lien]
written_by:
  - model: claude-opus-5-5
    tool: Claude Code
    date: 2026-10-21
---

:::::warmup
::ref{id=g1-a1}
:::::

## Préparer un parcours {#folder}

**« Par où je commence avec mon IA ? »** Par un dossier.

Range-y tes documents, en sous-dossiers si tu veux. Un parcours peut partir de :

- tes supports de cours ;
- un manuel de logiciel ;
- un guide d'entreprise ;
- la documentation d'un langage de programmation.

Puis, dans un terminal :

```
boussole init ~/cours/Optimisation
```

Boussole ajoute dans le dossier :

| Fichier | Rôle |
|---|---|
| `subject.yaml` | le nom et la langue du parcours |
| `cursus/` | le parcours, que l'agent va écrire |
| `AGENTS.md`, `.boussole/` | les fiches méthode de ton :term[agent]{id=agent} |

Tes documents restent à leur place.

:::tip
Ouvre ton agent **dans ce dossier** et demande-lui : « Construis le parcours. »
:::

**« Et si je n'ai aucun document ? »** Pars de rien :

```
boussole new "Python" --goal "automatiser mon travail"
```

Ton agent te demande ce que tu veux savoir faire, cherche des sources ouvertes (documentation
officielle, cours d'université, manuels libres) et te les montre avant de faire le plan. Chaque
source garde son adresse et sa licence ; dans les séances, un :term[lien]{id=lien} l'ouvre à côté
du texte.

::::exercise{#g2-a1 type=order}
Remets les étapes dans l'ordre, du dossier vide au parcours ouvert.

:::answer
1. Ranger tes documents dans un dossier
2. Lancer `boussole init` sur ce dossier
3. Demander à ton agent de construire le parcours
4. Valider le plan qu'il te propose
5. Ouvrir le parcours avec `boussole open`
:::

:::hint
On ne valide un plan qu'une fois que l'agent l'a proposé.
:::

:::solution
Documents d'abord, puis `boussole init`, puis l'agent, qui propose le plan ; tu le valides ; et
`boussole open` t'ouvre le parcours dans le navigateur.
:::
::::

## Le plan, puis les séances {#plan}

**« Et si l'IA part dans une direction que je n'ai pas choisie ? »** Elle te demande d'abord.

L'agent avance en trois temps :

1. **Ta façon d'apprendre.** Quelques questions à choix : une question après chaque notion ? un
   exemple d'abord ? des séances de combien de temps ?
2. **Le plan à valider.** Chapitres, séances, durées, objectifs, révisions express et examen
   blanc : c'est le :term[plan]{id=plan}.
3. **Les séances.** Une à une, chacune vérifiée par le :term[validateur]{id=validateur}.

:::note
Tant que tu ne l'as pas validé, le plan reste **proposé** : aucune séance ne s'écrit.
:::

Tu peux commencer la première séance pendant que l'agent écrit la suivante : elle apparaît dès
qu'elle est prête.

::::exercise{#g2-a2 type=fill}
Complète.

L'agent propose le plan avec le statut [[1]]. Il passe à [[2]] seulement quand [[3]] le valides.
Pour ouvrir ensuite le parcours, la commande est [[4]].

:::answer
1: choice *proposed | approved | written
2: choice *approved | proposed | finished
3: choice *tu | l'agent | Boussole
4: choice *boussole open | boussole init | boussole validate
:::

:::hint
`init` prépare le dossier, `validate` vérifie le parcours, une seule commande l'affiche.
:::

:::solution
`proposed`, puis `approved` quand **tu** le valides ; `boussole open` ouvre le parcours.
`boussole init` prépare un dossier, `boussole validate` vérifie le parcours.
:::
::::

:::::checkpoint
::::mcq{#g2-c1}
Ton agent veut écrire des séances alors que le plan est encore `proposed`. Que fais-tu ?

- [ ] Tu le laisses faire, il sait ce qu'il fait
  > Un plan pas encore validé peut encore changer : les séances attendent ton accord.
- [x] Tu relis le plan, puis tu le valides ou tu demandes des changements
- [ ] Tu supprimes `cursus/` et tu recommences
  > Inutile : il suffit de répondre à sa demande de validation.

:::hint
Qui décide du plan, d'après la séance précédente ?
:::

:::solution
Relis le plan qu'il te montre et **valide-le** ou demande des changements. Les séances ne
s'écrivent qu'après ton accord.
:::
::::
:::::
