# SecureCrypt code

The Spring Boot application, browser assets, tests, Maven manifest and hosting configuration live here. See the [project overview](../README.md) and [documentation index](../documents/README.md).

Run from this directory with Java 21+, Maven and Node 22+:

```sh
mvn verify
node --test tests/browser-crypto.test.mjs
mvn spring-boot:run
```

The packaged jar is target/SecureCrypt.jar. With the application running locally, run tests/interop.mjs using BASE_URL for that instance. Build Docker images with this directory as the context. Existing GitHub-linked hosting should use code/ as its application root; uploaded source archives must contain this directory's contents. The existing Embarko app identity is preserved. No deployment is performed by this reorganization.
