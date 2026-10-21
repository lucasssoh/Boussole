---
id: c5
objectives:
  - Dire qui est responsable quand un parcours est partagé.
  - Trier ce que tu peux en général partager et ce qui demande l'accord de l'auteur.
  - Créditer le travail des autres, et dire honnêtement ce qu'une IA a écrit.
terms: [track, licence, contributor, written-by]
written_by:
  - model: claude-opus-5-5
    tool: Claude Code
    date: 2026-10-21
---

:::::warmup
::ref{id=c4-a3}
:::::

## Qui partage en répond {#sharing}

**« Un camarade veut mon parcours. Boussole peut le lui envoyer ? »** Boussole n'envoie rien :
c'est toi.

Un :term[parcours]{id=track} est un dossier sur ta machine. Il contient :

- les séances, le plan et le lexique, dans `cursus/` ;
- **les documents** dont il est tiré : peut-être les slides de ton prof.

Envoie le dossier, et tu envoies les documents avec.

Boussole est comme un lecteur vidéo. Rien n'empêche techniquement de mettre sur un site de
vidéos un film que tu as acheté ; c'est la loi qui l'interdit, et c'est celui qui le met en
ligne qui en répond. Ici, pareil :

- entre deux personnes, chacun partage ce qu'il choisit, et **en est responsable** ;
- partager des documents que tu n'as pas le droit de partager est de la responsabilité de
  **celui qui les partage**, pas de Boussole ;
- qui emballe et publie un parcours contenant des documents **accepte la responsabilité d'avoir
  le droit** de les partager.

:::note
Boussole prévoit une façon d'emballer et de partager des parcours. Le principe ci-dessus s'y
appliquera.
:::

::::mcq{#c5-a1}
Tu compresses le dossier de ton parcours, slides de ton prof comprises, et tu le postes dans le
groupe de discussion de la classe. Qui est responsable du partage des slides ?

- [x] Toi, qui les as partagées
- [ ] Boussole, qui a construit le parcours
  > Boussole travaille sur ta machine et n'a rien envoyé : c'est toi qui as posté le dossier.
- [ ] Ton agent, qui a écrit les séances
  > L'agent a écrit des séances ; le choix d'envoyer les slides était le tien.

:::hint
Qui a posté le dossier ?
:::

:::solution
**Toi.** Celui qui partage répond de ce qu'il partage. Avant d'envoyer des documents qui ne sont
pas les tiens, demande à leur auteur.
:::
::::

## Ce que tu peux en général partager {#what-to-share}

**« Alors qu'est-ce que je peux envoyer ? »** Trie le dossier.

| En général oui | En général non, sans accord |
|---|---|
| Les questions que tu as écrites | Les slides et le poly de ton prof |
| Des résumés avec tes mots | Les feuilles de TD et les annales |
| Des documents sous une :term[licence]{id=licence} qui le permet, à ses conditions | Des scans de pages de manuel |
| Des œuvres du domaine public | Des pages web tous droits réservés |

Deux points d'attention :

- un parcours qui suit pas à pas le cours d'un prof reste construit sur son travail :
  **préviens-le, et crédite-le** ;
- retiré du dossier, un document ne peut plus être ouvert par les séances qui le citent : la
  personne avec qui tu partages ajoute sa propre copie, par exemple celle que le prof lui a
  donnée.

:::example[Trier un parcours avant de le partager]
Inès a demandé à M. Durand, qui a répondu : « Mon poly reste dans la classe ; tes questions,
partage-les comme tu veux. »

Elle partage avec une amie d'une autre école :

- **gardé** : ses séances et ses questions, et *Pro Git* (CC BY-NC-SA 3.0), crédité,
  gratuitement ;
- **retiré** : le poly de M. Durand ;
- **dans `subject.yaml`** : M. Durand parmi les contributeurs, avec son rôle.
:::

::::mcq{#c5-a2 multiple=true}
Lesquels peux-tu **en général** partager sans demander ?

- [x] Les questions que tu as écrites toi-même
- [x] Un chapitre de *Pro Git*, sous CC BY-NC-SA, crédité et gratuitement
- [ ] Les slides de ton prof
  > Elles appartiennent à ton prof : demande d'abord.
- [ ] Des scans de ton manuel
  > Le manuel appartient à ses auteurs et à son éditeur.

:::hint
Relis la colonne « En général oui ».
:::

:::solution
**Tes propres questions**, et **un document dont la licence le permet**, à ses conditions (ici :
crédité, pas d'usage commercial, même licence). Les slides de ton prof et le manuel demandent
l'accord de leurs auteurs.
:::
::::

## Respecter le travail des autres {#respect}

**« Qui a écrit ce cours, au fond ? »** Qui lit ton parcours doit pouvoir le savoir.

Écrire un cours prend des heures. Le crédit, c'est ce qui fait que ceux qui les écrivent
continuent d'écrire, et de partager. Concrètement :

- **crédite** les profs et les :term[contributeurs]{id=contributor} dont tu as utilisé le
  travail ;
- **garde** la ligne :term[écrit par]{id=written-by} de quelqu'un d'autre quand tu révises sa
  séance ;
- **ne présente jamais** un cours généré ou copié comme ton propre travail ;
- **dis ce qu'une IA a écrit** : son identifiant exact dans `written_by`, et ta propre ligne
  quand tu l'as révisé.

:::tip
Être honnête sur une IA ne coûte rien : « une IA l'a écrit, je l'ai vérifié » est une
information utile pour qui étudie le parcours.
:::

::::mcq{#c5-a3}
Tu partages un parcours dont une IA a écrit les séances, que tu as ensuite corrigées. Comment
doit-il se présenter ?

- [x] Dans chaque séance, la ligne du modèle avec `wrote`, puis la tienne avec `revised`
- [ ] Seulement ton nom, avec `wrote` : tu as tout vérifié
  > Cela cache le travail de l'IA : on ne sait plus qui a écrit quoi.
- [ ] Pas de `written_by` du tout, pour faire simple
  > Personne ne voit plus qui a écrit la séance, et le validateur dit `P012`.

:::hint
Que dit chaque rôle : `wrote`, `revised` ?
:::

:::solution
**La ligne du modèle, puis la tienne.** L'IA a écrit, tu as révisé : les deux sont vrais, et les
deux s'affichent à la première étape de la séance.
:::
::::

:::::checkpoint
::::mcq{#c5-c1}
Ton prof a accepté que tu partages ton parcours avec la classe, pas en ligne. Un mois plus tard,
quelqu'un propose de le mettre sur un site public. Que fais-tu ?

- [x] Tu redemandes au prof : son accord valait pour la classe seulement
- [ ] Tu le publies : le prof a déjà dit oui
  > Le prof a dit oui pour la classe, pas pour tout le web.
- [ ] Tu le publies, et le site le retirera s'il y a un problème
  > Celui qui partage en répond : le site ne prend pas la responsabilité à ta place.

:::hint
À quoi exactement le prof a-t-il dit oui ?
:::

:::solution
**Tu redemandes.** Un accord couvre ce qui a été demandé : « dans la classe » n'est pas « en
ligne ». Beaucoup de profs acceptent, souvent sous une licence de leur choix.
:::
::::
:::::
