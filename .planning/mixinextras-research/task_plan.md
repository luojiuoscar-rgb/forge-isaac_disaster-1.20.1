# MixinExtras Research Plan

## Goal

Integrate MixinExtras 0.5.5 into this Forge 1.20.1 / Minecraft 1.20.1 project
as a Jar-in-Jar runtime and migrate the approved high-value Mixin call sites to
chainable MixinExtras injectors without changing gameplay behavior.

## Phases

- [completed] Inspect the current Mixin setup, build, mappings, and likely injection targets.
- [completed] Verify MixinExtras APIs, supported packaging, and Forge integration from primary sources.
- [completed] Compare the library's capabilities with this project's current pain points and risks.
- [completed] Write the Chinese research conclusion with concrete adoption guidance and verification boundaries.
- [completed] Add the ForgeGradle compile-only annotation processor and Jar-in-Jar runtime dependency.
- [completed] Migrate `LivingEntityMixin.travel` friction handling to `@WrapOperation` while preserving behavior.
- [completed] Run build, artifact, diff, test, and runtime startup verification.
- [completed] Migrate the approved renderer redirects and return-value injections to `@WrapOperation` and `@ModifyReturnValue`.
- [completed] Re-run compile, tests, packaging, and client/server Mixin transformation smoke tests.
- [completed] Review the current uncommitted code changes and resolve the reported scope/test concerns.

## Constraints

- Target: Forge 1.20.1, Minecraft 1.20.1, Java 17.
- Preserve the existing dirty worktree and do not revert unrelated changes.
- Distinguish verified source facts from inference or version-dependent behavior.

## Errors Encountered

| Error | Attempt | Resolution |
|---|---:|---|
| `python` is not recognized in PowerShell | 1 | Automatic planning session catch-up could not run; continue with manual planning files and record the limitation. |
| `curl -o NUL` created a workspace file named `NUL` | 1 | Confirmed the exact file was temporary and removed only that file; no other workspace files were targeted. |
| Default `JAVA_HOME` points to a missing JDK 21 path | 1 | Use the verified Microsoft JDK 17 path for Gradle commands. |
| Dedicated Server stopped before world startup because `run/eula.txt` is absent | 1 | Mixin and MixinExtras loading completed successfully; gameplay/server-world behavior remains unverified in this workspace. |
| Client task was still running after reaching resource and renderer initialization | 1 | Stopped only the identified client Java process; Gradle reported exit `-1` from that intentional stop. |
| OCR review CLI rejected the documented `--format` flag | 1 | Used the installed CLI's supported text output and completed the same preview/rule/diff review workflow. |
