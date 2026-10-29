# Security policy

## Scope

Only the current `main` branch is maintained. SecureCrypt is an educational project; regression tests are not an independent security audit or certification.

Default AES-GCM and explicit legacy AES decryption use Web Crypto locally. Other educational algorithms send text and keys to the Java API. Legacy/classical algorithms should not be used to protect new secrets. Browser-local execution still trusts the delivered application and the user's device. See [API and format reference](docs/API.md) and [implementation report](improvements/05-correctness-and-crypto-update.md).

## Report a vulnerability privately

Use [GitHub's private vulnerability reporting form](https://github.com/VJ-veshwikjain2215/Secure-Crypt/security/advisories/new). Do not open public issues containing exploit details, credentials, private messages, or passwords.

Include the affected revision, a minimal reproduction using synthetic data, expected/actual behavior, and the potential impact. Test only resources you control; do not stress the shared live deployment, access other users' data, or attempt persistence.

Reports are reviewed on a best-effort basis. No response-time guarantee or paid bug-bounty program is offered. Coordinate public disclosure with the maintainer after a fix or mitigation is available.
