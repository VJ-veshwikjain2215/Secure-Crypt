# Secure-Crypt review and improvement reports

Reviewed **29 October 2026** against [the live Embarko app](https://veshwik-secure-crypt.embarko.app/) and source revision `a95eaac3fa9d8fc314598cac3635b6da33f7715b`.

1. **[Current-site audit](01-current-site-audit.md)** — working flows, reproduced defects, security observations, and limitations.
2. **[Comparable tools](02-comparable-tools.md)** — CyberChef, cryptii, and CrypTool-Online; their advantages and trade-offs.
3. **[Prioritized improvement plan](03-improvement-plan.md)** — proposed work, acceptance criteria, and a release sequence.

## Main conclusion

Secure-Crypt works as an educational encryption demo for typical short inputs. It should not yet be presented as a private, production-grade secret-encryption tool. Correctness, integrity protection, and accurate privacy messaging matter more than adding algorithms.

The bounded live API suite recorded **23 passing and 5 failing assertions out of 28**. Failures include desired security/quality properties, not just broken endpoints. Additional malformed-request and CORS checks are recorded separately. Maven's test phase succeeded but reported **no tests to run**.

## Evidence

- [Measured API results](evidence/api-results.json)
- [Reproducible PowerShell 7 API audit](evidence/run-api-audit.ps1)
- [Supplementary request results](evidence/supplementary-results.json)
- [Maven summary](evidence/maven-summary.txt)
- [Browser encryption screenshot](evidence/browser-encryption.png)
- [390 px phone screenshot](evidence/mobile-390.png)

The reports contain synthetic samples and test passwords only. This folder documents findings and recommendations; adding it does not implement the fixes or change the deployed site.
