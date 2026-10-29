# Contributing to SecureCrypt

SecureCrypt is an educational encryption workbench. Contributions should make its behavior easier to understand, more reliable, or more accessible. Search existing issues first; discuss new algorithms, dependencies, or format changes before starting substantial work.

## Local setup

1. Fork the repository and create a focused branch.
2. Install Java 21+ and Maven. Node 22+ is needed only for JavaScript tests.
3. Run `mvn spring-boot:run` and open `http://localhost:8080`.
4. Run `mvn verify` and `node --test tests/browser-crypto.test.mjs`.
5. With the packaged app running on port 8080, run the interoperability check:

```sh
BASE_URL=http://localhost:8080 node tests/interop.mjs
```

PowerShell: set `$env:BASE_URL='http://localhost:8080'`, then run `node tests/interop.mjs`.

## What a pull request needs

- Describe the concrete problem, resulting behavior, and checks performed.
- Keep changes focused. Add meaningful regression coverage for behavior changes.
- For interface changes, attach desktop and narrow-screen screenshots and check keyboard navigation, labels, focus, and both themes.
- For cryptography changes, document format compatibility and add known vectors, tampering/wrong-password cases, Unicode boundaries, and Java/browser interoperability checks. Do not introduce custom cryptographic primitives or silent legacy fallback.
- Preserve historical audit evidence. Record subsequent fixes in a new report or a clearly dated follow-up.

## Data, deployment, and licensing

Use synthetic messages and passphrases. Never include real secrets, personal encrypted documents, `.env` files, or deployment credentials in commits, screenshots, issues, or logs. Run load/abuse tests locally rather than against the shared live app.

A fork must remove the existing `appId` from `embarko.json` and use its own app name and credentials. The current identity belongs to the maintained live instance. The Dockerfile is retained for other hosting workflows; Embarko deployment archives must exclude it and use the Java provider configuration.

Source-code contributions use the MIT license. Only contribute assets you have the right to share and document their attribution. See [Security policy](SECURITY.md) for private vulnerability reports and [Code of conduct](CODE_OF_CONDUCT.md) for community expectations.
