# Workbench redesign

The interface now uses a neutral, responsive message/result workspace with encrypt/decrypt tabs, a local random-key generator, masked passphrases, a sample loader, output transfer, copy/save controls, and an expandable algorithm guide. Bootstrap JavaScript, icon fonts, gradient hero panels, and decorative feature statistics have been removed.

## Verified

- Java packaging and JavaScript syntax checks pass.
- Browser sample AES encryption → Reverse operation → decryption returns the original message.
- Phone-width layout stacks input/output and keeps the primary controls usable.
- Form fields have associated labels, operation feedback uses a live status region, and the operation tabs support arrow keys.
- Session history contains operation metadata, not message previews or keys.
- Key generation is local and the new UI does not call the password-in-query-string strength endpoint.

## Scope and remaining issues

This is an interface redesign, not a cryptographic-format migration. The existing Java API still uses unauthenticated CBC and has the validation, Unicode, and size-limit defects described in the dated audit. Their fixes remain separate work. The old strength endpoint also still exists, although the redesigned UI no longer uses it.

The page now explicitly describes server-side processing and its educational role instead of advertising absolute privacy. It does not claim browser-only encryption or a formal security audit.

Deployment configuration preserves the existing Embarko app identity and uses PORT on 0.0.0.0. The small package.json allows the deployment validator to recognize the archive; railpack.json selects Java/Maven as the actual build provider.
