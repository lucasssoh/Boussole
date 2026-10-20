# web

The interface of Boussole: Angular, standalone components and signals. Its structure and
conventions are in [`ARCHITECTURE.md`](../ARCHITECTURE.md#5-interface-web).

The Maven build of `boussole-server` installs Node, runs `npm ci`, the tests and the build, so
you only need this to work on the interface:

```
boussole serve        # the API, on port 7420
npm start             # the interface on http://localhost:4200, /api forwarded to 7420
npm test -- --watch=false
```

The types of the API are in `src/app/core/api.ts`, next to the views of `boussole-server`:
change both together. Every text of the interface exists in `src/app/i18n/en.ts` and `fr.ts`.
