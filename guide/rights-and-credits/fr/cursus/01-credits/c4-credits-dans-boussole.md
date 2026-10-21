---
id: c4
objectives:
  - Écrire qui a écrit, révisé ou traduit une séance.
  - Nommer les auteurs et les contributeurs d'un parcours.
  - Noter d'où vient chaque source, et sa licence.
terms: [written-by, contributor, document-tracking, source-note, validator, agent]
written_by:
  - model: claude-opus-5-5
    tool: Claude Code
    date: 2026-10-21
---

:::::warmup
::ref{id=c3-a1}
:::::

## Qui a écrit cette séance {#written-by}

**« C'est une personne qui a écrit ça, ou une IA ? »** Chaque séance le dit.

En tête d'un fichier de séance, les lignes :term[écrit par]{id=written-by} (`written_by`)
nomment qui l'a écrite, puis qui l'a révisée ou traduite, une ligne chacun :

```yaml
written_by:
  - model: claude-opus-5-5
    tool: Claude Code
    date: 2026-10-21
  - name: Inès Haddad
    role: revised
    date: 2026-11-03
```

| Champ | Sens |
|---|---|
| `model` | une IA, par son **identifiant exact** (`claude-opus-5-5`), pas juste « une IA » |
| `tool` | l'outil qui l'a fait tourner : Claude Code, Codex, Gemini CLI… |
| `name` | une personne, à la place de `model` |
| `date` | le jour du travail |
| `role` | `wrote` (écrit, par défaut), `revised` (révisé) ou `translated` (traduit) |

Tu vois ces lignes à la **première étape** de la séance : regarde celle de cette séance. Une
séance sans elles reçoit l'avertissement `P012` du :term[validateur]{id=validator}.

:::warning
Quand tu révises ou traduis une séance, **ajoute** ta ligne. N'enlève et ne remplace jamais les
lignes déjà là : elles disent qui a fait le travail avant toi.
:::

