# DiscordIntegration-Fabric (Jake's fork)

Guidance for agents working in this fork. `CLAUDE.md` is a symlink to this file.

This is Jake's fork of ErdbeerbaerLP's DiscordIntegration Fabric port. It runs on
Jake's Minecraft server `stlmc`, installed by the `mc-server-spinner-upper` Ansible
role as a local jar. Agents may work in this fork (Jake, 2026-10-03). The upstream
"Do not engage" notice that used to be in this file was removed by that decision.
Keep this file and the `CLAUDE.md` symlink when merging upstream changes.

## Rules

- **Never touch upstream.** Never push to, or open issues, pull requests or
  discussions on, any upstream repository. Work only against `origin`.
- **Keep the login routing contract.** `ForkChannels` reads the
  `DCINTEGRATION_LOGIN_CHANNEL` env var to route join, leave and timeout messages
  (3.1.1). The name stays the same, and unset must still mean upstream behavior.
- **The working branch is `1.20.6`**, despite its name. Versions are in
  `gradle.properties`. Build with `./gradlew build`.
- **No AI attribution.** No AI trailers, and no vendor or model names in commit
  messages.

## Harness

Tasks for this fork are on the Huly board project `MCS`, with the other Minecraft
repositories. Its harness settings are in
`~/gh/mc-server-spinner-upper/harness/repos/DiscordIntegration-Fabric.toml`. Workers
read `docs/harness/worker-guide.md`. Reviews and merges read
`docs/harness/review-guide.md` and `docs/harness/merge-guide.md`.
