# Repository structure

Application source, runtime assets, build manifests, tests and operational tooling live in code/. Project documentation lives in documents/, with project-plan/ reserved for planning. The root README and the license, changelog, contribution and security policies, code of conduct, editor settings and GitHub metadata remain at repository level.

## Path changes

Maven, Node tests, Docker and hosting commands run from code/. GitHub Actions and the Maven Dependabot directory now use code/.

The old docs/ contents are now directly in documents/. The former improvements/ tree is documents/improvements/; executable audit tooling moved to code/ops/ while historical evidence is preserved.

Existing tracked Python bytecode and the Civic SQLite database, where applicable, are removed from the published source tree and retained locally under ignored code/ paths. Moving a database into an ignored directory does not erase earlier Git history. No credentials, local databases or generated caches are added.

## Verification scope

Verification uses the existing Maven, browser-cryptography and interoperability CI suite. Application logic and ciphertext formats are unchanged.

This change updates repository layout and configuration. It does not issue a deployment command or change external service settings. Existing GitHub-integrated hosts can still run their own configured automation when source is pushed.

Checked locally on 2 November 2026: Local Maven verify passed all 20 Java tests and packaged the jar from code/. All four existing browser-cryptography tests passed. Source moves preserve the Java/browser implementations; documentation links and CI/cache paths resolve.