::::exercise{#c4-a1 type=fill}
Complète.

Ton agent écrit une séance, puis tu la corriges. La ligne de l'agent [[1]]. Ta propre ligne a le
rôle [[2]]. Une séance sans `written_by` reçoit l'avertissement [[3]].

:::answer
1: choice *reste telle quelle | est remplacée par la tienne | est supprimée
2: choice *revised | wrote | translated
3: choice *P012 | F039 | F040
:::

:::hint
Relis l'avertissement : que deviennent les lignes déjà là ?
:::

:::solution
La ligne de l'agent **reste** ; la tienne vient après, avec le rôle **revised**. Sans aucun
`written_by`, le validateur dit `P012`.
:::
::::

## Qui fait le parcours {#authors}

**« Qui remercier pour ce parcours ? »** Sa page le dit.

Dans `subject.yaml`, deux listes s'affichent sur la page du parcours :

- `authors` : qui fait et signe le parcours, des personnes ou des organisations
  (`organisation: true`) ;
- `contributors` : qui a aidé, chacun avec un `role` qui dit comment.

:::example[Créditer le prof]
Inès construit un parcours à partir du poly de M. Durand, et Tom écrit les exercices du
chapitre 3 :

```yaml
authors:
  - name: Inès Haddad
contributors:
  - name: M. Durand
    role: poly dont le parcours est tiré
  - name: Tom Weber
    role: exercices du chapitre 3
```

La page de ce parcours-ci, elle, nomme un auteur : Boussole, une organisation.
:::

::::exercise{#c4-a2 type=match}
Le parcours d'Inès résume le poly de M. Durand, et une IA a écrit ses séances. Associe chacun à
l'endroit où il est crédité.

:::answer
Inès, qui fait et signe le parcours => authors, dans subject.yaml
Son université, qui signe le parcours avec elle => authors, avec organisation: true
M. Durand, dont le parcours résume le poly => contributors, avec un rôle
Le modèle d'IA qui a écrit les séances => written_by, dans chaque séance
:::

:::hint
`subject.yaml` parle de tout le parcours ; `written_by` parle d'une séance.
:::

:::solution
Les **authors** signent le parcours (une organisation avec `organisation: true`) ; les
**contributors** ont aidé, chacun avec un rôle ; l'IA qui a écrit une séance est nommée dans le
**`written_by`** de cette séance.
:::
::::

## D'où vient chaque source {#source-record}

**« Mon agent a trouvé un super article, mais il est tous droits réservés. »** Il le résume au
lieu de le copier.

Le :term[suivi des documents]{id=document-tracking}, `cursus/sources.yaml`, note chaque
document. Pour un document du web, quatre champs de plus :

| Champ | Sens |
|---|---|
| `url` | l'adresse de la page, `https://…` |
| `title` | son titre |
| `licence` | sa licence, comme la page la donne : `CC BY-SA 4.0`, `all rights reserved`… |
| `retrieved` | le jour où il a été récupéré |

Une entrée avec une `url` mais sans `licence` reçoit l'avertissement `F039`.

Ce qui va dans le dossier des sources dépend de la licence :

- la licence **permet une copie** : l'agent télécharge le document ;
- elle **ne le permet pas** : l'agent écrit une :term[note de source]{id=source-note},
  `web/<nom>.md`, son propre résumé de la page, avec son adresse et quelques courtes citations.
  Jamais une copie.

```yaml
- file: web/cache-http.md         # le résumé de l'agent, pas une copie
  url: https://example.com/blog/cache-http
  title: Le cache HTTP expliqué
  licence: tous droits réservés
  retrieved: 2026-10-21
- file: progit.pdf                # sa licence permet une copie
  url: https://git-scm.com/book/fr/v2
  title: Pro Git
  licence: CC BY-NC-SA 3.0
  retrieved: 2026-10-21
```

::::mcq{#c4-a3}
Ton :term[agent]{id=agent} trouve un article de blog utile marqué « Tous droits réservés ».
Qu'est-ce qu'il met dans ton dossier des sources ?

- [ ] Une copie de la page en PDF, avec son adresse
  > La licence ne permet pas de copie, même avec l'adresse.
- [x] Une note de source `web/<nom>.md`, son propre résumé avec l'adresse et de courtes citations
- [ ] Rien : il utilise l'article sans le dire
  > Le parcours doit dire d'où viennent ses idées.

:::hint
Qu'écrit un agent quand la licence ne permet pas de copie ?
:::

:::solution
**Une note de source** : son propre résumé, l'adresse et quelques courtes citations, avec une
entrée dans `cursus/sources.yaml` qui donne `url`, `title`, `licence: tous droits réservés` et
`retrieved`.
:::
::::

:::::checkpoint
::::mcq{#c4-c1}
Tu traduis en anglais une séance que ta camarade a écrite. Que fais-tu de son `written_by` ?

- [x] Tu gardes sa ligne, et tu ajoutes la tienne avec le rôle `translated`
- [ ] Tu remplaces sa ligne par la tienne : c'est toi qui as écrit le texte anglais
  > C'est elle qui a écrit la séance : sa ligne reste, la tienne vient après.
- [ ] Tu enlèves `written_by` : la séance a maintenant deux auteurs
  > Personne ne verrait plus qui l'a écrite, et le validateur dit `P012`.

:::hint
Quelles lignes un traducteur enlève-t-il ?
:::

:::solution
**Tu gardes sa ligne et tu ajoutes la tienne** avec `role: translated`. On voit les deux : qui a
écrit la séance, et qui l'a traduite.
:::
::::

::::mcq{#c4-c2}
Le validateur affiche `F039` sur une entrée du suivi des documents. Qu'est-ce qui manque ?

- [x] Sa `licence`
- [ ] Son empreinte, `sha256`
  > Sans empreinte, un changement passe inaperçu, mais ce n'est pas `F039`.
- [ ] Une ligne `written_by`
  > `written_by` concerne les séances, et son absence donne `P012`.

:::hint
`F039` concerne une entrée qui a une `url`.
:::

:::solution
**La `licence`.** Une entrée avec une `url` dit sous quelle licence la page est partagée, comme
sa page l'indique.
:::
::::
:::::
