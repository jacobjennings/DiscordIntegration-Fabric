Merge after restoring the default command sender's original text name.

Reviewed `codex/mcs-1-dcintegration-26-3` at `be7b9695d38f7ba52e8671dbe6c04a210188dfe7`.
The completed wave is `mcs-1-dcintegration-26-3`.
Implementation: [MCS-1](http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-1).
Review: [MCS-15](http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-15).
Base: `origin/1.20.6` at `5a5b23fccaad2cfe1eb1ecbe32f3adeb620334c8`.

I read the implementation brief and the complete source diff.
The worker report is present on the reviewed commit at
`docs/harness/reports/mcs-1-dcintegration-26-3/report.md`.
The path supplied in the review brief was incomplete.

## Findings

- **Blocking.** `src/main/java/de/erdbeerbaerlp/dcintegration/fabric/command/DCCommandSender.java:30` changes the default sender's text name from `DiscordIntegration` to `Discord Integration`. The brief requires preserving behavior. The new constructor derives both names from its component. Bytecode inspection confirms that Minecraft's player and IP ban commands use `getTextName()` for the ban source. Commands executed through `FabricServerInterface.runMCCommand()` therefore write a different source into ban records. Restore the original text name while preserving the existing display name. Preserve this distinction for command stacks derived from the sender as well.

- **Suggestion.** `docs/harness/reports/mcs-1-dcintegration-26-3/report.md:31` says `~26.3` also accepts later 26.x versions. Fabric Loader 0.19.5 compares both the major and minor components for this operator. The predicate accepts 26.3 and its patches. It rejects 26.4. Correct the report's explanation. Keep the implemented predicate because it covers the requested target.

## Verification

I built an isolated archive of the exact reviewed commit.
The build directory is `/tmp/dcintegration-review-be7b9695`.
I read the complete output of each Gradle invocation.

- `./gradlew classes`: BUILD SUCCESSFUL. Two actionable tasks, both executed.
- `./gradlew assemble`: BUILD SUCCESSFUL. Four actionable tasks, two executed and two up to date.
- `./gradlew build`: BUILD SUCCESSFUL. Four actionable tasks, all up to date. The test task reported NO-SOURCE. This is build validation, with no behavioral test coverage.

The jar is `/tmp/dcintegration-review-be7b9695/build/libs/dcintegration-fabric-3.1.3-26.3.jar`.
Its embedded metadata declares mod version 3.1.3, Minecraft `~26.3`,
Fabric Loader `>=0.19.5`, and Java `>=25`.
The bundled permission and placeholder mods also accept Minecraft 26.3.
The jar was not installed or uploaded.

Each changed dependency version exists at its published source.
These checks used fresh HTTP responses rather than Gradle cache entries.

| Version | Checked source |
| --- | --- |
| Minecraft 26.3 | [Fabric game metadata](https://meta.fabricmc.net/v2/versions/game) and the [Minecraft release manifest](https://piston-meta.mojang.com/v1/packages/4fe1aa1ef8da1cb95c5bad1fb98890ca56dd8ca3/26.3.json) |
| Fabric Loader 0.19.5 | [Published POM](https://maven.fabricmc.net/net/fabricmc/fabric-loader/0.19.5/fabric-loader-0.19.5.pom) |
| Loom 1.18.2 | [Published POM](https://maven.fabricmc.net/net/fabricmc/fabric-loom/1.18.2/fabric-loom-1.18.2.pom) |
| Fabric API 0.161.0+26.3 | [Published POM](https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/0.161.0+26.3/fabric-api-0.161.0+26.3.pom) |
| Placeholder API 3.2.0+26.3 | [Published POM](https://maven.nucleoid.xyz/eu/pb4/placeholder-api/3.2.0+26.3/placeholder-api-3.2.0+26.3.pom) |
| Styled Chat 2.14.0+26.3 | [Modrinth POM](https://api.modrinth.com/maven/maven/modrinth/styled-chat/2.14.0+26.3/styled-chat-2.14.0+26.3.pom) |
| Gradle 9.8.0 | [Published release list](https://services.gradle.org/versions/all) |

The Minecraft release manifest specifies Java 25.
[Loom's module metadata](https://maven.fabricmc.net/net/fabricmc/fabric-loom/1.18.2/fabric-loom-1.18.2.module)
requires Gradle plugin API 9.7.0.
That supports the reported need to update the wrapper outside the brief's file list.

I inspected the 26.3 class descriptors with `javap`.
The seven configured mixins retain their target methods.
The chat redirect has one matching invocation in `broadcastChatMessage`.
The advancement reward invocation remains guarded by the transition from incomplete to complete.
The changed display accessors retain the announcement flag, title, and description roles.
The command and login component conversions now use the running server's registry access.
These inspections do not prove successful runtime mixin application.
No server or client was started.
No lab host was contacted.

`ForkChannels.java` is unchanged.
`DCINTEGRATION_LOGIN_CHANNEL` still falls back to `advanced.serverChannelID` when unset.
The build does not read `update-checker.json`, so leaving it unchanged follows the brief.
No license text or headers changed.
No secrets, jars, or caches occur in the source diff.
The source diff passes the Git whitespace check.

## RECOMMENDATIONS

Restore the original command sender text name before merging
[MCS-1](http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-1).
Then repeat the compile and jar checks on the corrected commit.
Correct the worker report's predicate explanation.
Runtime validation remains outside this review's authorized scope.
