# Working on Boussole

For AI agents and for people changing the code of Boussole, by hand or with an agent. (The
`AGENTS.md` that `boussole init` installs next to a learner's documents is another file:
`boussole-cli/src/main/resources/boussole-agent/AGENTS.md`.)

## The rule

**Code, docs and diagrams move together, in the same commit.** Nobody should have to ask for the
docs to be updated: a change is finished when everything that describes it says the same thing.

`DocumentationSyncTest` (in `boussole-cli`) fails `mvn verify` when the code and the docs drift
apart: a block, a directive, an exercise type, a diagnostic code, a use case, a command or an
Angular component missing from its doc or diagram, a block the server sends that the interface
does not know, or a diagram changed but not rendered. What it cannot check is listed in each
recipe below: do it all the same.

## Read first

1. [`README.md`](README.md): what Boussole is.
2. [`ARCHITECTURE.md`](ARCHITECTURE.md): modules, patterns, conventions, data.
3. [`docs/FORMAT.md`](docs/FORMAT.md): the course format, the contract between agents and
   Boussole.
4. [`docs/diagrams/`](docs/diagrams/README.md): one class diagram per area.
5. [`CONTRIBUTING.md`](CONTRIBUTING.md): commits, versions, changelog, releases.

| Module | Holds | Depends on |
|---|---|---|
| `boussole-format` | the course model, the parser, the validator | nothing |
| `boussole-core` | use cases and ports (studying, rewards) | format |
| `boussole-adapters` | folders, SQLite, `config.yaml`, YouTube | core |
| `boussole-server` | HTTP API, views, the bundled guide | core, adapters |
| `boussole-cli` | `boussole` commands, the agent kit | server |
| `web/` | the Angular interface | the HTTP API |
| `skills/` | the playbooks installed for agents | `docs/FORMAT.md` |
| `guide/` | the built-in *Getting started* track, in the course format | `docs/FORMAT.md` |

## Recipes

Each recipe lists every place a change of that kind touches. Tick them all.

### A new kind of block (a directive)

1. `boussole-format`: the record in `model/block/` and its place in the `permits` of `Block`;
   `BlockWalker`; a `DirectiveParser` in `parse/directive/`, registered in
   `DirectiveParserRegistry.standard()`; its `DiagnosticCode`s if it can be written wrong.
2. Tests: `SessionParsingTest` (read, and each mistake with its message).
3. `boussole-server`: a `BlockView` record and its `@JsonSubTypes` name; `BlockViewMapper`.
4. `web/`: its type in `core/api.ts`; a component in `learn/blocks/`; its `@case` in
   `BlockListComponent`; styles in `styles.css`, light and dark; a Vitest for any logic.
5. `docs/FORMAT.md`: §5.2 or §5.5 (syntax and meaning), the table of places, §6 for its codes.
6. Diagrams: `11-model-blocks`, `20-parsing-structure`, `70-server`, `80-web`.
7. `skills/`: say when an agent should use it (`seance-write`, `exercise-author`), and how to fix
   its codes (`cursus-fix`).
8. An example of it in `examples/` or `guide/`, so that `mvn verify` validates a real use.
9. `CHANGELOG.md`.

### A new exercise type

`ExerciseType`, an `AnswerParser` registered in `AnswerParserRegistry`, a `ResponseChecker`
in the core, its `LearnerResponse`, the request in `boussole-server` (`AttemptRequest`) and in
`web/core/api.ts`, its input in `ExerciseBlockComponent` (with `ready()` and the stored answer
of `QuestionState`); `docs/FORMAT.md` §5.6; diagrams `12-model-answers`, `21-parsing-answers`,
`50-core-learn`, `80-web`; `skills/exercise-author`; an example; `CHANGELOG.md`.

### A new validation rule or diagnostic code

The rule in `validate/rule/` (or the report in the parser), its `DiagnosticCode` with severity,
a test with the exact message; the table of `docs/FORMAT.md` §6; the row of
`skills/cursus-fix`; diagram `31-validation`; `CHANGELOG.md`. A message says what to write
instead, not only what is wrong.

### A new use case, or a change in what the server answers

The use case in `boussole-core/learn` (one class, one public method), wired in
`LearningConfiguration`; a port method if it needs data, implemented in the in-memory test
repository and in `SqliteProgressRepository` (a new `V<n>__*.sql` migration if the schema
changes); the controller and its views; `web/core/api.ts` and `boussole-api.ts`; tests at each
layer (`LearningTest`, `SqliteProgressRepositoryTest`, `ServerApiTest`); `ARCHITECTURE.md` §6;
diagrams `50-core-learn`, `60-adapters`, `70-server`.

### A change the learner sees in the interface

- Every text in `web/src/app/i18n/en.ts` **and** `fr.ts`. Words are those of the product
  vocabulary below. No emoji: icons come from `IconComponent`.
- A new component: diagram `80-web`.
- Checked in a browser, light and dark, English and French, at phone width too.
- If it changes how studying works, the guide says so (`guide/en`, `guide/fr`).
- `CHANGELOG.md`, in the words of the interface.

### A new command or option

The picocli class in `boussole-cli`, its test, diagram `40-cli`, the README if a learner types
it, the guide if it is part of getting started. A new `config.yaml` key: `BoussoleConfig`,
`ARCHITECTURE.md` §8.

### A new playbook (skill)

`skills/<name>/SKILL.md`, the list in `AgentKit.SKILLS` (a test checks it), `skills/README.md`,
the routing rule in `boussole-cli/src/main/resources/boussole-agent/AGENTS.md` if agents
must reach it, `ARCHITECTURE.md` §7.

### The guide

`guide/en` and `guide/fr` say the same thing; both pass `boussole validate guide/<lang>`
(`GuideSubjectTest` checks it). New words of the product go into both lexicons.

## Writing for learners

The interface, the guide and the README speak to a learner, not to a reviewer of the design:
say what Boussole does and why it helps, starting from the learner's problem; leave design
decisions to `ARCHITECTURE.md`. Short sentences, lists and tables over long paragraphs, whole
words (no abbreviations).

| In the code | What the learner reads (en / fr) |
|---|---|
| subject | track / parcours |
| screen | step / étape |
| session path, the map | the Trail / le Tracé |
| hint | nudge / coup de pouce |
| first answer final | first try / du premier coup |
| confirm before a multiple MCQ | final check / dernière vérification |
| review | quick review / révision express |
| `free_navigation: false` | guided mode / mode guidé |
| skills | playbooks / fiches méthode |
| `boussole validate` | validator / validateur |
| `cursus/sources.yaml` | document tracking / suivi des documents |

## Checking your work

- `mvn verify`: Java tests, ArchUnit, `DocumentationSyncTest`, Vitest, the validator on
  `examples/` and `guide/`.
- `docs/diagrams/render.sh` after any `.puml` change (needs `plantuml` and `graphviz`).
- Browser checks against an isolated Boussole, never the learner's own: set `XDG_CONFIG_HOME`,
  `XDG_DATA_HOME` and `XDG_STATE_HOME` to a temporary folder, with its own `config.yaml` and
  `port`, before `bin/boussole open`.
- Never change a learner's documents or progress to test something.

Then commit as [`CONTRIBUTING.md`](CONTRIBUTING.md) says: one change per commit,
`Component : result`, and its `CHANGELOG.md` line.
