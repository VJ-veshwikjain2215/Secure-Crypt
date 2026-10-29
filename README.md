# SecureCrypt

**An encryption workbench for learning by doing.**

[Open the workbench](https://veshwik-secure-crypt.embarko.app/) · [Review and improvement reports](improvements/README.md) · [Redesign notes](improvements/04-workbench-redesign.md)

Explore six text transformations in a focused message/result workspace. SecureCrypt is an educational Java and Spring Boot project by Veshwik Jain.

## The interface

- Encrypt/decrypt tabs with keyboard navigation and Ctrl/Command + Enter to run.
- One algorithm selector, masked passphrase input, and local random-key generation.
- Sample text, readable output, copy/download controls, and one-click output transfer.
- Operation history containing metadata only; text and keys are not saved in that list.
- Expandable algorithm explanations, light/dark themes, and responsive layouts.
- Explicit privacy information: operations send text and keys to the application server.

## Algorithms and limitations

AES-128/CBC, DES, Triple DES, Caesar, Vigenère, and repeating-key XOR are included for learning and comparison. Legacy/classical ciphers are not recommended for new secret-protection systems.

The current backend is an educational implementation. It does not authenticate CBC ciphertext, and the audit records Unicode and size-limit defects. The redesign does not fix those cryptographic/backend issues. Use sample data rather than real secrets, and read the [current-site audit](improvements/01-current-site-audit.md).

## Run locally

Install Java 21+ and Maven, then:

```sh
mvn spring-boot:run
```

Open `http://localhost:8080`. To package the app:

```sh
mvn package
java -jar target/SecureCrypt.jar
```

`PORT` overrides the server port. The Maven test phase currently reports no tests; the reproducible live audit is separate and documented under `improvements/evidence/`.

## Structure

- `src/main/java/` — REST controllers, cipher services, validation, and key utilities.
- `src/main/resources/static/` — HTML, CSS, JavaScript, and favicon.
- `improvements/` — audit evidence, comparison, roadmap, and redesign notes.
- `docs/` — original academic report and project documentation.
- `embarko.json` — identity of the existing Embarko app. Remove that identity before deploying a fork.
- `railpack.json` — Java build-provider configuration. The small `package.json` is a deployment-validator compatibility descriptor, not a Node application.

The frontend uses native HTML/CSS/JavaScript and Google Fonts. It has no Bootstrap or icon-font runtime dependency. The API uses Spring Boot 3.3 and JCA.
