---
id: c2
objectives:
  - Dire ce que permet « tous droits réservés », et ce que veut dire une licence absente.
  - Lire une licence Creative Commons à partir de ses lettres.
  - Trouver la licence d'une page web ou d'un document.
terms: [licence, all-rights-reserved, creative-commons, public-domain]
written_by:
  - model: claude-opus-5-5
    tool: Claude Code
    date: 2026-10-21
---

:::::warmup
::ref{id=c1-a1}
:::::

## Tous droits réservés {#all-rights-reserved}

**« Il n'y a pas de licence sur cette page, donc elle est libre ? »** C'est l'inverse.

Une :term[licence]{id=licence} est la réponse donnée d'avance par l'auteur à « puis-je copier,
partager ou adapter ceci ? ».

| Ce que dit le document | Ce que ça veut dire |
|---|---|
| « © 2026 A. Martin » ou « :term[Tous droits réservés]{id=all-rights-reserved} » | Tu peux le lire et t'en servir pour toi. Pour le copier, le partager ou l'adapter, demande. |
| Rien du tout | Pareil : le silence n'est pas une permission. |
| Une licence, comme CC BY | Oui d'avance, aux conditions qu'elle fixe. |

Être en libre lecture sur le web n'y change rien : libre à lire ne veut pas dire libre à copier.

::::mcq{#c2-a1}
Un article de blog que tu voudrais utiliser porte « © 2025 A. Martin. Tous droits réservés. »
Qu'est-ce que cela permet ?

- [ ] Le copier dans un parcours que tu vas partager, puisqu'il est en libre lecture
  > Libre à lire n'est pas libre à copier : l'auteur s'est réservé ces droits.
- [x] Le lire et t'en servir pour toi ; le copier ou le partager demande l'accord de l'auteur
- [ ] Rien du tout, pas même le lire
  > Tous droits réservés n'interdit pas de lire : l'auteur l'a publié pour être lu.

:::hint
Relis la première ligne du tableau.
:::

:::solution
**Le lire et t'en servir pour toi.** Pour le copier, le partager ou l'adapter, il te faut
l'accord de l'auteur.
:::
::::

## Creative Commons et domaine public {#creative-commons}

**« Je veux que mon cours soit réutilisé, sans qu'on ait à me demander à chaque fois. »** C'est à
ça que servent les licences :term[Creative Commons]{id=creative-commons} : l'auteur dit oui
d'avance, à conditions. Chaque condition tient en deux lettres.

| Lettres | Nom | Tu dois… |
|---|---|---|
| BY | Attribution | citer l'auteur, la source et la licence, et dire ce que tu as changé |
| SA | Partage à l'identique | partager ce que tu en fais sous la même licence |
| NC | Pas d'utilisation commerciale | ne pas t'en servir pour gagner de l'argent |
| ND | Pas de modification | le partager tel quel seulement, jamais une version modifiée |

Elles se combinent : CC BY, CC BY-SA, CC BY-NC, CC BY-NC-SA, CC BY-ND, CC BY-NC-ND. Toutes
demandent BY. Un numéro donne la version, comme 4.0.

Reste le :term[domaine public]{id=public-domain} : plus aucun droit sur l'œuvre. Soit sa
protection a expiré, après une durée fixée par la loi (souvent plusieurs décennies après la mort
de l'auteur), soit son auteur y a renoncé, par exemple avec **CC0**. Tout le monde peut la
copier, l'adapter et la partager ; citer la source reste une bonne pratique.

:::example[Lire CC BY-NC-SA]
*Pro Git*, un livre sur Git, est partagé sous CC BY-NC-SA 3.0. Lettre par lettre :

- **BY** : cite le livre et ses auteurs, Scott Chacon et Ben Straub ;
- **NC** : pas d'usage commercial ;
- **SA** : ce que tu en fais et partages passe sous la même licence.

Tu peux donc en tirer un parcours et le partager gratuitement, en citant le livre, sous CC
BY-NC-SA. Tu ne peux pas le vendre.
:::

::::exercise{#c2-a2 type=match}
Associe chaque élément de licence à ce qu'il te demande.

:::answer
BY => citer l'auteur et la source
SA => partager ce que tu en fais sous la même licence
NC => pas d'usage commercial
ND => le partager seulement tel quel
CC0 => aucune condition
:::

:::hint
BY pour « par » qui, S pour « share » (partager), N pour « non ».
:::

:::solution
**BY** cite l'auteur, **SA** garde la même licence, **NC** interdit l'usage commercial, **ND**
interdit les versions modifiées. **CC0** met l'œuvre dans le domaine public : aucune condition.
:::
::::

## Trouver la licence {#finding-a-licence}

**« Où est écrite la licence ? »** Rarement en gros. Regarde :

1. le **pied de page** du site : une ligne ©, un lien « Licence » ou « License » ;
2. la page **« À propos »** ou **« Conditions d'utilisation »** du site ;
3. la **première ou la dernière page** d'un PDF ;
4. le fichier **LICENSE** d'un dépôt de code ;
5. un **badge** avec les lettres CC : il mène à sa licence Creative Commons.

Beaucoup de documentations sont partagées exprès, sous une licence ouverte :

| Source | Licence |
|---|---|
| Wikipédia | CC BY-SA |
| *Pro Git* | CC BY-NC-SA 3.0 |
| La documentation de Python | PSF License |
| Beaucoup de manuels GNU | GNU Free Documentation License |

Rien trouvé ? Traite le document comme **tous droits réservés**.

::link[Les licences Creative Commons, expliquées par Creative Commons]{url=https://creativecommons.org/share-your-work/cclicenses/}

::::exercise{#c2-a3 type=fill}
Complète.

Tu cherches la licence d'une page web. Tu regardes d'abord [[1]] de la page. Un badge avec les
lettres CC mène à [[2]]. Si tu ne trouves rien, tu traites la page comme [[3]].

:::answer
1: choice *le pied de page | le titre | la première image
2: choice *une licence Creative Commons | une publicité | un abonnement payant
3: choice *tous droits réservés | domaine public | CC BY
:::

:::hint
Le silence n'est pas une permission.
:::

:::solution
D'abord le **pied de page** ; un badge CC mène à une **licence Creative Commons** ; et sans
licence trouvée, la page est **tous droits réservés**.
:::
::::

:::::checkpoint
::::mcq{#c2-c1}
Une prof veut publier son cours pour que chacun puisse le réutiliser et l'adapter, à condition
de la citer et de ne pas le vendre. Quelle licence convient ?

- [x] CC BY-NC
- [ ] CC BY-ND
  > ND interdit les versions modifiées : personne ne pourrait adapter le cours.
- [ ] CC0
  > CC0 ne demande rien, pas même le nom de la prof, et permet de vendre.
- [ ] Tous droits réservés
  > Chacun devrait demander avant de réutiliser : l'inverse de ce qu'elle veut.

:::hint
La citer : quelles lettres ? Ne pas le vendre : quelles lettres ?
:::

:::solution
**CC BY-NC** : BY pour être citée, NC pour interdire l'usage commercial, et pas de ND, donc
adapter est permis. CC BY-NC-SA conviendrait aussi, si elle veut en plus que les adaptations
restent sous la même licence. Avant de publier, elle vérifie les règles de son établissement,
qui peut avoir des droits sur le cours.
:::
::::
:::::
