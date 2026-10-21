---
id: c3
objectives:
  - Distinguer expliquer avec tes mots et copier.
  - Citer un court passage avec sa source.
  - Dire pourquoi un parcours cite ses sources, et comment une séance le fait.
terms: [quotation, licence, track]
written_by:
  - model: claude-opus-5-5
    tool: Claude Code
    date: 2026-10-21
---

:::::warmup
::ref{id=c2-a1}
:::::

## Avec tes mots {#own-words}

**« Mon agent peut juste copier les paragraphes utiles ? »** Mieux vaut qu'il les réexplique.

Les idées n'appartiennent à personne ; la formulation appartient à son auteur. Donc :

| Copier | Avec tes mots |
|---|---|
| Les phrases de l'auteur, telles quelles | L'idée, réexpliquée |
| Quelques mots remplacés par des synonymes | Un autre ordre, un autre exemple |
| La même structure, phrase après phrase | Des questions qui font utiliser l'idée |

Changer quelques mots, ce n'est pas écrire avec tes mots : c'est encore le texte de l'auteur,
à peine déguisé.

:::tip
C'est aussi mieux pour apprendre : un :term[parcours]{id=track} qui ne fait que copier le cours
n'y ajoute rien. Réexpliquer, avec un exemple et une question, c'est ce qui aide à retenir.
:::

::::mcq{#c3-a1}
Laquelle de ces façons de faire, c'est écrire **avec tes mots** ?

- [ ] Remplacer quelques mots par des synonymes dans chaque phrase du poly
  > Les phrases, leur ordre et leur structure sont toujours ceux de l'auteur.
- [x] Réexpliquer l'idée avec un exemple à toi, puis poser une question dessus
- [ ] Copier le paragraphe et ajouter sa source en dessous
  > Citer la source est bien, mais un paragraphe entier reste une copie, pas tes mots.

:::hint
Quel choix garde l'idée mais change tout le reste ?
:::

:::solution
**Réexpliquer l'idée avec ton propre exemple.** Tu gardes l'idée, qui n'appartient à personne,
et tu écris tout le reste toi-même.
:::
::::

## Une courte citation {#quotation}

**« Cette définition est parfaite telle quelle. »** Alors cite-la.

Une :term[citation]{id=quotation} donne les mots exacts de l'auteur :

- **courte** : une phrase ou deux, pas une page ;
- **entre guillemets**, pour que personne ne la prenne pour tes mots ;
- **avec sa source** : auteur, document, page.

Beaucoup de pays permettent les courtes citations pour l'enseignement, l'étude ou la critique,
sans demander, à des conditions de ce genre. Le détail varie : quand un passage est long,
demande.

:::example[Citer une définition]
Dans une séance sur le routage :

> « Un routeur choisit la route au plus long préfixe correspondant. » (M. Durand, *Réseaux*,
> cours 3, page 14)

Autrement dit : parmi les routes qui correspondent à l'adresse, la plus précise gagne. Pour
`10.1.2.3`, une route vers `10.1.0.0/16` l'emporte sur une route vers `10.0.0.0/8`.
:::

::::exercise{#c3-a2 type=order}
Remets dans l'ordre les étapes d'une bonne citation.

:::answer
1. Choisir un court passage qui mérite d'être donné mot pour mot
2. Le recopier exactement, entre guillemets
3. Donner sa source : auteur, document, page
4. L'expliquer avec tes mots juste après
:::

:::hint
Tu ne peux pas donner la source d'un passage avant de l'avoir choisi.
:::

:::solution
Choisir un passage **court**, le recopier **exactement** entre guillemets, donner sa
**source**, puis l'**expliquer** : la citation appuie ton texte, elle ne le remplace pas.
:::
::::

## Pourquoi un parcours cite ses sources {#why-cite}

**« Ça vient d'où ? »** Un parcours doit pouvoir répondre.

Citer permet :

- de **vérifier** : ouvrir l'original quand un point n'est pas clair ;
- de **créditer l'auteur** pour le travail qu'il a fait ;
- de **respecter la licence** : toutes les :term[licences]{id=licence} Creative Commons
  demandent BY ;
- de **retrouver**, dans six mois, d'où vient chaque passage.

Dans une séance, deux directives ouvrent la source à côté du texte :

```markdown
Le routeur choisit le plus long préfixe
(:pdf[poly, page 14]{src=Reseaux_Cours3.pdf page=14}).

::link[La documentation de git init]{url=https://git-scm.com/docs/git-init}
```

- `:pdf` ouvre un document de ton dossier des sources, à une page si tu la donnes ;
- `:link` (dans une phrase) ou `::link` (sur sa propre ligne) ouvre une page web, par son
  adresse entière.

::::mcq{#c3-a3}
Dans une séance, tu veux citer la page 14 du poly de ton dossier des sources, ouverte à côté du
texte. Quelle directive ?

- [x] `:pdf[poly, page 14]{src=Reseaux_Cours3.pdf page=14}`
- [ ] `:link[poly]{url=Reseaux_Cours3.pdf}`
  > `link` sert pour une page web, avec une adresse entière qui commence par `https://`.
- [ ] `::video[poly]{src=Reseaux_Cours3.pdf}`
  > `video` lit une vidéo, pas un PDF.

:::hint
Quelle directive porte le nom du type de document ?
:::

:::solution
**`:pdf`**, avec `src` pour le fichier et `page` pour la page. `:link` sert pour les pages web,
`::video` pour les vidéos.
:::
::::

:::::checkpoint
::::mcq{#c3-c1}
Ton agent veut mettre une page entière du poly de ton prof dans une séance, mot pour mot, avec
la source. Est-ce une citation ?

- [ ] Oui, puisque la source est donnée
  > Une citation est courte : une page entière est une copie, même avec sa source.
- [x] Non, c'est trop long : mieux vaut l'expliquer avec ses mots et citer une phrase ou deux
- [ ] Oui, du moment que c'est entre guillemets
  > Des guillemets ne rendent pas court un long passage.

:::hint
Quelle longueur fait une citation ?
:::

:::solution
**Non.** Une citation, c'est une phrase ou deux. Pour une page entière, l'agent l'explique avec
ses mots, cite seulement ce qui doit être exact, et renvoie à la page avec `:pdf`.
:::
::::
:::::
