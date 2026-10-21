---
id: s11
objectives:
  - Dire ce que la couche 3 apporte par rapport à la couche 2.
  - Distinguer datagramme et circuit virtuel.
terms: [trame, mac, agregation, circuit-virtuel, forwarding]
sources:
  - Reseaux_Cours_Intro_2026.pdf#page=14
written_by:
  - model: claude-opus-5-5
    tool: Claude Code
---

:::::warmup
::::exercise{#s11-w1 type=text}
Routage ou forwarding : lequel **consulte** la table, pour chaque paquet ?

:::answer
forwarding
le forwarding
commutation
la commutation
:::

:::hint
L'un **construit** la table (lentement), l'autre la **lit** (pour chaque paquet).
:::

:::solution
Le **forwarding** consulte la table (la FIB) pour chaque paquet, en nanosecondes. Le
**routage** construit la table, en secondes, en échangeant des annonces.
:::
::::
:::::

## Ce que la couche 3 apporte {#couche3}

:::recall
- :term[Trame]{id=trame} : l'unité de la couche 2, sur **un seul lien**.
- :term[Adresse MAC]{id=mac} : 48 bits, **plate**, valable sur le lien local.
:::

La couche 2 achemine une trame **entre deux interfaces d'un même lien**. Son adressage est
**plat** : 48 bits sans structure hiérarchique, donc impossible à regrouper. Une table de
commutation Ethernet mondiale contiendrait une entrée par interface existante, de l'ordre de
$10^{10}$.

La couche 3 apporte trois choses que la couche 2 n'a pas :

1. un **adressage hiérarchique**, donc :term[agrégeable]{id=agregation} ;
2. un **relayage entre liens** (le routeur) ;
3. un **horizon global** : un chemin de bout en bout à travers des réseaux qu'on ne contrôle pas.

::pdf[Cours, la couche réseau]{src=Reseaux_Cours_Intro_2026.pdf page=14}

:::example[Ce que vaut la hiérarchie]
Grâce à l'agrégation, la table du cœur d'Internet contient de l'ordre de **1 million de
préfixes IPv4** pour environ 80 000 systèmes autonomes, et non $10^{10}$ entrées. C'est comme
une adresse postale : un centre de tri national ne connaît pas chaque rue, seulement les
départements.
:::

::::exercise{#s11-a1 type=numeric unit=bits}
Combien de bits fait une adresse MAC ?

:::answer
48
:::

:::hint
Une adresse MAC s'écrit avec 6 octets, comme `3c:52:82:1a:9f:04`.
:::

:::solution
48 bits (6 octets). Les adresses MAC sont attribuées par fabricant, sans lien avec le réseau
où se trouve la machine : deux machines voisines n'ont aucun préfixe commun, donc on ne peut
pas les regrouper dans une table.
:::
::::

## Datagramme ou circuit virtuel {#datagramme-circuit}

:::recall
:term[Datagramme et circuit virtuel]{id=circuit-virtuel} : avec ou sans état dans les routeurs.
:::

```
DATAGRAMME (IP)                        CIRCUIT VIRTUEL (ATM, MPLS)
chaque paquet est indépendant          on établit un chemin d'abord
adresse complète dans chaque paquet    puis un identifiant court
état dans les routeurs : AUCUN         état : une entrée par circuit
```

::::example[Une panne de routeur]
Un routeur tombe au milieu d'un transfert. En **datagramme**, les paquets suivants prennent un
autre chemin dès que le routage a convergé : la communication continue. En **circuit
virtuel**, le circuit passait par ce routeur : il faut en établir un nouveau.

:::graph[R2 tombe : les paquets passent par R3]{layout=layers}
A "Hôte A" !
A -> R1 !
R1 -- R2 : en panne
R1 -> R3 : 2 !
R2 -- R4
R3 -> R4 : 3 !
R4 -> B !
B "Hôte B" !
:::
::::

::::mcq{#s11-a2}
Dans quel modèle les routeurs gardent-ils un état **par communication** ?

- [ ] Datagramme
  > Non : en datagramme, chaque paquet porte l'adresse complète et les routeurs ne gardent rien.
- [x] Circuit virtuel
- [ ] Les deux
  > Non : c'est justement ce qui distingue les deux modèles.

:::hint
Lequel des deux doit « établir » quelque chose avant d'envoyer ?
:::

:::solution
Le circuit virtuel : chaque routeur traversé garde une entrée par circuit établi.
:::
::::

:::::checkpoint
::::mcq{#s11-c1 multiple=true}
Qu'apporte la couche 3 que la couche 2 n'a pas ?

- [x] Un adressage hiérarchique
- [x] Le relayage entre liens différents
- [ ] Un adressage plat sur 48 bits
  > C'est l'adressage MAC, celui de la couche 2.

:::hint
Pense à ce qui manque à un commutateur pour relier deux réseaux différents.
:::

:::solution
L'adressage hiérarchique (agrégeable) et le relayage entre liens, plus un horizon global.
:::
::::
:::::

:::::practice
::::exercise{#s11-p1 type=free}
Un opérateur propose de remplacer IP par un réseau tout en circuits virtuels pour garantir la
qualité de service. Donne un argument pour et deux arguments contre, en t'appuyant sur la
notion d'état dans les routeurs.

:::hint
Combien de communications simultanées traversent un routeur du cœur d'Internet ?
:::

:::solution
**Pour** : on peut réserver des ressources par circuit, donc garantir un débit.
**Contre** : un routeur du cœur verrait des millions de circuits, soit autant d'entrées d'état
à maintenir ; et une panne casse tous les circuits qui passaient par lui, alors qu'en
datagramme le trafic est simplement redirigé.
:::
::::
:::::
