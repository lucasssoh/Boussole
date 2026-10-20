# boussole-cli

The `boussole` command (picocli), packaged with the server and the interface as one executable
jar, `target/boussole.jar`.

| Package | Commands |
|---|---|
| `cli` | `validate`, and what every command shares (`Launcher`, `ExitCode`) |
| `cli.init` | `init`: the subject files and the agent kit (skills and `FORMAT.md`, copied into the jar from the repository) |
| `cli.serve` | `open`, `serve`, `stop` |

`boussole open` recognises a running Boussole by `GET /api/server`; when none answers, it
starts `java -jar boussole.jar serve` as a process of its own, its output in
`~/.local/state/boussole/server.log`.
