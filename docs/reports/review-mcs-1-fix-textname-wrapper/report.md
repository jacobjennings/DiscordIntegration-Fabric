Merge.

Reviewed `codex/mcs-1-fix-textname-wrapper` at `221917f5fc4557ffb0662939ff250865c13dc554`.
The completed wave is `mcs-1-fix-textname-wrapper`.
Implementation: [MCS-1](http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-1).
Prior review: [MCS-15](http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-15).
This review: [MCS-17](http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-17).

I read the repository guidance, implementation brief, and worker report from the reviewed commit.
The worker report is at `docs/harness/reports/mcs-1-fix-textname-wrapper/report.md`.
I inspected the diff against `origin/1.20.6` and isolated this fix against its starting commit `be7b9695d38f7ba52e8671dbe6c04a210188dfe7`.
The inherited port retains the scope of its prior review.

## Findings

No blocking findings.

- **Suggestion.** `docs/harness/reports/mcs-1-fix-textname-wrapper/report.md:23` says every `with*` copy preserves the provider. `withEntity` replaces it with `NamesProvider.FOR_ENTITY` when the entity changes. Narrow this claim to copies that preserve identity. The previous 26.1.2 implementation also substituted the entity's names, so this is a report correction rather than a regression.

## Acceptance evidence

`DCCommandSender.java` restores text name `DiscordIntegration` and keeps display name `Discord Integration`.
The new mixin targets the exact public Component constructor found in the 26.3 bytecode.
Its shadow matches the private final `namesProvider` field.
The constructor injection replaces that field only for the default sender's shared display component.
The user-based sender keeps its tag for both names.

I inspected `CommandSourceStack` with `javap -p -c` against the cached 26.3 Minecraft jar.
Neither public constructor accepts a separate text name.
The `Component` constructor delegates through `NamesProvider.constant`.
Both getters read the provider field.
`withSource`, `withPermission`, and other copies that preserve identity pass that provider to the private constructor.
The replacement therefore survives those copies without relying on subclass getter overrides.
`FabricServerInterface.runMCCommand()` still constructs the default sender.
The mixin is registered and present in the built jar.

The report's explanation of `~26.3` is corrected.
I checked the predicate through `Fabric Loader 0.19.5`'s public API.
It accepts 26.3 and 26.3.1 and rejects 26.4.
The chosen predicate remains `~26.3`, which covers the target and its patches.

The regenerated wrapper jar has SHA-256 `238e777fcddd7e34f9708186085def2abd6e08e658505b38718d79d74c21abd5`.
It matches the [official wrapper checksum](https://services.gradle.org/distributions/gradle-9.8.0-wrapper.jar.sha256).
The distribution URL still selects Gradle 9.8.0.
Its pinned SHA-256 matches the [official distribution checksum](https://services.gradle.org/distributions/gradle-9.8.0-bin.zip.sha256).
All four regenerated wrapper files are committed.
The extra mixin registration is necessary and explained in the worker report.
No license file or license header changed.
The wrapper jar is the source-managed artifact explicitly required by the brief.
No output jar, cache, or secret is added.
The login routing implementation is unchanged.

## Verification

I built an isolated archive of the exact reviewed commit with Java 25.
The build directory is `/tmp/dcintegration-review-221917f-gHsPto`.
I read the output of each invocation.

- `./gradlew classes`: BUILD SUCCESSFUL. Two actionable tasks, both executed.
- `./gradlew assemble`: BUILD SUCCESSFUL. Four actionable tasks, two executed and two up to date.
- `./gradlew build`: BUILD SUCCESSFUL. Four actionable tasks, all up to date. The test task reported NO-SOURCE.

The jar is `/tmp/dcintegration-review-221917f-gHsPto/build/libs/dcintegration-fabric-3.1.3-26.3.jar`.
Its SHA-256 is `a3d2a24d7a7cfa677eb05b1f01719c9a5ee7376570fef6b8af4aa17cae97f636`.
Its metadata declares version 3.1.3, Minecraft `~26.3`, `Fabric Loader >=0.19.5`, and Java `>=25`.
Build output reports deprecations but no errors.
There are no repository test sources.
These checks do not demonstrate runtime mixin application.

The fix introduces no dependency version changes beyond its starting branch.
I also checked the inherited versions against fresh published responses.
Each endpoint returned HTTP 200.

| Version | Checked source |
| --- | --- |
| `Minecraft 26.3` | [Release manifest](https://piston-meta.mojang.com/mc/game/version_manifest_v2.json) |
| `Fabric Loader 0.19.5` | [Published POM](https://maven.fabricmc.net/net/fabricmc/fabric-loader/0.19.5/fabric-loader-0.19.5.pom) |
| `Loom 1.18.2` | [Published POM](https://maven.fabricmc.net/net/fabricmc/fabric-loom/1.18.2/fabric-loom-1.18.2.pom) |
| `Fabric API 0.161.0+26.3` | [Published POM](https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/0.161.0+26.3/fabric-api-0.161.0+26.3.pom) |
| `Placeholder API 3.2.0+26.3` | [Published POM](https://maven.nucleoid.xyz/eu/pb4/placeholder-api/3.2.0+26.3/placeholder-api-3.2.0+26.3.pom) |
| `Styled Chat 2.14.0+26.3` | [Modrinth POM](https://api.modrinth.com/maven/maven/modrinth/styled-chat/2.14.0+26.3/styled-chat-2.14.0+26.3.pom) |

The whitespace check passes with `core.whitespace=cr-at-eol` for the generated Windows script.
No server or client was started.
No lab host was contacted.
No jar was installed or deployed.

## RECOMMENDATIONS

Accept the fix for [MCS-1](http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-1).
Narrow the worker report's copy-method claim as described above.
Any runtime validation needs a separately authorized task.
Keep that task within the fork and avoid upstream contact.
