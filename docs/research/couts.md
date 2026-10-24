# Coût des agents : mesure de référence

Mesure du 24 octobre 2026, prise dans les journaux de Claude Code (`~/.claude/projects/…/subagents/*.jsonl`,
champ `usage` de chaque réponse). Modèle : `claude-opus-5-5` partout. Elle sert de point de
comparaison : toute optimisation de l'orchestration, des fiches méthode ou du choix des modèles
se compare à ces chiffres, **à qualité au moins égale**.

## Les chiffres

| Travail | Agents | Appels | Sortie | Cache lu | Cache écrit |
|---|---|---|---|---|---|
| Écriture d'A&C, 59 séances, un seul agent | 1 | 166 | 274 000 | 94 460 000 | 2 807 000 |
| Révision d'A&C (59 séances) et d'OC (25), un agent par chapitre | 16 | 401 | 646 000 | 36 930 000 | 3 474 000 |
| Révision d'un chapitre, en moyenne | 1 | 25 | 40 000 | 2 300 000 | 217 000 |

Les entrées hors cache sont négligeables (moins de 1 000 tokens par agent).

## Ce qu'ils disent

En « équivalents-entrée », avec les rapports habituels du tarif Anthropic (cache lu ≈ 0,1,
cache écrit ≈ 1,25, sortie ≈ 5 ; à vérifier sur la grille du moment) :

| Travail | Sortie | Cache lu | Cache écrit | Total |
|---|---|---|---|---|
| Écriture d'A&C | 1,4 M | 9,4 M | 3,5 M | **14,3 M** |
| Révision, 16 chapitres | 3,2 M | 3,7 M | 4,3 M | **11,2 M** |
| Révision, un chapitre | 0,2 M | 0,2 M | 0,3 M | **0,7 M** |

- **Un seul agent pour tout un parcours** : les deux tiers du coût sont la relecture du
  contexte, qui grossit à chaque séance et se relit à chaque appel.
- **Un agent par chapitre** : le coût se partage entre le texte produit et le contexte de
  départ de chaque agent (fiches méthode, format, séances du chapitre, sources). Le contexte fixe
  (`FORMAT.md` et trois fiches, environ 13 000 tokens) n'en est qu'une petite part : le reste,
  c'est ce que l'agent va lire en plus.
- **Onze agents à la fois** ont épuisé la fenêtre de session avant d'avoir écrit une ligne ; trois
  à la fois ont tenu.

## Ce qui a été changé après cette mesure

- `boussole index` : la liste des questions, pour choisir les `::ref` sans ouvrir les autres
  chapitres.
- Les choix des QCM sont mélangés par Boussole : les agents n'ont plus à déplacer les bonnes
  réponses.
- L'`AGENTS.md` installé : un agent par chapitre, trois à la fois, chacun n'écrit que dans son
  dossier ; un modèle selon la tâche.

Non retenu : extraire le texte des PDF une fois pour toutes. Les transparents sont souvent des
images, des scans de manuscrits ou des mises en page qui ne se lisent pas ligne à ligne ; un
texte extrait serait faux sans qu'on le voie.

## Comment mesurer la suite

1. Même tâche, même parcours : écrire ou réviser un chapitre déjà mesuré.
2. Relever, par agent : appels, sortie, cache lu, cache écrit (le script ci-dessous).
3. Relever la qualité : `boussole validate .` sans problème, nombres recalculés justes, et ce
   que l'apprenant signale en étudiant (question ambiguë, piège trop facile, erreur).
4. Ne garder un changement que s'il coûte moins **sans** dégrader le point 3.

```python
import json, glob
for f in glob.glob('subagents/agent-*.jsonl'):
    seen, total = set(), [0, 0, 0, 0]
    for line in open(f):
        message = json.loads(line).get('message') or {}
        usage = message.get('usage') if isinstance(message, dict) else None
        if usage and message.get('id') not in seen:
            seen.add(message.get('id'))
            for i, key in enumerate(['output_tokens', 'input_tokens', 'cache_read_input_tokens', 'cache_creation_input_tokens']):
                total[i] += usage.get(key, 0)
    print(f, len(seen), total)
```
