# Sciences de l'apprentissage : ce qu'elles disent à Boussole

Synthèse de recherche demandée dans `DEV_FEEDBACK.md` (sections *À étudier* et *À approfondir*).
Elle précède toute modification des fiches méthode (`skills/`) : rien n'a été changé dans le
code ni dans les fiches.

Date : 23 octobre 2026. Lu avant d'écrire : `README.md`, `docs/FORMAT.md`, `ARCHITECTURE.md`
(§ joueur de séance, progression), `DEV_FEEDBACK.md`, et les fiches `cursus-plan`,
`seance-write`, `exercise-author`, `exam-build`, `learner-profile`.

## Comment lire ce document

- Chaque thème suit le même plan : **ce que dit la recherche**, **limites et débats**,
  **références**, **pour Boussole** (ce qui existe déjà, ce que cela suggère).
- **Vérification des références.** Chaque DOI cité a été résolu auprès de Crossref le
  23/10/2026 : auteur principal, année, titre et revue correspondent. Aucune référence n'est
  inventée.
- **Vérification des chiffres.** Les tailles d'effet sont marquées :
  - *(résumé)* : lues dans le résumé de l'article ;
  - *(secondaire)* : rapportées de façon concordante par des sources qui citent l'article, sans
    accès au texte original ;
  - *(non vérifié)* : chiffre de mémoire, à confirmer avant de le citer ailleurs.
- **Ordre de grandeur.** En éducation, d = 0,2 est un petit effet, 0,5 un effet moyen, 0,8 un
  grand effet. Les méta-analyses agrègent des études hétérogènes (souvent en laboratoire, sur
  des étudiants anglophones) : un effet moyen ne garantit rien pour un cours donné.
- **Problème / hypothèse / proposition.** Dans les sections « Pour Boussole » et dans la liste
  finale, un *problème* est un écart constaté entre les fiches et la recherche ; une
  *hypothèse* est une amélioration plausible dont l'intérêt pour Boussole reste à démontrer ;
  une *proposition* est un changement concret, à décider par le mainteneur.

## Ce que Boussole fait aujourd'hui, en bref

| Mécanisme | Où | État |
|---|---|---|
| Une notion = explication, **un** exemple résolu, puis exercices « À toi » du même type | `seance-write`, `P001`, `P002` | en place |
| Échauffement sur la séance **précédente** du même parcours | `seance-write`, `:::warmup` | en place |
| Point de contrôle et/ou pratique finale | `closing` de `learner.yaml`, `P006` | en place |
| Mini-quiz après chaque explication | `quick_check`, **désactivé par défaut** | option |
| Essayer avant l'exemple | `order: try-first`, défaut `example-first` | option |
| Coups de pouce en paliers (où regarder, comment commencer, presque fini) | `exercise-author`, `P004` | en place |
| Un retour sur chaque mauvais choix de QCM, une solution qui explique | `exercise-author`, `P010` | en place |
| Une seule réponse par question dans une leçon (`AnswerOnce`) | cœur | en place |
| Révisions express (questions reprises par `::ref`) et examens blancs | `exam-build` | en place |
| Correction immédiate (leçon, révision), différée (examen) | joueur de séance | en place |
| Prérequis déclarés au niveau de la séance, rappels `:::recall` | format, `cursus-plan` | en place |
| Planification, rappels | V2 | à venir |
| Répétition espacée | V3 | à venir |
| XP, niveau, série de jours | `ReadStudyStats` | en place |

---

## 1. Pratique de récupération (*retrieval practice*)

**Ce que dit la recherche.** Se tester sur un contenu fait mieux retenir que le relire : c'est
l'effet de test. Il est l'un des résultats les plus solides de la psychologie de
l'apprentissage.

- Méta-analyse de Rowland (2014) : g ≈ 0,50 en faveur du test contre la relecture *(secondaire)* ;
  l'effet est plus fort quand le test est suivi d'un retour (g ≈ 0,73 contre 0,39 sans retour,
  *(secondaire)*).
- Adesope et al. (2017) : le test est plus efficace que la relecture et que toutes les autres
  conditions de comparaison *(résumé)* ; g ≈ 0,61 *(secondaire)*.
- Agarwal et al. (2021), sur 50 expériences en classe réelle : 57 % des effets sont moyens ou
  grands, quels que soient le niveau, la discipline, le format et le délai *(résumé)*.
- Dunlosky et al. (2013) classent le test d'entraînement parmi les deux techniques d'« utilité
  élevée » (avec la pratique espacée).
- Même une tentative **ratée** aide, si la bonne réponse suit : effet de pré-test (Richland,
  Kornell et Kao, 2009 ; Kornell, Hays et Bjork, 2009).
- Une récupération **difficile mais réussie** profite plus qu'une récupération facile (Pyc et
  Rawson, 2009).

**Limites et débats.** Les effets les plus nets portent sur la mémorisation de faits ; ils sont
plus faibles et plus variables pour la compréhension et le transfert (voir §9). Sans retour, un
test peut ancrer une erreur (voir §11, QCM). Les études en classe viennent surtout de pays
occidentaux (6 % seulement ailleurs, Agarwal et al., 2021).

**Références**

- Roediger, H. L., & Karpicke, J. D. (2006). Test-enhanced learning: Taking memory tests improves long-term retention. *Psychological Science*, 17(3). https://doi.org/10.1111/j.1467-9280.2006.01693.x
- Rowland, C. A. (2014). The effect of testing versus restudy on retention: A meta-analytic review of the testing effect. *Psychological Bulletin*, 140(6). https://doi.org/10.1037/a0037559
- Adesope, O. O., Trevisan, D. A., & Sundararajan, N. (2017). Rethinking the use of tests: A meta-analysis of practice testing. *Review of Educational Research*, 87(3). https://doi.org/10.3102/0034654316689306
- Agarwal, P. K., Nunes, L. D., & Blunt, J. R. (2021). Retrieval practice consistently benefits student learning: A systematic review of applied research in schools and classrooms. *Educational Psychology Review*, 33. https://doi.org/10.1007/s10648-021-09595-9
- Dunlosky, J., Rawson, K. A., Marsh, E. J., Nathan, M. J., & Willingham, D. T. (2013). Improving students' learning with effective learning techniques. *Psychological Science in the Public Interest*, 14(1). https://doi.org/10.1177/1529100612453266
- Richland, L. E., Kornell, N., & Kao, L. S. (2009). The pretesting effect: Do unsuccessful retrieval attempts enhance learning? *Journal of Experimental Psychology: Applied*, 15(3). https://doi.org/10.1037/a0016496

**Pour Boussole.**

- *Déjà là* : c'est le cœur du contrat de séance. Chaque notion se termine par des exercices,
  la séance par un point de contrôle, l'échauffement reprend la séance d'avant, les révisions
  express reprennent les questions par `::ref` (`exam-build` le justifie : « retrieving what
  was learnt is what makes it stay »). La correction suit chaque réponse.
- *Problème* : le mini-quiz après l'explication (`quick_check`) est **désactivé par défaut** et
  présenté dans `learner-profile` comme une affaire de goût. La recherche plaide pour qu'une
  récupération courte soit la norme, pas l'option.
- *Problème* : les questions « À toi » viennent **juste après** l'exemple résolu et sont « du
  même type ». La récupération est alors presque immédiate et facile : utile pour s'entraîner,
  faible pour la mémoire à long terme. Ce qui fait retenir, c'est surtout l'échauffement, le
  point de contrôle et la révision.
- *Hypothèse* : une question de pré-test au début d'une notion (avant l'explication) aiderait,
  d'autant que l'option `try-first` existe déjà sous une autre forme.

---

## 2. Pratique espacée (*spacing*)

**Ce que dit la recherche.** Répartir les révisions dans le temps fait mieux retenir que les
regrouper, à temps d'étude égal.

- Cepeda et al. (2006), synthèse de plus de 300 expériences : l'avantage de l'espacement est
  constant ; l'écart optimal croît avec le délai jusqu'à l'examen.
- Cepeda et al. (2008) : l'écart optimal est une fraction du délai de rétention, souvent
  résumée en « 10 à 20 % du délai » pour des délais de quelques semaines, une fraction plus
  petite pour des délais plus longs *(ordre de grandeur couramment cité, non vérifié dans le
  texte)*.
