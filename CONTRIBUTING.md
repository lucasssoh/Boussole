# Contributing

How commits, versions, the changelog and releases work in Boussole. These rules apply to
everyone, people and AI agents alike. How the code is organised is in
[`ARCHITECTURE.md`](ARCHITECTURE.md).

## Commits

```
Component : what changes
```

- **In English**, with a space before the colon.
- **Component** is the part you touched:

  | Component | Covers |
  |---|---|
  | `Format` | `boussole-format`: model, parser, validator |
  | `Core` | `boussole-core`: the domain |
  | `Adapters` | `boussole-adapters`: files, SQLite, system timers, YouTube |
  | `Server` | `boussole-server`: HTTP API |
  | `CLI` | `boussole-cli` |
  | `Web` | `web/`: the Angular interface |
  | `Skills` | `skills/`: agent instructions |
  | `Docs` | `README.md`, `ARCHITECTURE.md`, `docs/` |
  | `Examples` | `examples/` |
  | `Build` | Maven, CI, release workflow |

  Narrow it down in brackets when that helps: `Web : [player] …`, `Core : [reminder] …`,
  `Format : [validator] …`.
- **The title says the result, not the action.** `hints revealed one at a time, the next one
  only after an attempt` tells more than `fix hints`.
- **The body is optional.** Use it to say *why*, when the title alone doesn't make it obvious.
- **One subject per commit.** A fix, a feature, or the docs on their own. Don't mix a fix with
  a new feature.
- **A change to the course format** updates, in the same commit, `docs/FORMAT.md`, the parser or
  validator, and `examples/`. The three never disagree.
- **Release commits** read `X.Y.Z : the changelog ready for the tag`.

Each `-m` is a paragraph: the first is the title, the next ones the body.

```
git commit -m "Format : [validator] a qcm with several ticked choices needs multiple=true" \
           -m "Without it, the interface showed radio buttons and the learner could never pick the right answer."
```

More examples:

```
Format : directives parsed with their nesting, the outer container one colon longer
Web : [player] the video beside the text starts at the notion's segment
Adapters : system timers rewritten only for the reminders that changed
0.2.0 : the changelog ready for the tag
```

## Versions

Versions follow `MAJOR.MINOR.PATCH`.

| You did… | Bump | Example | Heading |
|---|---|---|---|
| A bug fix | PATCH | 1.0.2 → 1.0.3 | the version alone |
| A new feature or a visible change | MINOR | 1.0.x → 1.1.0 | the date |
| A change that breaks existing setups (configuration, database without migration, a manual step needed) or a new **course format** version | MAJOR | 1.x → 2.0.0 | a code name |

- Until `1.0.0`, Boussole is not ready for daily use: a MINOR release may break things, and
  says so in its changelog.
- The application version and the course format version (`format:` in `subject.yaml`) are
  separate. A new format version is always a MAJOR release of the application.
- The version and the code name are the maintainer's call. Propose them, don't decide them.
- A minor release may contain fixes. A patch release never contains a new feature.
- The version of the Maven modules and of `web/package.json` is the release's version.
- **A pushed tag never moves.** If a release turns out broken, publish the next one.

## Changelog

[`CHANGELOG.md`](CHANGELOG.md) is filled in as you go, not at release time.

- Every change users will notice gets a line under `## Unreleased`, in one of `### Fixed`,
  `### Added`, `### Changed`, `### Removed`.
- **Written for users**, addressing them as "you": what they see or can now do. No file paths,
  no internals.
- One line per change, starting with **the part concerned in bold**:

  ```
  - **Session player:** hints now open one at a time, and the solution only after you have answered once.
  ```

- A change to the course format or to the agent skills is written for the people who write
  courses: what they can now write, or must write differently.
- Leave out what users don't notice: refactors, internal docs, build tweaks.
- Release headings: `## 2.0.0 « Name »` for a major release, `## 1.1.0 (October 1 2026)` for a
  minor one, `## 1.0.3` alone for a patch. A patch's section is just its list of fixes.

## Releasing

1. Check that everything since the last release is in `Unreleased`:

   ```
   git fetch --tags
   git log $(git describe --tags --abbrev=0)..HEAD --oneline
   ```

2. Rename `## Unreleased` to `## 1.1.0 (October 1 2026)`, `## 1.0.3` or `## 2.0.0 « Name »`.
3. Set the version everywhere:

   ```
   mvn versions:set -DnewVersion=1.1.0 -DgenerateBackupPoms=false
   npm --prefix web version 1.1.0 --no-git-tag-version
   ```

4. Commit, tag, push:

   ```
   git commit -am "1.1.0 : the changelog ready for the tag"
   git tag -a v1.1.0 -m "v1.1.0 (October 1 2026)"
   git push origin main v1.1.0
   ```

5. The tag starts the Release workflow: it runs `mvn verify`, builds `boussole.jar`, and
   publishes the GitHub release from the changelog section. Check that it's green in the
   Actions tab.

## Before committing

- `mvn verify` passes: tests, ArchUnit, and the validator on `examples/`.
- A change to the interface is checked in a browser, with a screenshot in light and dark mode,
  in English and French.
- New interface text exists in both languages.
- A class added, removed or renamed, or a relation between classes changed, is reflected in
  [`docs/diagrams/`](docs/diagrams/README.md), rendered with `docs/diagrams/render.sh`, in the
  same commit.

## AI agents

If you're an agent, everything above applies to you too. Also:

- Don't push, tag or publish a release unless you're asked to.
- **Every commit you write ends with your `Co-Authored-By:` line**, as its last paragraph. No
  exception.

  ```
  git commit -m "Format : the lexicon parsed, with aliases and links between terms" \
             -m "Co-Authored-By: <agent name> <its address>"
  ```
- Report what you verified and what you couldn't. Don't call something tested when it wasn't.
