# MCS-1 merge result

```text
Reviewed tip 221917f -> trial merge -> real merge aa90b73
                                      build PASS
                                      classes PASS
                                      origin/1.20.6 aa90b73
                                      ancestry PASS
```

Merged [MCS-1](http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-1) into `origin/1.20.6`.
The merge commit is `aa90b7362c435922ccb88de7335f81839a31f331`.
The reviewed tip is `221917f5fc4557ffb0662939ff250865c13dc554`.
Its branch is `codex/mcs-1-fix-textname-wrapper`.
The review report on `origin/review/mcs-1-fix-textname-wrapper` starts with `Merge.`.
The review report is `docs/reports/review-mcs-1-fix-textname-wrapper/report.md`.

## Merge evidence

Fetched before merging.
The reviewed branch still matched the required tip.
The main branch still matched `5a5b23fccaad2cfe1eb1ecbe32f3adeb620334c8`.
The clean merge branch started at that main commit.
Ran `git merge --no-commit --no-ff origin/codex/mcs-1-fix-textname-wrapper` as a trial.
Read the result and the staged change summary.
Aborted the trial.
Ran `git merge --no-ff origin/codex/mcs-1-fix-textname-wrapper` for the real merge.
Both merges completed without conflicts.
Conflict resolutions: none.
No review findings were changed.

The new-file check used `git diff --name-only --diff-filter=A` against the fetched main.
It found three new files totaling 15,109 bytes.
The largest new file was 7,018 bytes.
No file exceeded 10 MB.
The new-file total was below 50 MB.
The generated jar remains untracked under the ignored build directory.

## Gate evidence

Ran both commands in the worktree with Java 25.
Set `TMPDIR=/tmp/mc-server-spinner-upper-workers`.
Set `CUDA_VISIBLE_DEVICES` empty.
Ran the gates in the foreground without an output pipeline.
Read their output through completion.

Before the gate, `/proc/loadavg` read `16.81 29.38 42.87 28/9873 2979181`.
The process-state check counted zero tasks whose state began with `D`.
A three-second `/proc/stat` sample measured 62.3 percent CPU busy and 37.7 percent CPU idle.
These readings did not meet the deferral conditions.

| Command | Result | Task summary |
| --- | --- | --- |
| `./gradlew build` | `BUILD SUCCESSFUL in 12s` | `4 actionable tasks: 4 executed` |
| `./gradlew classes` | `BUILD SUCCESSFUL in 1s` | `2 actionable tasks: 2 up-to-date` |

Both commands exited zero.
The build reported `compileTestJava NO-SOURCE` and `test NO-SOURCE`.
There is no `src/test` directory.
Gradle emitted no test runner summary for files, passed, failed, skipped or todo counts.
Those counts are unavailable because no tests ran.
The step-zero dependency check found no `node_modules/typescript`.
There is no `node_modules` directory.
The project uses Java and Gradle compilation for its configured second gate.
The classes command executed Gradle and reported its compilation tasks.
It did not emit an installation hint.

The build reported deprecated APIs and Gradle features.
Neither gate reported an error.
The built jar is `build/libs/dcintegration-fabric-3.1.3-26.3.jar`.
Its SHA-256 is `a3d2a24d7a7cfa677eb05b1f01719c9a5ee7376570fef6b8af4aa17cae97f636`.

## Remote confirmation

Pushed the merge commit to the merge branch and confirmed it with `git ls-remote`.
Fetched again after both gates.
Main and the reviewed branch had not moved.
Pushed with `git push origin HEAD:1.20.6`.
The push advanced main from `5a5b23f` to `aa90b73` without force.
Fetched after the push.
Ran `git merge-base --is-ancestor` for the reviewed tip against `origin/1.20.6`.
Ran it again for the merge commit.
Both ancestry commands exited zero.
The fetched main tip was `aa90b7362c435922ccb88de7335f81839a31f331`.

No browser was started by this run.
No browser cleanup was needed.
No server or client was started.
No lab host was contacted.
Nothing was deployed, published or released.

## RECOMMENDATIONS

Authorize the runtime mixin check separately in staging or an approved rollout.
Repair the harness verdict reader so a first-line `Merge.` verdict is accepted.
