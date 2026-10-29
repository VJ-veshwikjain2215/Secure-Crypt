# 05 — Correctness, local AES, and API boundaries

Date: 29 October 2026. This follows the workbench redesign; reports 1–4 retain their historical observations.

## What changed

| Original issue | Shipped behavior |
| --- | --- |
| AES-CBC accepted tampered data without authentication | New AES uses authenticated GCM; wrong passwords and altered header/salt/nonce/ciphertext are rejected. |
| All operations sent text/passphrases to Java | Default AES and old AES decryption run in browser Web Crypto. No automatic server fallback. Other educational algorithms remain server processed, identified in the interface. |
| Fixed salt for default AES | Fresh random 16-byte salt and 12-byte nonce per encryption; PBKDF2-HMAC-SHA256, 600,000 iterations. |
| Old AES outputs would break after changing format | Explicit decrypt-only legacy CBC option preserves compatibility, without claiming integrity. |
| Caesar/Vigenère changed non-Latin letters incorrectly | Only ASCII Latin letters transform; other Unicode is preserved. |
| An encrypted 20,000-character input exceeded decryption limit | Separate 20,000-unit plaintext and 120,000-unit ciphertext limits; tested at the Unicode boundary. |
| Malformed JSON and invalid generator types returned 500 | Expected malformed requests return generic 400 messages; unexpected failures return generic 500 without input/exception details. |
| Password-strength GET put secrets in URLs and overstated strength | Endpoint retired with 410; heuristic removed. UI generates random sample keys locally. |
| Broad CORS and unbounded generation | Same-origin API; generator length 8–128; keys capped at 1,024 units. |
| No automated regression tests | Java, Web Crypto, API-contract, resource-budget, and cross-runtime tests added to CI. |

## Format and migration

`SC1.600000.<salt>.<nonce>.<ciphertext-plus-tag>` uses canonical unpadded Base64URL fields. Salt is 16 bytes, nonce 12 bytes, GCM tag 16 bytes. Key length is 128 bits. The first four dot-separated fields are UTF-8 additional authenticated data. Only this version and work factor are accepted, avoiding attacker-selected PBKDF2 cost. Password bytes use UTF-8; passwords are not normalized, so callers must retain the exact passphrase.

Choose **Old AES-CBC · decrypt only** for earlier outputs. Successfully decrypted legacy content can be encrypted again using modern AES. There is no silent format guessing, CBC fallback, or new CBC encryption. Other algorithms retain their original formats.

## Verification

- Java suite: 20 tests, including parameterized Unicode and maximum-length cases, independent legacy fixture, tampering, malformed requests, bounds, and shared API budget.
- Browser crypto suite: 4 tests, including 20,000-character Unicode, random output, modified envelope fields, wrong password, malformed encoding, and historical CBC fixture. The local-AES test replaces fetch with a throwing stub to detect module network calls.
- Interoperability runner: 4 round trips, browser encryption → Java decryption and Java encryption → browser decryption, using Unicode passwords/text and maximum-length text.
- Browser controls: sample encryption, reversal with 120,000-character ciphertext capacity, and successful original-message recovery.

Commands and prerequisites are in the repository README. Tests use synthetic messages and passwords. The original 23/28 audit result is a baseline, not a claim that this release still has those five failures or that every earlier assertion has been rerun unchanged.

## Resource controls and limits

API responses use `Cache-Control: no-store`; general responses include nosniff and no-referrer. There are two concurrent API slots and a shared 120-request rolling minute budget per process, with 429/Retry-After responses. Declared Content-Length above 512,000 bytes receives 413. These controls do not replace edge-level or distributed abuse protection; chunked bodies still need a streaming byte cap. Static files remain accessible when the API budget is exhausted.

Remaining work includes independent crypto review, file encryption, offline packaging, broader browser/accessibility tests, richer cipher explanations, a structured envelope inspector, and stronger hosting-level abuse protection. Legacy/classical algorithms remain for education. Browser-local processing still trusts the delivered application and device. No security certification is claimed.

## Design references

- [MDN Web Crypto encrypt](https://developer.mozilla.org/en-US/docs/Web/API/SubtleCrypto/encrypt): authenticated AES-GCM and secure-context requirements.
- [OWASP Cryptographic Storage](https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html): authenticated encryption and random IV/nonce guidance.
- [OWASP Password Storage](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html): PBKDF2-HMAC-SHA256 work-factor reference. That reference concerns password storage; the chosen envelope KDF remains an application design decision, not a compliance claim.
