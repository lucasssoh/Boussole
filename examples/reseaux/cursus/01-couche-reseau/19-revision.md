---
id: r19
objectives:
  - Retrouver sans notes ce que la couche 3 apporte, en temps limité.
terms: [mac]
---

Révision de la séance sur le rôle de la couche réseau : 5 minutes, 3 questions.

::ref{id=s11-a1}

::ref{id=s11-a2}

::::mcq{#r19-q1}
Pourquoi ne peut-on pas router Internet avec des :term[adresses MAC]{id=mac} ?

- [ ] Elles sont trop longues
  > 48 bits, c'est moins qu'une adresse IPv6 : la longueur n'est pas le problème.
- [x] Elles sont plates : impossible de les regrouper par préfixe
- [ ] Elles changent à chaque redémarrage
  > Non : une adresse MAC est fixée par le fabricant de l'interface.

:::hint
Une table de routage regroupe les destinations : qu'est-ce qui permet de regrouper des adresses ?
:::

:::solution
Les adresses MAC n'ont pas de structure hiérarchique : deux machines voisines n'ont aucun préfixe
commun. Une table de routage devrait donc contenir une entrée par interface au monde.
:::
::::
