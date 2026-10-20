# boussole-server

The HTTP API (Spring Boot) and the interface it serves, on 127.0.0.1 only.

```java
try (RunningServer server = BoussoleServer.start(settings)) { … }   // port 0: any free port
```

| Package | Contents |
|---|---|
| (root) | `BoussoleServer`, `ServerSettings`, `LearningConfiguration`: the only place adapters meet use cases |
| `course`, `session`, `exercise`, `files` | one controller each, and the `…View` records they send |
| `block` | `BlockViewMapper`: blocks as the browser gets them, without answers, hints not yet revealed or solutions |
| `api` | errors, and `/api/server` for `boussole open` and `boussole stop` |
| `web` | `LocalRequestFilter` (localhost only, `X-Boussole` header on changes) and the serving of `web/` |

The build runs `npm ci`, the tests and the build of [`web/`](../web) and puts the interface in
this module's resources.

Tests: `ServerApiTest` starts a real server on the example subject and checks, among others,
that no answer leaves it before an attempt. `ModuleDependencyTest` (ArchUnit) keeps the
dependencies pointing down.
