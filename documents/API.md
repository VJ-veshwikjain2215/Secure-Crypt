# API and encrypted-message format

The default browser AES path does not call these endpoints. Direct API clients send their message and key to the server; use synthetic data for examples. All endpoints are same-origin, without user accounts or API authentication.

## Routes

| Method | Route | Purpose |
| --- | --- | --- |
| POST | `/api/encrypt` | Encrypt using a supported algorithm; `AES_LEGACY` is rejected. |
| POST | `/api/decrypt` | Decrypt with the selected algorithm. |
| GET | `/api/generate-key?length=16` | Generate a sample key; accepted length 8–128. The UI instead generates keys locally. |
| GET | `/api/key-strength` | Retired, always 410. Never send a password in a URL. |

POST bodies use JSON with `algorithm`, `text`, and `key`. Supported names are `AES`, `AES_LEGACY`, `CAESAR`, `VIGENERE`, `XOR`, `DES`, and `TRIPLE_DES`.

Example using synthetic data against a local instance:

```sh
curl http://localhost:8080/api/encrypt \
  -H 'Content-Type: application/json' \
  --data '{"algorithm":"CAESAR","text":"Hello!","key":"3"}'
```

A successful response contains `success: true`, `result: "Khoor!"`, `algorithm: "CAESAR"`, a human-readable `message`, and `processedAt` as Unix epoch milliseconds. Failures have `success: false` and a message; clients should inspect HTTP status and should not depend on exact error wording.

Caesar keys must fit a Java signed integer and are reduced modulo 26. Vigenère removes non-ASCII letters from its keyword and requires at least one remaining letter. Both ciphers preserve characters outside A–Z/a–z and are educational only. DES/Triple DES/XOR output Base64; they do not authenticate messages.

## AES SC1 envelope

```text
SC1.600000.<salt>.<nonce>.<ciphertext-and-tag>
```

- AES-128-GCM, 128-bit authentication tag.
- PBKDF2-HMAC-SHA256 at 600,000 iterations; fresh random 16-byte salt per encryption.
- Fresh random 12-byte nonce per encryption.
- Binary fields use canonical, unpadded Base64URL.
- UTF-8 bytes of the first four dot-separated fields form the additional authenticated data.
- Ciphertext includes the appended GCM tag.
- Passphrases use UTF-8 and are not normalized. Keep the exact original passphrase.
- Only the specified format version and work factor are accepted.

The Java and browser implementations share this format. Run `tests/interop.mjs` against a running local app to verify both directions. This specification describes the implementation, not an external security certification.

## Legacy AES migration

Choose `AES_LEGACY` explicitly to decrypt original Base64 AES-CBC outputs. The historical format contains a 16-byte IV followed by ciphertext, with fixed-salt PBKDF2-HMAC-SHA256 at 65,536 iterations. It has no authentication and cannot establish integrity, even when decryption succeeds. New encryption using this mode is rejected. Re-encrypt recovered content with modern AES-GCM when appropriate.

## Limits and errors

Plaintext encryption is limited to 20,000 UTF-16 code units, ciphertext input to 120,000, keys to 1,024, and algorithm names to 32. AES decryption also validates recovered plaintext size. Empty or blank text/keys are rejected.

| Status | Meaning |
| --- | --- |
| 200 | Successful operation. |
| 400 | Invalid input, malformed request, unsupported format, or failed decryption. |
| 410 | Retired password-strength route. |
| 413 | Declared request body exceeds 512,000 bytes. |
| 429 | Shared API budget or concurrency limit reached; inspect `Retry-After`. |
| 500 | Unexpected server failure; details are not returned. |

API responses use `Cache-Control: no-store`. Each process allows two concurrent API requests and 120 requests per rolling minute across callers. These are shared resource guards, not per-user/distributed abuse protection. Chunked request bodies do not have a separate streaming byte cap. See [security policy](../SECURITY.md) for remaining trust boundaries.
