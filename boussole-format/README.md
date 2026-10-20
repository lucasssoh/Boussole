# boussole-format

Reads a subject folder and checks it against the [course format](../docs/FORMAT.md).

**It knows how to** read `subject.yaml`, the syllabus, the lexicon and every session file into
an immutable model, and report each problem as `file:line: code message`.

**It does not** check answers, store progress, render Markdown or touch the network: those
belong to `boussole-core`, `boussole-adapters` and `web/`. Checking that videos exist needs a
`VideoCatalog`, given from outside.

```java
InspectionReport report = new SubjectInspector().inspect(Path.of("~/courses/RESEAUX"));
report.diagnostics().forEach(System.out::println);
report.course().ifPresent(course -> …);
```

| Package | Contents |
|---|---|
| `model` | the course, its parts, blocks and answers |
| `load` | the YAML files and session files |
| `parse` | the body of a session: raw tree, blocks, directives, answers |
| `validate` | the rules that need the whole course |
| `diagnostic` | codes, positions, diagnostics |
| `source` | the learner's documents (existence, page counts) |
| `yaml` | YAML with line numbers |

Tests: `ExampleSubjectTest` keeps [`examples/reseaux`](../examples/reseaux) valid; each rule has
its own test class; `SessionParsingTest` covers what is found while reading.
