# boussole-cli

The `boussole` command (picocli), packaged with the server and the interface as one executable
jar, `target/boussole.jar`.

| Package | Commands |
|---|---|
| `cli` | `validate`, and what every command shares (`Launcher`, `ExitCode`) |
| `cli.init` | `init`: the subject files and the agent kit (skills and `FORMAT.md`, copied into the jar from the repository) |
| `cli.serve` | `open`, `serve`, `stop` |

`boussole open` recognises a running Boussole by `GET /api/server`; when none answers, it
starts `serve` as a process of its own, its output in `~/.local/state/boussole/server.log`. That
process runs a copy of the jar (`server.jar`, same folder): a running server loads its classes
from its jar as it goes, and would break if a new build replaced `boussole.jar`. `serve` writes
its process ID in `server.pid`, so that `boussole stop` can end a server that no longer answers.
