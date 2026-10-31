# boussole-cli

The `boussole` command (picocli), packaged with the server and the interface as one executable
jar, `target/boussole.jar`.

| Package | Commands |
|---|---|
| `cli` | `validate`, and what every command shares (`Launcher`, `ExitCode`) |
| `cli.init` | `init`: the subject files and the agent kit (skills and `FORMAT.md`, copied into the jar from the repository) |
| `cli.serve` | `open`, `serve`, `stop`, and `ServerControl`, which the full screen shares |
| `cli.devices` | `devices` and its subcommands, through the running server's `/api/devices` |
| `cli.admin` | `boussole` alone (beta): the server, the devices and the tracks full screen (Lanterna); one class per screen |

`boussole open` recognises a running Boussole by `GET /api/server`; when none answers, it
starts `serve` as a process of its own, its output in `~/.local/state/boussole/server.log`. That
process runs a copy of the jar (`server.jar`, same folder): a running server loads its classes
from its jar as it goes, and would break if a new build replaced `boussole.jar`. `serve` writes
its process ID in `server.pid`, so that `boussole stop` can end a server that no longer answers.
For the same reason, `bin/boussole` runs a copy of `boussole.jar`, so that a build does not break
a command still running (the full screen, `serve`); it names the jar itself in the
`boussole.jar` property, which `Launcher` reads for the commands Boussole writes for agents.
