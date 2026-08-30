# MixinExtras Research Progress

## 2026-08-30

- Read the applicable process and Minecraft Forge 1.20.1 guidance.
- Confirmed there is no existing MixinExtras-specific planning directory.
- Confirmed the root project is Forge 1.20.1 and already has Sponge Mixin configuration.
- Inspected the build and all seven current Mixin classes; identified renderer `@Redirect` and return-value post-processing as the most relevant comparison points.
- Retrieved the GitHub tag list and Maven Central search metadata. Found a version mismatch that requires tag-specific source and POM verification before making an adoption recommendation.
- Read the official `0.5.5` README and representative source annotations. Confirmed the ForgeGradle dependency split, bootstrap/plugin initialization path, chainable wrapper/return-value injectors, local capture semantics, and the Mixin 0.8.7+ limitation on injector ordering.
- Read the version-selection source and direct Maven metadata/POMs. Confirmed Forge 1.20.1's Mixin 0.8.5 selects the MixinExtras 0.8.4 compatibility layer, and corrected the apparent Maven version mismatch as a stale search-index result.
- Inspected the published Forge jar and ran Gradle dependency insight under verified JDK 17. The jar contains the Forge bootstrap/config resources, and Gradle resolved the project's existing Mixin 0.8.5 successfully; the default invalid `JAVA_HOME` was bypassed only for this command.
- Compared the current Mixin source against MixinExtras handler contracts. Recorded a concrete first candidate (`travel` friction redirect), two renderer wrapper candidates, and four return-value candidates; explicitly ruled out unrelated refactors driven only by library adoption.
- Verified the published Forge jar manifest, Jar-in-Jar metadata, and nested runtime markers.
- Checked the current source/build configuration for existing MixinExtras or JarJar references; none are present.
- Cleaned up one accidentally created temporary `NUL` file after the artifact inspection.
- Final recommendation: adopt `mixinextras-forge:0.5.5` with `common` compile-only/annotation-processor wiring, then trial only the travel friction `@WrapOperation`; do not claim runtime compatibility until the dependency is actually added and client/server startup is tested.
- Automatic `session-catchup.py` was attempted but could not run because `python` is not on PATH.
- Created this scoped planning directory; no source or build file was changed.

## 2026-08-30 Implementation

- Added `mixinextras_version=0.5.5` to `gradle.properties`.
- Added `mixinextras-common` as `compileOnly(annotationProcessor(...))` and `mixinextras-forge` as a ranged `jarJar` runtime dependency in `build.gradle`.
- Migrated `LivingEntityMixin.travel` friction handling from `@Redirect` to `@WrapOperation`; frozen entities still return `0.989F`, while normal entities call `original.call(state, level, pos, entity)`.
- Kept `mods.toml`, the existing Sponge Mixin 0.8.5 runtime, client-only Mixin configuration, and all second-phase candidates unchanged.
- Fresh `compileJava`, `jar`, and `jarJar` verification was completed under Microsoft JDK 17. The all jar contained Jar-in-Jar metadata, `mixinextras-forge-0.5.5.jar`, and its nested `MixinExtras-0.5.5.jar` runtime plus initialization resources.
- Remaining verification: `git diff --check`, `test`, optional `reobfJarJar`, and client/server startup if the environment permits.

## 2026-08-30 Verification Results

- `git diff --check`: passed with only normal LF-to-CRLF warnings from Git.
- `test --no-daemon`: passed with `BUILD SUCCESSFUL` and exit code 0.
- `reobfJarJar --no-daemon`: passed with `BUILD SUCCESSFUL` and exit code 0.
- Final `build/libs/isaac_disaster-0.1.0-all.jar` contains `META-INF/jarjar/metadata.json` and `META-INF/jarjar/mixinextras-forge-0.5.5.jar`. The nested Forge artifact contains `MixinExtrasConfigPlugin.class`, `mixinextras.init.mixins.json`, and `META-INF/jars/MixinExtras-0.5.5.jar`.
- `runServer --no-daemon`: reached Mixin preparation and then logged `MixinExtrasServiceImpl(version=0.5.5)` initialization without class-loading or injection errors; exited normally because the development server has no accepted `eula.txt`.
- `runClient --no-daemon`: reached the same MixinExtras initialization, mixed `LivingEntityMixin` and client mixins, initialized OpenGL/resources, and showed no MixinExtras/class-loading/injection error. The client was then intentionally stopped, so Gradle returned exit `-1`.
- The startup smoke tests used the dev/exploded classpath; a production all-jar launch with third-party mod combinations and interactive freeze movement was not performed. Existing asset and environment warnings are unrelated to this dependency change.

## 2026-08-30 Mixin Compatibility Optimization

- Migrated `LivingEntityRendererMixin` `setupRotations` and `scale` call-site handlers from `@Redirect` to `@WrapOperation`; each normal path calls the supplied `Operation<Void>` with the renderer receiver and original arguments.
- Removed the two renderer `@Shadow` methods because the operation now preserves the original call and allows wrapper chaining.
- Migrated `LivingEntityMixin.getScale`, `PlayerMixin.getDimensions`, `PlayerMixin.getStandingEyeHeight`, and `AttributeInstanceMixin.getValue` from cancellable return-value `@Inject` to `@ModifyReturnValue`.
- Preserved the existing formulas, owner-null fallback, client-only renderer placement, exact target descriptors, and all lifecycle/cancellation injections outside the approved scope.
- Fresh `compileJava`, `test`, `jar`, and `reobfJarJar` completed successfully under Microsoft JDK 17.
- Fresh server smoke test reached MixinExtras 0.5.5 initialization and prepared 18 mixins before the expected missing-EULA exit. Fresh client smoke test reached MixinExtras initialization, transformed `LivingEntity`, `Player`, `LivingEntityRenderer`, and `AttributeInstance`, and reached resource/registry initialization; the client was intentionally stopped after that point.
- No new class-loading or Mixin injection errors appeared in the filtered startup logs. Interactive gameplay checks for freeze friction, scale, Rock Bottom, and frozen rendering remain manual follow-up validation rather than claims made by this build smoke test.

## 2026-08-30 Pending Changes Review

- Ran the installed `open-code-review` delegate preview and resolved rules for the 10 reviewable code files in the workspace.
- Reviewed the complete `git diff HEAD` for the Gradle configuration, Frozen fire-shatter change, ItemId change, and all MixinExtras migrations.
- The two review observations were confirmed by the user as not being issues: the current test coverage gap is accepted, and the `MIDAS_TOUCH` level change is intentional.
- Final review result: no confirmed issue requiring a source fix. Existing verification remains `compileJava`, `test`, `jar`, and `reobfJarJar` successful under Java 17; runtime gameplay and third-party-mod compatibility remain outside this review's evidence.
