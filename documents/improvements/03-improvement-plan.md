# 3. Prioritized improvement plan

**Planning date:** 29 October 2026. This is a proposed backlog, not a list of implemented fixes. References such as SC-01 point to the [audit findings](01-current-site-audit.md).

## Product direction

Keep two explicit experiences:

1. **Learn:** historical ciphers, visible transformations, examples, and limitations.
2. **Protect text:** a small, authenticated-encryption flow with a clearly stated trust boundary.

Do not market the current CBC/password design as production-grade private encryption. Decide whether secrets should stay in the browser before expanding the product.

## Phase 1 — Correctness and honest privacy

| Work | Finding | Acceptance criteria |
| --- | --- | --- |
| Fix ciphertext length handling | SC-02 | Every accepted plaintext round-trips, including 20,000 ASCII characters and multibyte Unicode within the documented byte limit. UI and API use separate encoded/decoded limits. Oversized values fail before expensive processing. |
| Preserve unsupported scripts | SC-03 | Caesar/Vigenère transform only the documented alphabet; Hindi, Japanese, accented text, emoji, whitespace, and punctuation are either preserved or rejected explicitly. Add regression cases for the observed failures. |
| Remove passwords from query strings | SC-04 | Strength feedback runs locally; typing a key generates no request containing it. If a temporary server estimator remains, use a bounded POST body and no-store responses. Never put keys in share links or analytics. |
| Correct privacy wording | SC-05 | Show what is sent to the server before users submit. Replace 100% claims. Document session-history behavior and separate application storage from hosting/log retention. |
| Fix request-error contracts | SC-07 | Malformed JSON, bad parameter types, unsupported algorithms, and missing fields return structured 400-class responses. Unexpected failures use a generic 500 response with private diagnostics. |
| Mask and label controls | SC-12 | Secret fields are masked by default; reveal state is explicit; every field and icon-only button has a programmatic name; the mobile menu announces its state. |

**Exit gate:** the existing 28-assertion suite is preserved as a baseline; functional failures are addressed and security assertions are updated only when their replacement behavior exists. Add UI tests for max-length transfers, Unicode, and validation. A build with no tests is not a passing regression suite.

## Phase 2 — Authenticated modern encryption

### Design before implementation

- Use a maintained platform/library implementation of **AES-GCM**, not handwritten cryptography. Authenticated modes are preferred by [OWASP's cryptographic storage guidance](https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html).
- Generate a fresh salt for password derivation and a fresh nonce for each encryption. Salt uniqueness and nonce requirements are separate concerns.
- Benchmark and document the password-derivation work factor on representative devices. Store algorithm/KDF parameters in the output, not in unversioned hard-coded assumptions.
- Define a versioned envelope containing format version, cipher/KDF identity, work parameters, salt, nonce, ciphertext, and authentication tag. Treat header fields as authenticated associated data where appropriate.
- Keep an explicitly labeled **legacy CBC decrypt** path if existing outputs need compatibility. Never silently guess a format or reinterpret old data as the new scheme.

### Acceptance tests

A correct password round-trips. A wrong password fails without revealing internal causes. Flipping bytes in the nonce, ciphertext, tag, or authenticated metadata fails. Truncated, malformed, unknown-version, and oversized envelopes fail cleanly. Independent known-answer and interoperability tests cover the chosen representation. Include Unicode and empty/whitespace semantics in the documented contract.

### Architecture choice

**Preferred for a privacy-focused tool:** perform encryption and strength estimation in the browser with Web Crypto and keep the Java API as a separately labeled educational example. Browser-local code still needs secure delivery and does not promise absolute privacy.

**Alternative:** retain server processing, state that trust model plainly, avoid logging bodies/keys, use no-store responses, and audit infrastructure retention. Do not describe this alternative as equivalent to browser-local processing.

## Phase 3 — Reliability and abuse controls

- Bound generator length and key/input sizes; test min/max and rejected values (SC-09).
- Add request/body limits and an appropriate rate policy. Test at safe staging volumes, not against the live demo.
- Use `textContent` for dynamic toast messages; test hostile-looking text without executing it (SC-10).
- Decide whether third-party API callers are supported. Scope CORS accordingly, while treating rate limiting as an independent control (SC-11).
- Add no-store headers to sensitive endpoints. Review CSP against required assets; self-host stable UI dependencies where practical. Avoid breaking fonts/styles through an untested policy change (SC-13).
- Add dependency-update monitoring, a repeatable Java 21 build, Maven wrapper, and CI. Scan dependencies with a maintained scanner; no clean-vulnerability claim is supported by this audit.

**Exit gate:** unit and integration tests run in CI; validation error contracts and sensitive-data handling have regression coverage. Record a small, non-sensitive operational health check and test restart behavior.

## Phase 4 — Make it useful and memorable

| Improvement | Why it helps | Done when |
| --- | --- | --- |
| Send encrypted output to Decrypt | Removes repeated copying and mismatched algorithm state | One action transfers the result and algorithm; it does not silently persist or expose the key |
| Local examples and reset | Makes the first success easy | A clearly labeled sample can be loaded and fully cleared |
| Better strength guidance | Character counts alone overrate predictable passwords | Local estimator gives understandable feedback; common sample passwords are not advertised as strong |
| Explain mode and format | Helps users understand compatibility | UI shows text/byte limits, encoding, KDF, and format version without requiring expertise |
| Educational visualization | Creates a reason to share a learning project | Caesar/Vigenère show transformations and limitations with accessible text equivalents |
| Deliberate copy/export/history behavior | Reduces accidental disclosure | Copy/download actions are verified; session data can be cleared; secret-bearing exports require an explicit choice |
| Accessible layouts | Makes the existing tool usable by more visitors | Keyboard, screen reader, contrast, zoom, phone layouts, and reduced motion are checked with recorded evidence |

Borrow workflow clarity from [CyberChef](https://github.com/gchq/CyberChef), browser-local positioning from [cryptii](https://cryptii.com/), and teaching aids from [CrypTool-Online](https://www.cryptool.org/en/cto/). These are design references, not security endorsements.

## Release sequence and dependencies

1. **Correctness patch:** Phase 1, regression tests, accurate demo messaging.
2. **Crypto format release:** Phase 2, including legacy policy and compatibility tests. This is a data-format change requiring a migration explanation.
3. **Reliability release:** Phase 3 with CI and documented runtime behavior.
4. **Learning-studio release:** Phase 4, with real screenshots, example recipes, and measured usability improvements.

No calendar estimates are asserted before implementation scope and the browser-versus-server decision are agreed. Security and data-reversibility fixes should not wait behind visual redesign work.

## Success measures

- Zero known round-trip regressions across documented algorithms, alphabets, and limits.
- Every supported authenticated-format tamper case is rejected.
- No password appears in a request URL.
- Error-contract and meaningful regression tests pass in CI.
- A new visitor can encrypt and decrypt the example without manual algorithm mismatch.
- Public claims match implemented and verified behavior.

Stars and adoption are outcomes of usefulness, trust, documentation, and sharing; they are not guaranteed by adding badges or more algorithms.