- Latimier, Peyre et Ramus (2021), 29 études : espacer les **tests** (et pas seulement les
  relectures) donne g = 0,74 contre des tests regroupés *(secondaire)* ; les intervalles
  **croissants** ne font pas mieux que des intervalles **réguliers** *(secondaire)*.
- Réapprentissage successif (Rawson et Dunlosky) : se tester jusqu'à réussir une fois, puis
  recommencer lors de plusieurs séances espacées. Effets importants sur les examens réels et la
  rétention à long terme (Rawson, Dunlosky et Sciartelli, 2013).

**Limites et débats.** L'écart optimal dépend de la date de l'examen, rarement connue avec
précision. Les algorithmes de répétition espacée (par exemple FSRS, Ye et al., 2022) sont
conçus pour des cartes indépendantes, pas pour des notions qui dépendent les unes des autres.
L'espacement est aussi **contre-intuitif** : les apprenants préfèrent le massé, qui donne une
impression de maîtrise (Kornell et Bjork, 2008 ; Bjork, Dunlosky et Kornell, 2013).

**Références**

- Cepeda, N. J., Pashler, H., Vul, E., Wixted, J. T., & Rohrer, D. (2006). Distributed practice in verbal recall tasks: A review and quantitative synthesis. *Psychological Bulletin*, 132(3). https://doi.org/10.1037/0033-2909.132.3.354
- Cepeda, N. J., Vul, E., Rohrer, D., Wixted, J. T., & Pashler, H. (2008). Spacing effects in learning: A temporal ridgeline of optimal retention. *Psychological Science*, 19(11). https://doi.org/10.1111/j.1467-9280.2008.02209.x
- Latimier, A., Peyre, H., & Ramus, F. (2021). A meta-analytic review of the benefit of spacing out retrieval practice episodes on retention. *Educational Psychology Review*, 33. https://doi.org/10.1007/s10648-020-09572-8
- Rawson, K. A., & Dunlosky, J. (2022). Successive relearning: An underexplored but potent technique for obtaining and maintaining knowledge. *Current Directions in Psychological Science*, 31(4). https://doi.org/10.1177/09637214221100484
- Rawson, K. A., Dunlosky, J., & Sciartelli, S. M. (2013). The power of successive relearning: Improving performance on course exams and long-term retention. *Educational Psychology Review*, 25. https://doi.org/10.1007/s10648-013-9240-4

**Pour Boussole.**

- *Déjà là* : l'échauffement (un écart d'une séance), la révision express en fin de chapitre
  « ou quelques jours après », la révision qu'on peut refaire, la planification (V2) et la
  répétition espacée (V3) prévues.
- *Problème* : une question n'est reprise **qu'une fois**, à la séance suivante, puis
  éventuellement à la révision du chapitre. Rien ne la fait revenir des semaines plus tard,
  quand l'oubli est le plus fort. Les examens blancs, eux, interdisent de reprendre les
  questions des leçons.
- *Problème* : dans une leçon, une question ratée n'est pas reposée (`AnswerOnce`). Le
  réapprentissage successif demande au contraire de reposer **les questions ratées** jusqu'à
  une réussite. L'agent qui écrit l'échauffement ne sait pas lesquelles ont été ratées.
- *Proposition (fiches)* : l'échauffement reprend la séance précédente **et** une ou deux
  questions de séances plus anciennes (deux à cinq séances avant) ; une révision cumulative
  s'ajoute à mi-parcours et avant l'examen, en plus des révisions de chapitre.
- *Proposition (interface, V3)* : une file « à revoir » alimentée par les questions ratées et
  par l'âge des réussites, avec des intervalles réguliers plutôt qu'un algorithme complexe : la
  méta-analyse de Latimier et al. ne montre pas d'avantage aux intervalles croissants.

---

## 3. Entrelacement (*interleaving*)

**Ce que dit la recherche.** Mélanger des problèmes de types différents, au lieu de les grouper
par type, aide à apprendre **quand** appliquer chaque méthode.

- Rohrer et Taylor (2007), puis Rohrer, Dedrick et Stershic (2015) en classe de
  mathématiques : les élèves entraînés sur des problèmes mélangés réussissent nettement mieux
  un test différé.
- Brunmair et Richter (2019), 59 études : g = 0,42 en moyenne *(secondaire, issu du résumé)* ;
  effet le plus fort pour des catégories visuelles proches (tableaux, g = 0,67, *(secondaire)*),
  petit pour les tâches mathématiques, nul ou négatif pour certains matériaux verbaux
  *(non vérifié pour ce dernier point)*. Le titre résume la conclusion : « la similarité
  compte » ; l'entrelacement aide surtout à **distinguer des choses qui se ressemblent**.
- Kornell et Bjork (2008) : les apprenants jugent le groupé plus efficace, alors que le mélangé
  l'est.

**Limites et débats.** Forte hétérogénéité. L'effet suppose que les types mélangés aient déjà
été appris : on n'entrelace pas des notions jamais vues. Il est en partie confondu avec
l'espacement.

**Références**

- Rohrer, D., & Taylor, K. (2007). The shuffling of mathematics problems improves learning. *Instructional Science*, 35. https://doi.org/10.1007/s11251-007-9015-8
- Rohrer, D., Dedrick, R. F., & Stershic, S. (2015). Interleaved practice improves mathematics learning. *Journal of Educational Psychology*, 107(3). https://doi.org/10.1037/edu0000001
- Brunmair, M., & Richter, T. (2019). Similarity matters: A meta-analysis of interleaved learning and its moderators. *Psychological Bulletin*, 145(11). https://doi.org/10.1037/bul0000209
- Kornell, N., & Bjork, R. A. (2008). Learning concepts and categories: Is spacing the "enemy of induction"? *Psychological Science*, 19(6). https://doi.org/10.1111/j.1467-9280.2008.02127.x

**Pour Boussole.**

- *Déjà là* : le point de contrôle couvre la séance entière ; l'examen blanc suit l'ordre du
  vrai examen, donc mélange.
- *Problème* : tout le reste est **groupé**. « À toi » : « du même type que l'exemple ». Révision
  express : « de facile à difficile », au sein d'un seul chapitre. L'apprenant sait toujours
  quelle méthode appliquer avant de lire l'énoncé ; il n'apprend jamais à la choisir.
