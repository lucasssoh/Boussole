---
name: lexique-build
description: Build or extend the lexicon of a Boussole course (cursus/lexique.yaml) from the learner's documents, with definitions a newcomer understands. Use before writing sessions, and whenever a session needs a term the lexicon lacks.
---

# Build the lexicon

`cursus/lexique.yaml` holds the terms of the subject. In sessions, `:term[…]{id=…}` shows a term's
definition when the learner hovers it, and `:::recall` reminds it before it is needed.

## Which terms

- Every technical term the sources use and the learner must know to follow the course.
- Every acronym (`MTU`, `RIB`), with its expansion.
- Terms from earlier courses the sources take for granted.
- Not everyday words, and not notions a session explains at length: those get a notion, not an
  entry.

## Each entry

```yaml
- id: mtu
  term: MTU
  aliases: [Maximum Transmission Unit, taille maximale de trame]
  definition: >
    La taille maximale d'un paquet qu'un lien peut transporter d'un seul tenant, en octets
    (1 500 sur Ethernet). Un paquet plus grand doit être fragmenté.
  see: [fragmentation]
```

- `id`: lowercase letters, digits, `-`. Stable: sessions point to it.
- `term`: as the sources write it. `aliases`: other spellings, the expansion of an acronym, the
  English term if the course is in another language.
- `definition`: in the language of the course, **two or three sentences** for someone who has
  never seen the term: what it is, then one concrete figure or example. Define with simpler
  words than the term itself; never circular.
- `see`: IDs of related terms that exist in the lexicon.

Keep entries in the order the course introduces them. Never remove or rename an ID a session
uses: run `boussole validate .` afterwards, it reports any broken reference.
