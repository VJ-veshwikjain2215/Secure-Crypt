# 1. Current-site audit

**Date:** 29 October 2026 (Asia/Kolkata)
**Site:** https://veshwik-secure-crypt.embarko.app/
**Source:** `a95eaac3fa9d8fc314598cac3635b6da33f7715b`, plus the deployment-specific port/build configuration already used on Embarko.

## Verdict

The core short-text workflows work, including all six algorithms with ASCII input. The site is a useful academic demonstration, but it has confirmed data-correctness defects and lacks authenticated encryption. Its privacy presentation overstates the protection its server-based architecture provides.

Treat this as a bounded functional and code review, not a certification or a complete penetration test.

## Method and coverage

- Reviewed the live homepage, encryption result flow, and phone layout at 390 × 844.
- Submitted synthetic inputs through the live API. No real secrets or user records were used.
- Tested six algorithms with ASCII and mixed-language text; known classical-cipher examples; blank/invalid values; random IV behavior; a wrong AES password; an altered IV; the documented size boundary; and bounded key generation.
- Reviewed controllers, cipher services, key derivation, validation, exception mapping, CORS, HTML, and JavaScript.
- Ran `mvn -B test` locally. Compilation/test phase succeeded, but Maven reported **No tests to run**. This is not evidence of automated regression coverage.
- Did not perform load testing, deliberately large key generation, exploit chaining, a full browser/device matrix, dependency vulnerability scanning, or a formal accessibility/cryptographic audit. Clipboard/download completion, screen-reader navigation, and every legacy-cipher edge case remain unverified.

## What passed

The [API evidence](evidence/api-results.json) records **23/28 passing assertions**:

- All six algorithms returned the original ASCII sample after encryption and decryption.
- AES, DES, Triple DES, and XOR preserved the mixed Unicode sample.
- Vigenère matched `ATTACKATDAWN` + `LEMON` → `LXFOPVEFRNHR`.
- Caesar handled a negative shift correctly for ASCII.
- Blank text, blank AES key, unknown algorithm, malformed Base64, invalid Caesar key, and numeric-only Vigenère key returned HTTP 400.
- Repeated AES encryption produced different ciphertext, consistent with fresh IV generation.
- An incorrect AES password was rejected for the tested sample.
- Key generation returned the requested 16- and 32-character lengths.
- The browser displayed an encrypted result and a session-history entry. The phone layout stacked content vertically.

The five failing assertions concern Unicode preservation, tamper rejection, long-input reversibility, and password-strength quality. Additional checks below are separate from the 28-assertion count.

## Confirmed findings

### SC-01 · High · Encrypted data can be modified without rejection

**Evidence:** `aes-tamper-rejection`. Encrypting the synthetic sentence beginning `A long audit sample...`, flipping one bit in the first IV byte, and decrypting returned HTTP 200 with `success: true` and plaintext beginning `@ long audit sample...`.

**Cause:** [AESService](../src/main/java/com/securecrypt/service/AESService.java) uses CBC with padding and prepends an IV, but provides no authentication tag or MAC. DES and Triple DES use the same unauthenticated construction; this review directly demonstrated the issue only for AES.

**Impact:** A modified ciphertext can be accepted as a valid message. A successful wrong-password rejection does not establish integrity protection. This does not demonstrate recovery of somebody else's plaintext or password.

**Direction:** Introduce a versioned authenticated format using AES-GCM, preserve explicit legacy-decryption support, and test rejection of changes to every envelope component. OWASP prefers authenticated modes, or separate authentication when CBC is used. [OWASP guidance](https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html)

### SC-02 · High · Valid maximum-length encryption cannot be decrypted

**Evidence:** `aes-20000-ascii-roundtrip`. A 20,000-character ASCII message encrypted successfully, producing **26,712 Base64 characters**. Decrypting that exact result returned HTTP 400: input too large, maximum 20,000.

**Cause:** [ValidationUtil](../src/main/java/com/securecrypt/utility/ValidationUtil.java) applies the same limit to plaintext and expanded encoded ciphertext. Both browser textareas also have `maxlength="20000"`.

**Impact:** The application can create output its own API and input field cannot accept. Encoded Unicode and other algorithms need independent boundary coverage; this exact boundary failure was reproduced for AES.

**Direction:** Specify separate plaintext, byte-size, envelope, and decoded-ciphertext limits. Guarantee round trips for every accepted plaintext and make the UI use those limits.

### SC-03 · Medium · Caesar and Vigenère corrupt non-ASCII letters

**Evidence:** Mixed input `नमस्ते café 日本語 🔐` failed round trips for both algorithms. Caesar returned `नमस्ते cafg 日本語 🔐`; Vigenère also replaced several Hindi and Japanese letters.