- *Proposition (fiches)* : dans `exam-build`, une révision mélange les notions et les types de
  question (pas d'ordre par notion), et une partie au moins de ses questions vient de chapitres
  antérieurs ; dans `seance-write`, la pratique finale mélange les notions de la séance et, dès
  la deuxième séance d'un chapitre, celles des séances précédentes.
- *Proposition (fiches)* : écrire régulièrement des questions de **choix de méthode** (« Quelle
  notion s'applique à cette situation ? ») qui opposent les notions qui se ressemblent dans le
  cours (masque et préfixe, dual et primal…). C'est là que l'effet est le plus fort.

---

## 4. Charge cognitive

**Ce que dit la recherche.** La mémoire de travail est très limitée ; ce qui l'encombre sans
servir l'apprentissage (charge « extrinsèque ») le gêne. La charge « intrinsèque » dépend du
nombre d'éléments à tenir ensemble (*element interactivity*) et du niveau de l'apprenant.

- Sweller (1988) : résoudre des problèmes par essais et erreurs, en début d'apprentissage,
  mobilise la mémoire de travail au détriment de la construction de schémas.
- Sweller, van Merriënboer et Paas (2019) font le bilan de vingt ans d'effets : exemple
  résolu, attention partagée, redondance, modalité, retrait progressif, etc.
- Effet d'**inversion liée à l'expertise** (Kalyuga et al., 2003) : ce qui aide un débutant
  (exemple détaillé, texte qui double un schéma) devient inutile, voire nuisible, pour un
  apprenant avancé.
- Effet des détails séduisants (Rey, 2012) : anecdotes, images décoratives et digressions
  intéressantes mais hors sujet font baisser l'apprentissage *(direction de l'effet sûre,
  chiffre non vérifié)*.

**Limites et débats.** La charge cognitive se mesure mal (le plus souvent une auto-évaluation
sur une échelle). Plusieurs auteurs reprochent à la théorie d'expliquer après coup n'importe
quel résultat (de Jong, 2010). Ses recommandations les plus robustes sont les effets
individuels (exemples résolus, inversion liée à l'expertise), pas la théorie en bloc.

**Références**

- Sweller, J. (1988). Cognitive load during problem solving: Effects on learning. *Cognitive Science*, 12(2). https://doi.org/10.1207/s15516709cog1202_4
- Sweller, J., van Merriënboer, J. J. G., & Paas, F. (2019). Cognitive architecture and instructional design: 20 years later. *Educational Psychology Review*, 31. https://doi.org/10.1007/s10648-019-09465-5
- Kalyuga, S., Ayres, P., Chandler, P., & Sweller, J. (2003). The expertise reversal effect. *Educational Psychologist*, 38(1). https://doi.org/10.1207/S15326985EP3801_4
- de Jong, T. (2010). Cognitive load theory, educational research, and instructional design: Some food for thought. *Instructional Science*, 38. https://doi.org/10.1007/s11251-009-9110-0
- Rey, G. D. (2012). A review of research and a meta-analysis of the seductive detail effect. *Educational Research Review*, 7(3). https://doi.org/10.1016/j.edurev.2012.05.003

**Pour Boussole.**

- *Déjà là*, et cohérent avec la recherche : une étape à la fois dans le lecteur ; le rappel
  (`:::recall`) juste avant le besoin ; un graphe limité à 12 nœuds (`P011`) ; pas de formule à
  taper en LaTeX ; le PDF ouvert à côté, à la bonne page ; la durée calibrée à 15 minutes par
  notion ; `explanations: concise | detailed`.
- *Problème* : `background` (« je découvre », « j'ai vu les bases », « je révise l'examen ») ne
  change aujourd'hui que ce qu'il faut rappeler. L'inversion liée à l'expertise dit qu'il
  devrait aussi changer **la place des exemples** et **la quantité d'aide** (voir §5 et §6).
- *Proposition (fiches)* : dans `seance-write`, une règle explicite contre les détails
  séduisants : une anecdote, une vidéo ou une image n'entre que si elle explique la notion.

---

## 5. Étayage et retrait progressif (*scaffolding and fading*)

**Ce que dit la recherche.** L'étayage est une aide temporaire, ajustée au niveau de
l'apprenant, et **retirée** à mesure qu'il progresse (Wood, Bruner et Ross, 1976 ; van de Pol et
al., 2010 : contingence, retrait, transfert de responsabilité).

- Belland et al. (2017), 144 études en STEM : l'étayage informatisé a un effet constant de
  g = 0,46 *(résumé)* ; plus fort chez les adultes et sur la compréhension des principes
  *(résumé)*. Fait notable : le fait de retirer ou non l'aide ne change pas l'effet dans cette
  méta-analyse *(résumé)*.
- Tuteurs « pas à pas » : d ≈ 0,76 contre l'absence de tutorat, proche du tutorat humain
  (0,79) dans la revue de VanLehn (2011) *(secondaire)*. Ce qui compte est le grain de l'aide :
  au niveau de l'étape, pas seulement de la réponse finale.
- Coups de pouce à la demande : Aleven et al. (2016) montrent que les élèves les utilisent mal
  (trop vite, ou jamais), et que l'aide à la demande « aide, mais jusqu'à un certain point »
  *(résumé)*.

**Limites et débats.** Le retrait progressif est un principe consensuel mais peu testé de façon
isolée ; la méta-analyse de Belland ne le montre pas comme décisif. Le risque principal des
coups de pouce est l'abus : dévoiler tous les paliers jusqu'à la réponse.

**Références**

- Wood, D., Bruner, J. S., & Ross, G. (1976). The role of tutoring in problem solving. *Journal of Child Psychology and Psychiatry*, 17(2). https://doi.org/10.1111/j.1469-7610.1976.tb00381.x
- van de Pol, J., Volman, M., & Beishuizen, J. (2010). Scaffolding in teacher–student interaction: A decade of research. *Educational Psychology Review*, 22. https://doi.org/10.1007/s10648-010-9127-6
- Belland, B. R., Walker, A. E., Kim, N. J., & Lefler, M. (2017). Synthesizing results from empirical research on computer-based scaffolding in STEM education: A meta-analysis. *Review of Educational Research*, 87(2). https://doi.org/10.3102/0034654316670999
- VanLehn, K. (2011). The relative effectiveness of human tutoring, intelligent tutoring systems, and other tutoring systems. *Educational Psychologist*, 46(4). https://doi.org/10.1080/00461520.2011.611369
- Aleven, V., Roll, I., McLaren, B. M., & Koedinger, K. R. (2016). Help helps, but only so much: Research on help seeking with intelligent tutoring systems. *International Journal of Artificial Intelligence in Education*, 26. https://doi.org/10.1007/s40593-015-0089-1

**Pour Boussole.**

- *Déjà là* : coups de pouce en trois paliers, du vague au précis, dévoilés un par un avant de
  répondre, jamais la réponse finale ; aucun dans un examen blanc. C'est un bon étayage au grain
  de l'exercice.
- *Problème* : **rien ne retire l'aide** au fil du parcours. La séance 1 et la séance 12 ont la
  même structure, le même nombre de paliers, un exemple complet par notion.
- *Problème* : un `fill` à plusieurs trous ou un calcul en plusieurs étapes n'a qu'un verdict
  global ; les coups de pouce ne visent pas l'étape où l'apprenant bloque.
- *Proposition (fiches)* : une règle de retrait dans `seance-write` : au sein d'un chapitre, la
  première séance garde l'exemple complet ; les suivantes remplacent une partie des exemples par
  des **exemples à compléter** (`fill` sur les étapes de la méthode) ; la pratique finale et les
  révisions n'ont qu'un ou deux paliers de coup de pouce.
- *Hypothèse (interface)* : rendre visible, sans pénaliser, le nombre de coups de pouce
  utilisés, pour que l'apprenant voie l'aide diminuer. À tester : un compteur peut aussi
  décourager de demander de l'aide.

---

## 6. Exemples résolus et passage à la résolution autonome

**Ce que dit la recherche.** Pour un débutant, étudier des exemples résolus fait mieux apprendre
que résoudre seul les mêmes problèmes (Sweller et Cooper, 1985 ; Atkinson et al., 2000).

- Barbieri, Miller-Cotto, Clerjuste et Chawla (2023), mathématiques, 55 études : g = 0,48
  *(secondaire)*. Les exemples **corrects** seuls font mieux que les exemples erronés ou mixtes ;
  ajouter des consignes d'auto-explication a, dans cette méta-analyse, **réduit** l'effet
  *(secondaire)* (résultat à rapprocher du §7, et à interpréter avec prudence).
- Renkl et Atkinson (2003) : passer de l'exemple au problème par **retrait progressif** des
  étapes résolues (exemple complet, puis exemple dont la dernière étape manque, puis deux
  étapes…) ; van Merriënboer (1990) : les **problèmes à compléter** font mieux que les
  problèmes à produire en entier, en programmation.
- L'effet s'inverse avec l'expertise (Kalyuga et al., 2003) : pour qui maîtrise déjà, résoudre
  vaut mieux qu'étudier un exemple.
- **Débat ouvert : essayer d'abord ?** Kirschner, Sweller et Clark (2006) soutiennent que le
  guidage minimal ne marche pas pour les débutants. À l'inverse, Sinha et Kapur (2021),
  53 études : résoudre **avant** l'enseignement donne g = 0,36 en moyenne, jusqu'à 0,58 avec
  la méthode d'« échec productif » *(résumé)* ; mais l'effet s'inverse chez les jeunes élèves
  et pour les compétences générales *(résumé)*. Les deux camps s'accordent sur un point : un
  essai préalable n'aide que s'il est **suivi** d'une explication qui reprend les tentatives de
  l'apprenant.

**Références**

- Sweller, J., & Cooper, G. A. (1985). The use of worked examples as a substitute for problem solving in learning algebra. *Cognition and Instruction*, 2(1). https://doi.org/10.1207/s1532690xci0201_3
- Atkinson, R. K., Derry, S. J., Renkl, A., & Wortham, D. (2000). Learning from examples: Instructional principles from the worked examples research. *Review of Educational Research*, 70(2). https://doi.org/10.3102/00346543070002181
- Renkl, A., & Atkinson, R. K. (2003). Structuring the transition from example study to problem solving in cognitive skill acquisition: A cognitive load perspective. *Educational Psychologist*, 38(1). https://doi.org/10.1207/S15326985EP3801_3
- Barbieri, C. A., Miller-Cotto, D., Clerjuste, S. N., & Chawla, K. (2023). A meta-analysis of the worked examples effect on mathematics performance. *Educational Psychology Review*, 35. https://doi.org/10.1007/s10648-023-09745-1
- van Merriënboer, J. J. G. (1990). Strategies for programming instruction in high school: Program completion vs. program generation. *Journal of Educational Computing Research*, 6(3). https://doi.org/10.2190/4NK5-17L7-TWQV-1EHL
- Kirschner, P. A., Sweller, J., & Clark, R. E. (2006). Why minimal guidance during instruction does not work. *Educational Psychologist*, 41(2). https://doi.org/10.1207/s15326985ep4102_1
- Sinha, T., & Kapur, M. (2021). When problem solving followed by instruction works: Evidence for productive failure. *Review of Educational Research*, 91(5). https://doi.org/10.3102/00346543211019105

**Pour Boussole.**

- *Déjà là* : un exemple complet par notion, puis des exercices du même type : c'est la
  séquence « exemple puis problème » validée par la recherche pour les débutants. L'option
  `try-first` correspond au camp « essayer d'abord », et `seance-write` dit déjà que l'exemple
  qui suit « montre la méthode, pour que l'apprenant compare ». Le type `fill` à choix est un
  outil idéal pour les exemples à compléter, et `exercise-author` l'utilise déjà pour les
  preuves et les algorithmes.
- *Problème* : `order` est un **goût** demandé à l'apprenant, sans lien avec son niveau. La
  recherche le relie plutôt au niveau : exemple d'abord pour qui découvre, essai d'abord pour
  qui a vu les bases ou révise.
- *Problème* : « One example per notion, never more » est justifié par la durée, mais deux
  exemples **contrastés** aident davantage le transfert qu'un seul (voir §9).
- *Proposition (fiches)* : dans `learner-profile`, proposer par défaut l'ordre qui correspond au
  `background` (découverte : exemple d'abord ; bases ou révision : essai d'abord), en le
  disant à l'apprenant ; dans `seance-write`, la règle de retrait du §5.

---

## 7. Auto-explication

**Ce que dit la recherche.** Les apprenants qui s'expliquent à eux-mêmes chaque étape d'un
exemple (« pourquoi cette étape ? d'où vient ce résultat ? ») apprennent mieux (Chi et al.,
1989), et le leur demander améliore la compréhension (Chi et al., 1994).

- Bisra et al. (2018), 64 rapports : g = 0,55 *(secondaire, issu du résumé)*.
- Aleven et Koedinger (2002) : dans un tuteur de géométrie, faire **choisir dans un menu** le
  principe qui justifie chaque étape suffit à améliorer la compréhension. L'auto-explication n'a
  pas besoin d'un texte libre.

**Limites et débats.** L'auto-explication prend du temps ; sa qualité varie beaucoup d'un
apprenant à l'autre. Dans la méta-analyse de Barbieri et al. (2023), l'ajouter aux exemples
résolus en mathématiques a réduit l'effet : surcharge possible pour des débutants, ou temps
retiré à la pratique. Résultat à vérifier sur le type d'apprenants de Boussole.

**Références**

- Chi, M. T. H., Bassok, M., Lewis, M. W., Reimann, P., & Glaser, R. (1989). Self-explanations: How students study and use examples in learning to solve problems. *Cognitive Science*, 13(2). https://doi.org/10.1207/s15516709cog1302_1
- Chi, M. T. H., de Leeuw, N., Chiu, M.-H., & LaVancher, C. (1994). Eliciting self-explanations improves understanding. *Cognitive Science*, 18(3). https://doi.org/10.1207/s15516709cog1803_3
- Bisra, K., Liu, Q., Nesbit, J. C., Salimi, F., & Winne, P. H. (2018). Inducing self-explanation: A meta-analysis. *Educational Psychology Review*, 30. https://doi.org/10.1007/s10648-018-9434-x
- Aleven, V. A. W. M. M., & Koedinger, K. R. (2002). An effective metacognitive strategy: Learning by doing and explaining with a computer-based Cognitive Tutor. *Cognitive Science*, 26(2). https://doi.org/10.1207/s15516709cog2602_1

**Pour Boussole.**

- *Déjà là* : rien d'explicite. L'exemple est lu, pas interrogé ; le type `free` demande parfois
  une explication, sans vérification.
- *Proposition (fiches, sans changement de code)* : dans `exercise-author`, un motif
  « réponse + justification » : un `fill` à deux trous à choix, « La réponse est [[1]] parce
  que [[2]] », où les mauvaises justifications sont des raisonnements faux fréquents. C'est
  l'auto-explication par menu d'Aleven et Koedinger, vérifiable par Boussole.
- *Hypothèse* : une question « pourquoi cette étape ? » sur une étape clé de l'exemple résolu,
  posée juste après lui. À limiter à une par notion pour ne pas alourdir (résultat de Barbieri
  et al.).

---

## 8. Erreurs fréquentes et conceptions erronées

**Ce que dit la recherche.**

- Beaucoup d'erreurs sont **systématiques** : des procédures fausses mais cohérentes (« bugs »)
  qu'on peut cataloguer et reconnaître dans les réponses (Brown et Burton, 1978 ; Tatsuoka,
  1983).
- Une conception erronée cède quand l'apprenant en voit l'insuffisance et qu'une meilleure
  explication est intelligible et plausible (Posner et al., 1982). Les textes qui **énoncent
  l'idée fausse puis la réfutent** sont efficaces (Tippett, 2010).
- Des enseignants qui connaissent les conceptions erronées de leurs élèves les font davantage
  progresser que ceux qui ne connaissent que la bonne réponse (Sadler et al., 2013).
- Les erreurs commises avec **assurance** sont mieux corrigées que les autres, pourvu qu'un
  retour suive (effet d'hypercorrection : Butterfield et Metcalfe, 2001 ; Metcalfe, 2017).

**Limites et débats.** Les catalogues d'erreurs existent surtout pour les mathématiques et les
sciences au collège et au lycée ; dans les cours universitaires, il faut les reconstruire à
partir des corrigés et des annales. Un diagnostic d'erreur fait par une IA reste une hypothèse
(DEV_FEEDBACK le dit déjà).

**Références**

- Brown, J. S., & Burton, R. R. (1978). Diagnostic models for procedural bugs in basic mathematical skills. *Cognitive Science*, 2(2). https://doi.org/10.1207/s15516709cog0202_4
- Posner, G. J., Strike, K. A., Hewson, P. W., & Gertzog, W. A. (1982). Accommodation of a scientific conception: Toward a theory of conceptual change. *Science Education*, 66(2). https://doi.org/10.1002/sce.3730660207
- Tippett, C. D. (2010). Refutation text in science education: A review of two decades of research. *International Journal of Science and Mathematics Education*, 8. https://doi.org/10.1007/s10763-010-9203-x
- Sadler, P. M., Sonnert, G., Coyle, H. P., Cook-Smith, N., & Miller, J. L. (2013). The influence of teachers' knowledge on student learning in middle school physical science classrooms. *American Educational Research Journal*, 50(5). https://doi.org/10.3102/0002831213477680
- Metcalfe, J. (2017). Learning from errors. *Annual Review of Psychology*, 68. https://doi.org/10.1146/annurev-psych-010416-044022
- Butterfield, B., & Metcalfe, J. (2001). Errors committed with high confidence are hypercorrected. *Journal of Experimental Psychology: Learning, Memory, and Cognition*, 27(6). https://doi.org/10.1037/0278-7393.27.6.1491

**Pour Boussole.**

- *Déjà là* : chaque mauvais choix d'un QCM est « une vraie erreur d'apprenant », avec un retour
  qui dit pourquoi elle est fausse ; même règle pour les options d'un trou à choix.
- *Problème* : ces erreurs sont inventées question par question. Rien ne les recense au niveau
  du parcours, rien ne garantit qu'une même idée fausse soit visée plusieurs fois, ni qu'elle
  vienne des documents (corrigés, annales, remarques de l'enseignant).
- *Proposition (fiches)* : dans `cursus-plan`, relever pour chaque chapitre les erreurs que les
  documents signalent (« attention », « erreur fréquente », pièges des annales) et les écrire
  dans un fichier de travail (par exemple `cursus/erreurs.yaml`, à discuter : ce serait un
  nouveau fichier du format) ; `exercise-author` y puise ses distracteurs et donne à chaque
  erreur un identifiant.
- *Proposition (fiches)* : dans `seance-write`, quand une idée fausse est connue, l'énoncer puis
  la réfuter dans l'explication (texte de réfutation), par exemple dans un `:::warning`.
- *Hypothèse (format et interface)* : relier un mauvais choix à l'erreur qu'il représente
  (`{error=…}`) permettrait plus tard de dire « tu as fait trois fois la même confusion ».
  C'est la condition d'un vrai diagnostic (DEV_FEEDBACK, *Améliorer le diagnostic des
  erreurs*), mais aussi un changement de format à peser.

---

## 9. Rétroaction (*feedback*)

**Ce que dit la recherche.**

- Effet moyen : d = 0,48 sur 435 études (Wisniewski, Zierer et Hattie, 2020, *(résumé)*), mais
  très hétérogène : « le feedback ne peut pas être compris comme un traitement unique »
  *(résumé)*. Kluger et DeNisi (1996) : environ un tiers des effets sont **négatifs**, surtout
  quand le retour attire l'attention sur la personne plutôt que sur la tâche *(non vérifié pour
  la proportion exacte ; conclusion largement citée)*.
- Contenu : dans les environnements informatiques, un retour **élaboré** (une explication)
  donne 0,49, la seule bonne réponse 0,32, le seul « juste / faux » 0,05 ; l'élaboré l'emporte
  surtout pour les apprentissages de haut niveau (Van der Kleij et al., 2015, *(résumé)*).
- Moment : Van der Kleij et al. trouvent le retour immédiat plutôt meilleur pour les
  apprentissages simples et le différé pour les plus complexes, sans interaction significative
  *(résumé)*. En laboratoire, un retour différé de QCM améliore la rétention (Butler, Karpicke
  et Roediger, 2007, *(secondaire)*) ; en classe, l'immédiat est souvent meilleur (Kulik et
  Kulik, 1988). Débat non tranché ; dans les deux cas, **un** retour vaut beaucoup mieux
  qu'aucun.
- Recommandations de Shute (2008) : précis, centré sur la tâche, pas trop long, élaboré, sans
  comparaison avec les autres.

**Références**

- Hattie, J., & Timperley, H. (2007). The power of feedback. *Review of Educational Research*, 77(1). https://doi.org/10.3102/003465430298487
- Kluger, A. N., & DeNisi, A. (1996). The effects of feedback interventions on performance: A historical review, a meta-analysis, and a preliminary feedback intervention theory. *Psychological Bulletin*, 119(2). https://doi.org/10.1037/0033-2909.119.2.254
- Shute, V. J. (2008). Focus on formative feedback. *Review of Educational Research*, 78(1). https://doi.org/10.3102/0034654307313795
- Van der Kleij, F. M., Feskens, R. C. W., & Eggen, T. J. H. M. (2015). Effects of feedback in a computer-based learning environment on students' learning outcomes: A meta-analysis. *Review of Educational Research*, 85(4). https://doi.org/10.3102/0034654314564881
- Wisniewski, B., Zierer, K., & Hattie, J. (2020). The power of feedback revisited: A meta-analysis of educational feedback research. *Frontiers in Psychology*, 10. https://doi.org/10.3389/fpsyg.2019.03087
- Butler, A. C., Karpicke, J. D., & Roediger, H. L. (2007). The effect of type and timing of feedback on learning from multiple-choice tests. *Journal of Experimental Psychology: Applied*, 13(4). https://doi.org/10.1037/1076-898X.13.4.273
- Kulik, J. A., & Kulik, C.-L. C. (1988). Timing of feedback and verbal learning. *Review of Educational Research*, 58(1). https://doi.org/10.3102/00346543058001079

**Pour Boussole.**

- *Déjà là*, et fidèle à la recherche : verdict, retour propre au mauvais choix, solution qui
  explique **pourquoi** la bonne réponse est bonne, immédiatement dans une leçon et une
  révision, à la fin dans un examen blanc. C'est du retour élaboré, le type le plus efficace.
- *Problème* : le retour d'un mauvais choix et la solution sont écrits séparément ; rien ne dit
  à l'agent de relier la solution à l'erreur la plus probable (« si tu as trouvé 254, tu as
  oublié… »). Pour un `numeric` ou un `text`, aucun retour n'est propre à la mauvaise réponse
  donnée.
- *Problème (à surveiller)* : XP, niveau et série de jours attirent l'attention sur soi plutôt
  que sur la tâche, ce que Kluger et DeNisi associent aux retours qui nuisent. Rien ne montre
  que ce soit le cas dans Boussole ; DEV_FEEDBACK demande déjà d'éviter la ludification.
- *Proposition (fiches)* : dans `exercise-author`, la solution d'un calcul nomme les une ou deux
  erreurs les plus probables et ce qu'elles donnent ; un retour de mauvais choix dit **quoi
  vérifier** (pas seulement « Non »).
- *Hypothèse (format)* : des retours attachés à des mauvaises réponses attendues d'un
  `numeric` (« 254 => tu as compté l'adresse de diffusion »). Changement de format, à peser.

---

## 10. Transfert

**Ce que dit la recherche.** Appliquer un savoir à une situation nouvelle est difficile et
rarement spontané. Barnett et Ceci (2002) classent le transfert selon sa distance (domaine,
contexte, format, délai) : le transfert proche est courant, le lointain rare.

- Comparer **deux exemples** qui partagent la même structure sous des habillages différents
  aide à extraire le principe et à le réutiliser (Gick et Holyoak, 1983). Méta-analyse
  d'Alfieri, Nokes-Malach et Schunn (2013) : effet moyen favorable aux comparaisons de cas
  *(chiffre exact non vérifié)*.
- Le test d'entraînement aide aussi le transfert, moins que la mémoire, et surtout quand
  l'entraînement varie et que la réponse est suivie d'un retour élaboré (Pan et Rickard, 2018 ;
  *chiffre global non vérifié*).

**Limites et débats.** Le transfert lointain est l'un des résultats les plus décevants de la
recherche en éducation ; il ne faut pas promettre plus que du transfert proche à moyen.

**Références**

- Barnett, S. M., & Ceci, S. J. (2002). When and where do we apply what we learn? A taxonomy for far transfer. *Psychological Bulletin*, 128(4). https://doi.org/10.1037/0033-2909.128.4.612
- Gick, M. L., & Holyoak, K. J. (1983). Schema induction and analogical transfer. *Cognitive Psychology*, 15(1). https://doi.org/10.1016/0010-0285(83)90002-6
- Alfieri, L., Nokes-Malach, T. J., & Schunn, C. D. (2013). Learning through case comparisons: A meta-analytic review. *Educational Psychologist*, 48(2). https://doi.org/10.1080/00461520.2013.775712
- Pan, S. C., & Rickard, T. C. (2018). Transfer of test-enhanced learning: Meta-analytic review and synthesis. *Psychological Bulletin*, 144(7). https://doi.org/10.1037/bul0000151

**Pour Boussole.**

- *Déjà là* : la pratique finale et les examens blancs viennent des annales et des fiches de TD ;
  les révisions ajoutent des « variantes avec de nouvelles valeurs ».
- *Problème* : changer les **valeurs** n'est que du transfert très proche. « Du même type que
  l'exemple » et « même notation que l'exemple » renforcent l'habillage unique.
- *Proposition (fiches)* : dans `seance-write`, au moins une question « À toi » ou de pratique
  par notion change d'**habillage** (autre contexte, autre représentation, question posée à
  l'envers : trouver la donnée à partir du résultat) ; dans `exam-build`, une part des
  questions de révision sont des variantes de structure, pas seulement de valeurs.
- *Hypothèse* : autoriser un second exemple **contrasté** (même méthode, autre habillage, ou
  cas limite où la méthode échoue) quand la notion s'y prête, au lieu de la règle stricte
  `P002`. À tester contre le coût en durée.

---

## 11. Métacognition et calibration

**Ce que dit la recherche.**

- Les apprenants jugent mal ce qu'ils savent : relire et regrouper donnent une impression de
  maîtrise (*illusion de compétence*, Koriat et Bjork, 2005 ; Bjork, Dunlosky et Kornell, 2013).
- La **performance pendant l'apprentissage** n'est pas l'**apprentissage** : ce qui rend
  l'entraînement facile (massé, groupé, aidé) produit souvent moins de rétention (Soderstrom et
  Bjork, 2015).
- La surconfiance fait arrêter trop tôt et réussir moins bien (Dunlosky et Rawson, 2012).
- Les jugements d'apprentissage sont bien plus justes quand ils sont **différés** (Rhodes et
  Tauber, 2011, sur l'effet du jugement différé).
- Quand un apprenant note lui-même sa réponse libre, il se surestime ; lui donner la réponse
  modèle aide un peu, la découper en **idées à cocher** aide davantage (Rawson et Dunlosky,
  2007 ; Dunlosky et al., 2011).
- L'« effet Dunning-Kruger » tel qu'il est popularisé est en grande partie un artefact
  statistique (Gignac et Zajenkowski, 2020) : à ne pas invoquer.

**Références**

- Bjork, R. A., Dunlosky, J., & Kornell, N. (2013). Self-regulated learning: Beliefs, techniques, and illusions. *Annual Review of Psychology*, 64. https://doi.org/10.1146/annurev-psych-113011-143823
- Koriat, A., & Bjork, R. A. (2005). Illusions of competence in monitoring one's knowledge during study. *Journal of Experimental Psychology: Learning, Memory, and Cognition*, 31(2). https://doi.org/10.1037/0278-7393.31.2.187
- Soderstrom, N. C., & Bjork, R. A. (2015). Learning versus performance: An integrative review. *Perspectives on Psychological Science*, 10(2). https://doi.org/10.1177/1745691615569000
- Dunlosky, J., & Rawson, K. A. (2012). Overconfidence produces underachievement: Inaccurate self evaluations undermine students' learning and retention. *Learning and Instruction*, 22(4). https://doi.org/10.1016/j.learninstruc.2011.08.003
- Rhodes, M. G., & Tauber, S. K. (2011). The influence of delaying judgments of learning on metacognitive accuracy: A meta-analytic review. *Psychological Bulletin*, 137(1). https://doi.org/10.1037/a0021705
- Rawson, K. A., & Dunlosky, J. (2007). Improving students' self-evaluation of learning for key concepts in textbook materials. *European Journal of Cognitive Psychology*, 19(4-5). https://doi.org/10.1080/09541440701326022
- Dunlosky, J., Hartwig, M. K., Rawson, K. A., & Lipko, A. R. (2011). Improving college students' evaluation of text learning using idea-unit standards. *Quarterly Journal of Experimental Psychology*, 64(3). https://doi.org/10.1080/17470218.2010.502239
- Gignac, G. E., & Zajenkowski, M. (2020). The Dunning-Kruger effect is (mostly) a statistical artefact. *Intelligence*, 80. https://doi.org/10.1016/j.intell.2020.101449

**Pour Boussole.**

- *Déjà là* : l'exercice `free` se note soi-même (*compris / en partie / pas encore*) ; une
  révision ou un examen blanc se termine sur le nombre de bonnes réponses du premier coup ;
  chaque carte de séance montre combien de questions sont répondues et justes.
- *Problème* : ces compteurs mesurent la **performance immédiate** (réponse juste juste après
  l'exemple, coups de pouce compris). Les afficher comme une progression peut nourrir
  l'illusion de compétence que décrivent Soderstrom et Bjork. DEV_FEEDBACK le pressent
  (*Mesurer la compréhension plutôt que la seule progression*).
- *Problème* : la note de soi d'un `free` se fait contre une solution rédigée, sans critères
  découpés.
- *Proposition (fiches)* : dans `exercise-author`, la solution d'un `free` se termine par une
  liste courte des **éléments attendus** (trois à six), que l'apprenant coche mentalement avant
  de se noter.
- *Proposition (interface)* : distinguer « réussi en séance » de « retrouvé plus tard »
  (réussite dans un échauffement ou une révision, au moins un jour après) ; seul le second
  approche la maîtrise. Ne pas l'appeler maîtrise tant que sa validité n'a pas été évaluée.
- *Hypothèse (interface)* : une question d'assurance facultative avant de répondre (« sûr / pas
  sûr ») : elle entraîne la calibration, cible les erreurs commises avec assurance
  (hypercorrection, §8), et permet d'afficher un écart entre assurance et réussite. Coût :
  un clic de plus par question.

---

## 12. Évaluation et écriture des QCM

**Ce que dit la recherche.**

- **Nombre d'options.** Méta-analyse de Rodriguez (2005), 80 ans de recherche : **trois
  options** (une bonne, deux distracteurs) sont optimales ; elles permettent plus de questions
  dans le même temps, sans perte de qualité psychométrique *(résumé)*.
- **Qualité des distracteurs.** Dans des QCM à quatre options écrits par des enseignants, 13,8 %
  seulement des questions avaient trois distracteurs « fonctionnels » (choisis par au moins 5 %
  des candidats) ; en moyenne 1,54 par question ; environ la moitié des distracteurs ne
  fonctionnaient pas (Tarrant, Ware et Mohammed, 2009, *(secondaire)*). Ajouter des options
  ajoute surtout des options que personne ne choisit.
- **Règles d'écriture** (Haladyna, Downing et Rodriguez, 2002 ; Gierl et al., 2017) : options
  homogènes en longueur et en forme ; distracteurs plausibles tirés des erreurs réelles ; pas de
  « toutes ces réponses » ni de « aucune de ces réponses » ; pas d'indice grammatical ; énoncé
  qui pose la question entière ; éviter les négations.
- **QCM et apprentissage.** Un QCM peut ancrer les mauvaises options lues, d'autant plus qu'il y
  en a (Roediger et Marsh, 2005) ; un retour immédiat supprime cet effet négatif (Butler et
  Roediger, 2008). Des distracteurs **plausibles et liés** au contenu font même retenir
  l'information sur les mauvaises options (Little et al., 2012, *(résumé)*).
- **QCM à deux niveaux** (Treagust, 1988) : une réponse, puis la **raison** de cette réponse ;
  conçu pour révéler les conceptions erronées.

**Limites et débats.** Le « trois options » est établi pour l'évaluation (fiabilité, temps) ;
pour l'apprentissage, l'argument est indirect : ce qui compte est que chaque option fasse
réfléchir. Une bonne réponse au hasard reste possible (33 % à trois options), d'où l'intérêt de
mesurer sur plusieurs questions.

**Références**

- Haladyna, T. M., Downing, S. M., & Rodriguez, M. C. (2002). A review of multiple-choice item-writing guidelines for classroom assessment. *Applied Measurement in Education*, 15(3). https://doi.org/10.1207/S15324818AME1503_5
- Rodriguez, M. C. (2005). Three options are optimal for multiple-choice items: A meta-analysis of 80 years of research. *Educational Measurement: Issues and Practice*, 24(2). https://doi.org/10.1111/j.1745-3992.2005.00006.x
- Tarrant, M., Ware, J., & Mohammed, A. M. (2009). An assessment of functioning and non-functioning distractors in multiple-choice questions: A descriptive analysis. *BMC Medical Education*, 9. https://doi.org/10.1186/1472-6920-9-40
- Gierl, M. J., Bulut, O., Guo, Q., & Zhang, X. (2017). Developing, analyzing, and using distractors for multiple-choice tests in education: A comprehensive review. *Review of Educational Research*, 87(6). https://doi.org/10.3102/0034654317726529
- Roediger, H. L., & Marsh, E. J. (2005). The positive and negative consequences of multiple-choice testing. *Journal of Experimental Psychology: Learning, Memory, and Cognition*, 31(5). https://doi.org/10.1037/0278-7393.31.5.1155
- Butler, A. C., & Roediger, H. L. (2008). Feedback enhances the positive effects and reduces the negative effects of multiple-choice testing. *Memory & Cognition*, 36(3). https://doi.org/10.3758/MC.36.3.604
- Little, J. L., Bjork, E. L., Bjork, R. A., & Angello, G. (2012). Multiple-choice tests exonerated, at least of some charges. *Psychological Science*, 23(11). https://doi.org/10.1177/0956797612443370
- Treagust, D. F. (1988). Development and use of diagnostic tests to evaluate students' misconceptions in science. *International Journal of Science Education*, 10(2). https://doi.org/10.1080/0950069880100204

**Pour Boussole.**

- *Déjà là* : trois ou quatre choix, une seule bonne réponse sauf `multiple=true` annoncé,
  distracteurs tirés d'erreurs réelles, un retour sur chaque mauvais choix, correction immédiate
  (ce qui neutralise l'effet négatif décrit par Roediger et Marsh).
- *Sur la demande de DEV_FEEDBACK* (« augmenter le nombre de choix et rendre les réponses
  incorrectes plus subtiles ») : la recherche soutient la **seconde moitié** et pas la
  première. Plus d'options, ce sont surtout des options que personne ne choisit et du temps en
  plus ; des options plus plausibles, c'est ce qui fait réfléchir.
- *Problème probable (à mesurer)* : les modèles de langage écrivent souvent une bonne réponse
  plus longue, plus nuancée ou plus précise que les distracteurs. C'est l'indice le plus connu
  des guides d'écriture.
- *Proposition (fiches)* : dans `exercise-author`, garder trois ou quatre options et ajouter une
  courte liste de contrôle : options de même longueur et de même forme ; pas de « toutes / aucune
  de ces réponses » ; pas de négation non soulignée ; chaque distracteur nomme l'erreur qu'il
  représente (dans son retour) ; si on ne trouve pas d'erreur crédible pour une option,
  l'enlever plutôt que la remplir.
- *Proposition (fiches)* : pour éprouver la compréhension plutôt que la reconnaissance, alterner
  avec les formats où il faut construire (`numeric`, `fill`) et avec le QCM à deux niveaux du
  §7 (réponse + raison).
- *Hypothèse (validateur)* : un avertissement quand la bonne réponse est nettement la plus
  longue d'un QCM (par exemple 1,5 fois la plus longue des autres). Simple à vérifier,
  utile si le problème probable ci-dessus se confirme sur les parcours existants.

---

## 13. Graphes de prérequis et espaces de connaissances

**Ce que dit la recherche.**

- Hiérarchies d'apprentissage (Gagné, 1962) : une compétence s'appuie sur des compétences
  subordonnées qu'il faut maîtriser d'abord.
- **Espaces de connaissances** (Doignon et Falmagne, 1985 ; Falmagne et Doignon, 2011) : les
  états de connaissance possibles sont contraints par les prérequis ; on peut situer un
  apprenant avec peu de questions et lui proposer ce qu'il est **prêt** à apprendre (base du
  système ALEKS).
- **Composantes de connaissance** (Koedinger, Corbett et Perfetti, 2012) et suivi bayésien de
  la connaissance (Corbett et Anderson, 1994) : chaque question est reliée aux composantes
  qu'elle mobilise, ce qui permet d'estimer leur maîtrise et de localiser une difficulté.
- Pédagogie de la maîtrise : ne passer à la suite qu'après une réussite suffisante améliore les
  résultats, surtout pour les apprenants les plus faibles, mais prend plus de temps et, en
  autonomie, réduit le taux d'achèvement (Kulik, Kulik et Bangert-Drowns, 1990, *(résumé)*).

**Limites et débats.** Construire un graphe de prérequis fiable demande des experts et des
données ; un graphe produit par un modèle de langage à partir de PDF n'est qu'une hypothèse
plausible. Les modèles de suivi demandent beaucoup de réponses par composante, que peu de
parcours personnels fourniront. Un graphe n'a de valeur que s'il **change** quelque chose :
l'ordre des séances, un rappel, une recommandation.

**Références**

- Gagné, R. M. (1962). The acquisition of knowledge. *Psychological Review*, 69(4). https://doi.org/10.1037/h0042650
- Doignon, J.-P., & Falmagne, J.-C. (1985). Spaces for the assessment of knowledge. *International Journal of Man-Machine Studies*, 23(2). https://doi.org/10.1016/S0020-7373(85)80031-6
- Falmagne, J.-C., & Doignon, J.-P. (2011). *Learning Spaces*. Springer. https://doi.org/10.1007/978-3-642-01039-2
- Corbett, A. T., & Anderson, J. R. (1994). Knowledge tracing: Modeling the acquisition of procedural knowledge. *User Modeling and User-Adapted Interaction*, 4. https://doi.org/10.1007/BF01099821 (Crossref date le volume de 1995)
- Koedinger, K. R., Corbett, A. T., & Perfetti, C. (2012). The Knowledge-Learning-Instruction framework: Bridging the science-practice chasm to enhance robust student learning. *Cognitive Science*, 36(5). https://doi.org/10.1111/j.1551-6709.2012.01245.x
- Kulik, C.-L. C., Kulik, J. A., & Bangert-Drowns, R. L. (1990). Effectiveness of mastery learning programs: A meta-analysis. *Review of Educational Research*, 60(2). https://doi.org/10.3102/00346543060002265

**Pour Boussole.**

- *Déjà là* : `prerequisites` dans l'en-tête d'une séance (au niveau de la séance), `:::recall`
  juste avant le besoin, lexique, règle de `cursus-plan` : prévoir une séance pour un prérequis
  probablement absent, au chapitre où il sert d'abord.
- *Problème* : le grain est la **séance**. Une question n'est pas reliée aux notions qu'elle
  mobilise au-delà de sa place dans le fichier. On ne peut donc pas dire « tu bloques sur X
  parce que Y n'est pas acquis », ni construire le mode « Je suis perdu ».
- *Problème* : `cursus-plan` dit « les chapitres suivent l'ordre de l'enseignant ». C'est
  prudent (fidélité aux documents), mais rien ne vérifie que cet ordre respecte les
  dépendances.
- *Proposition (fiches, sans changement de format)* : dans `cursus-plan`, avant de découper,
  dresser la liste des notions et de leurs prérequis (notion → notions), la montrer dans la
  présentation du plan, et signaler toute notion utilisée avant d'être enseignée.
- *Hypothèse (format)* : des notions identifiées dans tout le parcours (elles ont déjà un ID
  `{#masque}`) et un attribut `requires=` sur une notion, que le validateur vérifierait
  (aucune notion utilisée avant sa séance). C'est le socle du diagnostic et du mode « Je suis
  perdu ». À n'engager que si une fonction de l'interface s'en sert.

---

## 14. Représentations multiples

**Ce que dit la recherche.**

- Plusieurs représentations (texte, schéma, formule, tableau, graphe) aident quand elles ont
  chacune une fonction : se compléter, se contraindre, ou construire une compréhension plus
  profonde par la traduction de l'une à l'autre (cadre DeFT, Ainsworth, 2006).
- Elles coûtent aussi : l'apprenant doit comprendre chaque représentation **et** les liens entre
  elles. Rau (2017) : l'effet dépend de l'aide donnée pour relier les représentations et de la
  compétence de l'apprenant à les lire.
- À distinguer nettement des **styles d'apprentissage** (« visuel », « auditif ») : adapter
  l'enseignement à un style déclaré n'a pas de preuve solide (Pashler et al., 2008).

**Références**

- Ainsworth, S. (2006). DeFT: A conceptual framework for considering learning with multiple representations. *Learning and Instruction*, 16(3). https://doi.org/10.1016/j.learninstruc.2006.03.001
- Rau, M. A. (2017). Conditions for the effectiveness of multiple visual representations in enhancing STEM learning. *Educational Psychology Review*, 29. https://doi.org/10.1007/s10648-016-9365-3
- Pashler, H., McDaniel, M., Rohrer, D., & Bjork, R. (2008). Learning styles: Concepts and evidence. *Psychological Science in the Public Interest*, 9(3). https://doi.org/10.1111/j.1539-6053.2009.01038.x

**Pour Boussole.**

- *Déjà là* : `:::graph` vérifié par le validateur, `mermaid`, images, vidéos, PDF à la page,
  maths KaTeX ; `seance-write` dit « dessiner quand une image aide » et « ne jamais inventer une
  figure que les sources ne soutiennent pas ». C'est la bonne prudence.
- *Problème* : `learner-profile` ouvre sur « People learn differently » et `videos: many`
  propose une vidéo par notion. Rien de cela n'est faux (ce sont des préférences de format et de
  temps, pas des styles d'apprentissage), mais la formulation peut laisser croire qu'adapter
  le format au goût améliore l'apprentissage.
- *Proposition (fiches)* : dans `seance-write`, quand une notion a deux représentations,
  **une question de traduction** de l'une à l'autre (`match` entre formules et graphes, `fill`
  qui lit une valeur sur un `:::graph`) : c'est la traduction qui produit l'effet, pas la
  juxtaposition.
- *Proposition (fiches)* : dans `learner-profile`, présenter les questions comme des préférences
  de rythme et de format, et dire que certaines valeurs par défaut (mini-quiz, révisions)
  viennent de la recherche (voir liste finale).

---

## 15. Synthèse : ce que la recherche confirme, ce qu'elle remet en cause

**Confirmé par la recherche** (garder) : se tester après chaque notion ; correction immédiate et
expliquée ; un retour sur chaque mauvais choix ; exemples résolus pour qui découvre ; coups de
pouce en paliers qui ne donnent pas la réponse ; une étape à la fois ; rappels juste avant le
besoin ; révisions qui reprennent les questions ; trois ou quatre options.

**Remis en cause ou incomplet** : récupération immédiate et facile plutôt qu'espacée ; pratique
groupée par type ; aucun retrait de l'aide ; ordre exemple / essai choisi au goût plutôt qu'au
niveau ; un seul habillage par notion ; compteurs de réussite qui mesurent la performance du
moment ; erreurs inventées question par question ; prérequis au grain de la séance.

---

## 16. Liste priorisée

Priorité selon trois critères : **solidité des preuves**, **écart avec ce que fait Boussole**,
**coût** (une fiche à réécrire coûte peu ; un changement de format ou d'interface touche le code,
la documentation, les diagrammes et le parcours `guide/course-format`, selon `AGENTS.md`).

### Problèmes constatés

| # | Problème | Où | Preuves |
|---|---|---|---|
| P1 | Une question n'est reprise qu'à la séance suivante, puis à la révision du chapitre ; rien plus tard | `seance-write`, `exam-build` | §1, §2 (Cepeda 2006 ; Latimier 2021 ; Rawson 2013) |
| P2 | Pratique et révisions groupées par notion et par type ; jamais de choix de méthode | `seance-write`, `exam-build` | §3 (Rohrer 2015 ; Brunmair 2019) |
| P3 | Le mini-quiz après chaque explication est désactivé par défaut et présenté comme un goût | `learner-profile` | §1 (Rowland 2014 ; Adesope 2017 ; Dunlosky 2013) |
| P4 | L'ordre exemple / essai dépend du goût, pas du niveau ; aucun retrait de l'aide au fil du parcours | `learner-profile`, `seance-write` | §4, §5, §6 (Kalyuga 2003 ; Renkl 2003 ; Sinha 2021) |
| P5 | Distracteurs inventés question par question, sans contrôle des indices de forme | `exercise-author` | §8, §12 (Haladyna 2002 ; Tarrant 2009 ; Sadler 2013) |
| P6 | Variantes de valeurs seulement : transfert très proche | `seance-write`, `exam-build` | §10 (Barnett 2002 ; Gick 1983) |
| P7 | Les compteurs affichent la performance du moment comme une progression | interface | §11 (Soderstrom 2015) |
| P8 | Une question ratée dans une leçon n'est jamais reposée exprès | cœur, `AnswerOnce` | §2 (Rawson 2022) |

### Propositions pour les fiches (sans code), par priorité

1. **Échauffements et révisions cumulatifs** (P1). `seance-write` : l'échauffement reprend la
   séance précédente et une ou deux questions de deux à cinq séances avant. `cursus-plan` : une
   révision cumulative à mi-parcours et avant chaque examen, en plus des révisions de chapitre.
   *Preuves fortes, coût faible.*
2. **Entrelacer révisions et pratique finale ; questions de choix de méthode** (P2).
   `exam-build` : révision mélangée (notions, types, une part de chapitres antérieurs), pas
   « facile à difficile » par notion. `seance-write` : pratique finale mélangée ; une question
   par chapitre qui oppose deux notions proches. *Preuves solides pour les catégories proches,
   coût faible.*
3. **Récupération par défaut** (P3). `learner-profile` : mettre « Oui » en premier, recommandé,
   pour le mini-quiz, et dire en une phrase pourquoi ; l'apprenant garde le choix. *Preuves
   fortes ; décision de produit à prendre par le mainteneur, puisque la fiche présente aujourd'hui
   « Non » comme défaut.*
4. **Écrire des distracteurs qui font réfléchir, sans en ajouter** (P5, réponse à la remarque
   [MCQ] de DEV_FEEDBACK). `exercise-author` : liste de contrôle (longueur et forme homogènes, pas
   de « toutes / aucune », chaque distracteur nomme son erreur, retirer une option sans erreur
   crédible) ; `cursus-plan` : relever les erreurs signalées par les documents et les annales.
   *Preuves solides, coût faible.*
5. **Réponse + justification** (§7, §12). `exercise-author` : motif de `fill` à deux trous à
   choix (« [[1]] parce que [[2]] »), au moins un par séance pour les notions conceptuelles.
   *Preuves moyennes à fortes (Aleven 2002 ; Bisra 2018 ; Treagust 1988), coût nul en code.*
6. **Retrait progressif et ordre selon le niveau** (P4). `learner-profile` : proposer l'ordre par
   défaut selon `background`. `seance-write` : exemples complets en début de chapitre, exemples
   à compléter (`fill`) ensuite, moins de paliers de coups de pouce en pratique finale et en
   révision. *Preuves solides pour le principe, plus faibles pour le dosage exact.*
7. **Changer d'habillage** (P6). `seance-write` : une question par notion dans un autre contexte
   ou à l'envers ; `exam-build` : variantes de structure. *Preuves moyennes, coût faible.*
8. **Grille d'autoévaluation des réponses libres** (§11). `exercise-author` : la solution d'un
   `free` finit par trois à six éléments attendus. *Preuves moyennes (Dunlosky 2011), coût nul.*
9. **Questions de traduction entre représentations** (§14) et **règle contre les détails
   séduisants** (§4). *Preuves moyennes, coût faible.*
10. **Liste des notions et de leurs prérequis dans le plan** (§13). `cursus-plan` : la dresser,
    la montrer, signaler les notions utilisées avant d'être enseignées. *Preuves théoriques
    solides, valeur pour Boussole à démontrer ; coût faible tant qu'elle reste dans la
    présentation du plan.*

### Hypothèses à tester avant d'engager du code

- **H1. File « à revoir »** (P1, P8) : les questions ratées et les réussites anciennes reviennent
  à intervalles réguliers (V3). Mesure : réussite aux échauffements et révisions, avec et sans.
- **H2. Deux indicateurs** (P7) : « réussi en séance » et « retrouvé plus tard » ; ne pas parler
  de maîtrise avant d'avoir vérifié que le second prédit la réussite aux examens blancs.
- **H3. Question d'assurance** avant de répondre (§11) : améliore-t-elle la calibration sans
  ralentir trop ?
- **H4. Erreurs identifiées** (`{error=…}`, §8) et **notions requises** (`requires=`, §13) dans
  le format : socle d'un diagnostic et du mode « Je suis perdu ». Coût élevé (format, validateur,
  serveur, interface, documentation) : à n'engager que si une fonction précise s'en sert.
