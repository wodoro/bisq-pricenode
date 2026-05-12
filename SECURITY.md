# Security Policy

  ## Supported Versions

  `bisq-pricenode` is a Java/Spring service that fetches, transforms, and relays third-party market price data and Bitcoin mining fee estimates to Bisq clients. It is commonly deployed as a Tor hidden service.

  Security fixes are applied to the active `main` branch. This repository currently has no tagged releases.

  | Version / Branch | Supported |
  | --- | --- |
  | `main` | :white_check_mark: |
  | Deployed builds based on current `main` | :white_check_mark: |
  | Forks or modified deployments | :x: |
  | Old commits not matching current `main` | :x: |

  Operators should update deployed pricenodes promptly when security fixes are merged.

  ## Reporting a Vulnerability

  Please do **not** report security vulnerabilities through public GitHub issues, pull requests, Discussions, Matrix rooms, forums, or social media.

  Report suspected vulnerabilities privately through GitHub's **Report a vulnerability** flow on this repository's Security page. If that option is not available, open a minimal public issue asking maintainers to enable private security
  reporting, but do not include exploit details.

  Include as much detail as possible:

  - affected branch, commit, or deployed version
  - affected component, such as price providers, fee-rate providers, HTTP endpoints, Tor deployment scripts, Docker configuration, or dependency/build logic
  - whether the issue can manipulate market prices, mining fee estimates, endpoint responses, logs, deployment secrets, or operator infrastructure
  - reproduction steps, proof-of-concept input, logs, or screenshots
  - whether exploitation requires control of an upstream data provider, network position, pricenode operator access, or only public HTTP access
  - any evidence of active exploitation

  Bisq is an open-source project maintained by contributors. Response times may vary, but reports involving price manipulation, fee-estimate manipulation, service compromise, secret exposure, Tor hidden-service deanonymization, or supply-chain
  compromise are treated as urgent security issues and will be triaged as quickly as possible.

  For lower-severity issues, maintainers will respond when contributor capacity is available.

  If the report is accepted, maintainers may coordinate a fix privately, prepare operator guidance, and delay public disclosure until patched deployments can be updated. Please avoid public disclosure until maintainers confirm that disclosure is
  safe.

  Bisq does not currently guarantee a bug bounty. Security work may be eligible for Bisq DAO compensation if it qualifies under the DAO compensation process.
