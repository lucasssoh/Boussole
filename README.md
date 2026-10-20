# Boussole

Turn your own course documents into a real course — lessons, quizzes, exercises checked as you
go, hints in tiers, videos and PDFs beside the text — and plan when to study it. Everything runs
on your machine.

> Status: design. Nothing is usable yet. See the [roadmap](ARCHITECTURE.md#11-roadmap).

## How it works

1. Put your documents (lecture notes, slides, past exams) in a subject folder.
2. `boussole init <folder>` installs instructions for AI agents next to them.
3. Ask your agent (Claude Code, Codex, Gemini CLI…) to build the course. It proposes a syllabus,
   you approve it, it writes the sessions in the [Boussole format](docs/FORMAT.md) and checks
   them with `boussole validate`.
4. `boussole open` and study in your browser.

Boussole itself never calls an AI and needs no account.

## Documentation

- [Course format](docs/FORMAT.md) — the contract between authors, agents and the application.
- [Architecture](ARCHITECTURE.md) — modules, design patterns, conventions.
- [Example subject](examples/reseaux).
- [Contributing](CONTRIBUTING.md) — commits, versions, changelog, releases.

## License

Apache License 2.0 with the Commons Clause: use it, change it, share it, contribute, in a
school, an association, a public body or a company, for free. What you may not do is sell
Boussole, or a service whose value comes mostly from it (a paid hosted version, for instance).
Keep the [`LICENSE`](LICENSE) and [`NOTICE`](NOTICE) files when you share it.

Boussole is therefore *source-available*, not *open source* in the OSI sense.

## Building

Requires Java 25 and Maven 3.9 (Node is downloaded by the build).

```
mvn verify
```