**Cause:** [CaesarCipherService](../src/main/java/com/securecrypt/service/CaesarCipherService.java) and [VigenereCipherService](../src/main/java/com/securecrypt/service/VigenereCipherService.java) detect Unicode letters/case but calculate offsets from ASCII `A`/`a` modulo 26.

**Direction:** Define these ciphers as A–Z/a–z operations and preserve every other character, or explicitly reject unsupported alphabets before encryption. Do not silently transform unsupported scripts.

### SC-04 · High for privacy expectations · Passwords appear in request URLs

**Evidence:** [script.js](../src/main/resources/static/js/script.js) calls `/api/key-strength?key=...` after key input; the controller accepts a `key` query parameter. Encryption and decryption also send text and passwords to the server in JSON.

**Impact:** Passwords appear in network request URLs, which can be captured by access logs, diagnostic systems, and proxies. Actual server/provider log retention was not inspected; this review does not claim that keys were retained. Fetch requests are not assumed to become ordinary browser navigation history.

**Direction:** Prefer a local strength estimator. Remove secret-bearing URLs, document the server trust boundary, and apply `Cache-Control: no-store` to sensitive responses. HTTPS protects transport but does not hide inputs from the application server.

### SC-05 · Medium · Privacy copy does not describe the trust boundary clearly

The hero advertises **100% Client Privacy** and **0 Data Stored**. The backend processes plaintext/passwords. No application database or intentional persistence was found in the inspected flow, but infrastructure/browser retention was not audited.

**Direction:** Replace absolute claims with precise statements about what is sent, processed, retained, and visible in session history. If client-only processing is introduced, distinguish it from the educational server API.

### SC-06 · Medium · Predictable passwords can receive STRONG ratings

**Evidence:** `predictable-password-rating`: `Password123!` was rated `STRONG`.

**Cause:** [KeyGeneratorUtil](../src/main/java/com/securecrypt/utility/KeyGeneratorUtil.java) scores length and character classes without common-password or predictability analysis.

**Direction:** Use a maintained local estimator and honest feedback. A meter is not a guarantee of resistance to guessing.

### SC-07 · Medium · Malformed requests become HTTP 500 with internal detail

**Evidence:** [Supplementary results](evidence/supplementary-results.json). Malformed JSON and `/api/generate-key?length=abc` both returned 500 and included parser/type-conversion details.

**Cause:** [GlobalExceptionHandler](../src/main/java/com/securecrypt/exception/GlobalExceptionHandler.java) catches unhandled framework validation failures and concatenates exception messages into responses.

**Direction:** Map parsing/type errors to stable 400 responses; use generic 500 messages and private diagnostic logging.

## Code-review concerns requiring follow-up

These are not claims of demonstrated exploitation:

| ID | Priority | Observation and practical next step |
| --- | --- | --- |
| SC-08 | Medium | PBKDF2 uses a fixed application salt and 65,536 iterations. Introduce a random per-encryption salt in the versioned format and benchmark a documented work factor. Random salt and IV have different purposes. |
| SC-09 | Medium | `/generate-key` accepts any positive integer with no upper cap. Large values can consume memory/CPU; no stress request was made. Add bounded validation. No application-level rate limit was found. Provider protection was not assessed. |
| SC-10 | Medium | `showToast` inserts error messages with `innerHTML`; unsupported-algorithm messages can contain request text. Replace the dynamic message sink with `textContent`. No end-to-end remote XSS exploit was established. |
| SC-11 | Low/medium | API CORS permits `*`, confirmed with a synthetic Origin. This is a public API, so CORS alone is not an authentication bypass; decide whether cross-site API use is intended and bound resource consumption independently. |
| SC-12 | Medium | Password input defaults to `type="text"`. Several visible labels lack `for` associations; the phone navigation toggle has no accessible name in the inspected accessibility tree. Mask by default and audit keyboard/screen-reader labeling. |
| SC-13 | Low/medium | Key generation uses SecureRandom, but generated keys are returned without a measured Cache-Control header. The homepage has a CSP, though it broadly permits HTTPS sources and inline code. Scope policy changes around required dependencies rather than assuming no policy exists. |
| SC-14 | Medium | No test sources were found under `src/test`; Maven reports no tests. Add known vectors, boundaries, error contracts, and browser regression tests before implementing fixes. |

## UI/product assessment

The two-card layout, readable result boxes, and algorithm explanations make a good first demonstration. Copy/download controls are present, but their completion was not verified in this review. There is no verified automatic transfer from encryption output into decryption, no self-describing ciphertext format, and no guided distinction between educational ciphers and recommended secret protection.

The phone layout is usable in the observed light-theme screenshot. A complete contrast audit and cross-browser check are still needed; no WCAG conformance claim is made.

## Recommended disposition

Keep the project clearly positioned as educational while resolving SC-01, SC-02, SC-04, and misleading privacy claims. Fix Unicode preservation and request validation before broadening the feature set. See the [improvement plan](03-improvement-plan.md) for acceptance criteria and sequencing.
