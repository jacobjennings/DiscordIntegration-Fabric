Merge.

Reviewed `codex/mcs-6-di-upstream-26-3` at `a280a4b464dd16bbc6e6abb6e47de560b927eb46` against `origin/26.3` at `d83e2944c266b5adc9e4c863d147540069e80b9a`.
Completed wave: `mcs-6-di-upstream-26-3`.
Implementation: [MCS-6](http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-6).
Review: [MCS-26](http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-26).

The source change meets the implementation brief. No blocking finding was identified.
The worker report was found on the source commit at `docs/harness/reports/mcs-6-di-upstream-26-3/report.md`.

## Findings


- Suggestion. `docs/harness/reports/mcs-6-di-upstream-26-3/report.md:21`. Correct the old dependency values in the version table. The base uses Fabric API `0.152.2+26.2`, placeholder API `3.1.0-beta.1+26.2`, Styled Chat `2.13.0+26.2`, and Vanish `1.6.14+26.2`. The current table misstates the upgrade history.

- Suggestion. `docs/harness/reports/mcs-6-di-upstream-26-3/report.md:38`. Correct the code inventory. Seven `VanillaRegistries.createLookup()` calls changed across three files. The display record is `DisplayInfo`. Login routing has three join calls and five leave or timeout calls. Accurate names and counts make the report useful for later maintenance.

- Suggestion. `docs/harness/reports/mcs-6-di-upstream-26-3/report.md:56`. Correct the predicate explanation. Loader 0.19.5 accepts both `26.3` and `26.3.1` for `~26.3`. It rejects `26.2` and `26.4`. The implemented predicate is appropriate, but the report wrongly suggests patches are excluded.

## Acceptance evidence

`ForkChannels.java:16` keeps `DCINTEGRATION_LOGIN_CHANNEL`, the system property fallback, whitespace handling, and the configured server channel fallback. Its logic matches the fallback branch implementation.
All three join paths in `PlayerManagerMixin` and all five leave or timeout paths in `NetworkHandlerMixin` use it.

`AdvancementMixin.java:33` targets `markForVisibilityUpdate` inside `PlayerAdvancements.award`.
The 26.3 bytecode calls that method only when previous progress was incomplete and current progress is complete.
Partial criteria and already completed advancements do not reach the injection.
Upstream therefore covers the fork's completion rule without another anchor change.

`CommandSourceStackMixin.java:35` selects the exact Component constructor descriptor present in 26.3.
The shadow matches the private final `NamesProvider` field.
The default sender passes the shared display component, so the handler selects its split name provider.
The provider returns `DiscordIntegration` for the text name and `Discord Integration` for display.
The user sender keeps its own name.
Bytecode confirms `getTextName()` reads that provider and `withSource()` carries it into the copied stack.
The new mixin is registered and packaged in the built jar.

`fabric/src/main/resources/fabric.mod.json:24` chooses `~26.3`.
An isolated check using Loader 0.19.5 verified the four version results listed in the finding.
The jar requires Loader `>=0.19.5` and retains mod version `3.2.1`.
`NeoForge`, the other enabled loader, remains enabled. Quilt remains commented out as in the base.
The diff stays inside the brief's file list.
No license text, secrets, generated jars, or caches appear in the diff.

## Dependency verification

Each changed version was checked directly over HTTPS during this review.
Maven responses contained the requested POMs. Modrinth responses named the requested versions and 26.3 compatibility.

| Dependency | New version | Verification source |
|---|---|---|
| Minecraft | 26.3 | [Fabric game metadata](https://meta.fabricmc.net/v2/versions/game), stable entry |
| `Fabric Loader` | 0.19.5 | [Fabric repository POM](https://maven.fabricmc.net/net/fabricmc/fabric-loader/0.19.5/fabric-loader-0.19.5.pom) |
| Fabric API | 0.161.0+26.3 | [Fabric repository POM](https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/0.161.0+26.3/fabric-api-0.161.0+26.3.pom) |
| Placeholder API | 3.2.0+26.3 | [Nucleoid repository POM](https://maven.nucleoid.xyz/eu/pb4/placeholder-api/3.2.0+26.3/placeholder-api-3.2.0+26.3.pom) |
| `Styled Chat` | 2.14.0+26.3 | [Modrinth version API](https://api.modrinth.com/v2/project/styled-chat/version) |
| Vanish | 1.6.16+26.3 | [Modrinth version API](https://api.modrinth.com/v2/project/vanish/version) |
| `NeoForge` | 26.3.0.48-beta | [NeoForge repository POM](https://maven.neoforged.net/releases/net/neoforged/neoforge/26.3.0.48-beta/neoforge-26.3.0.48-beta.pom) |

## Mixin verification

Inspected 26.3 Minecraft bytecode with `javap -p -s` and `javap -p -c`.
Checked the Styled Chat integration against its resolved 2.14.0+26.3 jar.

| Mixin | Evidence |
|---|---|
| `AdvancementMixin` | `award(AdvancementHolder, String)`, player field, and guarded visibility update call match |
| `ChatMixin` | Private `broadcastChatMessage(PlayerChatMessage)` contains the exact PlayerList invocation descriptor |
| `CommandManagerMixin` | `performCommand(ParseResults, String)` matches |
| `CommandSourceStackMixin` | Exact seven argument constructor and `NamesProvider` shadow match |
| `MixinMinecraftServer` | Constructor and `stopServer()` exist, handlers capture only CallbackInfo |
| `NetworkHandlerMixin` | `onDisconnect(DisconnectionDetails)` and public ServerPlayer field match |
| `PlayerManagerMixin` | `canPlayerLogin(SocketAddress, NameAndId)` and `placeNewPlayer(Connection, ServerPlayer, CommonListenerCookie)` match |
| `ServerPlayerMixin` | `die(DamageSource)` matches |
| `StyledChatMixin` | `modifyForSending` and the redirected `formatMessage` descriptor match |

These checks establish target compatibility. They do not prove runtime mixin application.
A Minecraft server or client was never started. No lab host was contacted.

## Build verification

Built an isolated archive of the reviewed commit at `/tmp/review-mcs-6-a280a4b`.
Ran `./gradlew --no-daemon :fabric:classes :fabric:build`.
Read the output: `BUILD SUCCESSFUL in 7s`, with 16 actionable tasks and 16 executed.
The build included `:fabric:assemble`. Test tasks reported `NO-SOURCE`.
Gradle reported deprecation warnings for future Gradle 10 compatibility.
`git diff --check` passed.

Jar: `/tmp/review-mcs-6-a280a4b/fabric/build/libs/dcintegration-fabric-MC26.3-3.2.1.jar`.
SHA256 of this review build: `bc4f64c0a11a7364854da6c3b432d3a92f008844570c986fcffb89f8ebd33301`.
Inspected the packaged metadata. Mod id is `dcintegration-fabric`, version is `3.2.1`, and dependency predicates match the source.
The jar stays local. It was not installed.

## RECOMMENDATIONS

Correct the worker report's three factual discrepancies when convenient.
Proceed to the merge lane for [MCS-6](http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-6).
Leave runtime loading and Discord behavior checks to the separately authorized staging task.
This review does not merge or deploy.