- **H5. Second exemple contrasté** autorisé (§10) : gain de transfert contre coût en durée.
- **H6. Avertissement du validateur** quand la bonne réponse d'un QCM est nettement la plus
  longue (§12) : d'abord mesurer la fréquence du problème sur `examples/` et `guide/`.
- **H7. Effet de la ludification** (XP, niveau, série) sur l'attention à la tâche (§9) : à
  observer, pas à supprimer sans données.

### Comment vérifier que les changements aident

La section *À définir — évaluation de la qualité* de DEV_FEEDBACK demande des critères. Une
partie découle directement de ce document et peut se compter dans un parcours généré, sans
avis subjectif :

- part des questions d'échauffement et de révision qui viennent de plus d'une séance avant ;
- part des révisions dont l'ordre n'est pas groupé par notion ;
- longueur relative de la bonne réponse dans les QCM ;
- nombre de distracteurs dont le retour nomme une erreur précise ;
- présence d'exemples à compléter après la première séance d'un chapitre ;
- notions utilisées avant la séance qui les enseigne.

Ces mesures disent si une fiche est suivie, pas si l'apprenant apprend mieux. Pour cela, il
faudra comparer des réussites **différées** (échauffements, révisions, examens blancs), seule
mesure que la recherche considère comme un indice d'apprentissage.
