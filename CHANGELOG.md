# Changelog

This history describes shipped milestones; the project has not assigned semantic release tags to these updates.

## 29 October 2026 — Authenticated AES and correctness

- Browser-local AES-GCM, versioned SC1 format, and Java interoperability.
- Explicit decrypt-only support for earlier AES-CBC outputs.
- Unicode-preserving classical ciphers and separate plaintext/ciphertext limits.
- Improved API validation, retired password-strength URLs, same-origin policy, and resource guards.
- Automated Java, browser-crypto, and interoperability regression checks.

[Implementation and verification report](improvements/05-correctness-and-crypto-update.md) · [Merged change](https://github.com/VJ-veshwikjain2215/Secure-Crypt/pull/4)

## 29 October 2026 — Workbench redesign

- Focused message/result workspace, algorithm field guide, keyboard controls, local random-key generation, metadata-only history, and responsive themes.
- Replaced Bootstrap and icon-font dependencies with native HTML/CSS/JavaScript.

[Redesign report](improvements/04-workbench-redesign.md) · [Merged change](https://github.com/VJ-veshwikjain2215/Secure-Crypt/pull/2)

## 29 October 2026 — Review baseline

- Published current-site audit, comparable-tool review, and prioritized improvement plan.
- Retained original audit evidence as a historical baseline; subsequent fixes are recorded separately.

[Review index](improvements/README.md)
