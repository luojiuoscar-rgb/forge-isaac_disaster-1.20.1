# MixinExtras Research Findings

## Scope

This file records evidence and conclusions for the MixinExtras investigation.
Current source and primary documentation outrank historical notes.

## Initial Local Facts

- The repository is a Forge 1.20.1 project with an existing Sponge Mixin setup.
- `build.gradle` currently uses `org.spongepowered:mixin:0.8.5:processor` as an annotation processor.
- `build.gradle` applies ForgeGradle `[6.0,6.2)`, configures Java 17, and enables refmap remapping for client, server, and data runs.
- `gradle.properties` selects Minecraft 1.20.1, Forge 47.4.9, and Parchment `2023.09.03-1.20.1`.
- `src/main/resources/isaac_disaster.mixin.json` declares common and client Mixin classes.
- The Mixin config currently has `required=false`, `minVersion=0.8`, and `defaultRequire=1`.
- Existing Mixin classes currently use core Sponge Mixin annotations such as `@Inject`, `@Redirect`, `@Shadow`, and `@Unique`.
- `LivingEntityRendererMixin` is the current representative `@Redirect` use; `LivingEntityMixin`, `AttributeMapMixin`, `AttributeInstanceMixin`, and `PlayerMixin` use return-value injections for post-processing.

## Local Evidence Pointers

- `build.gradle:1-7,16-17,85-117,144-153`
- `gradle.properties:8-17,34-37`
- `src/main/resources/isaac_disaster.mixin.json:1-20`
- `src/main/java/net/luojiuoscar/isaac_disaster/mixin/LivingEntityRendererMixin.java:27-63`

## External Evidence, Round 1

- GitHub API `https://api.github.com/repos/LlamaLad7/MixinExtras/tags?per_page=30` returned tags through `0.5.5` at the time of this investigation; the first entry was `0.5.5` at commit `b78912113b5b037dfd27d4b651231fd6895243bb`.
- Maven Central search `https://search.maven.org/solrsearch/select?q=g:%22io.github.llamalad7%22%20AND%20a:%22mixinextras-forge%22&rows=20&wt=json` returned one `mixinextras-forge` artifact and reported `0.5.0-rc.2` as its latest version.
- The raw URLs for `master/README.md` and `master/wiki/Installation.md` returned 404. This is a repository-layout signal only; it is not evidence that the project lacks documentation.
- The GitHub tag list and Maven Central index are currently inconsistent (`0.5.5` tag versus `0.5.0-rc.2` Maven latest). The Maven coordinate must be verified before recommending a dependency.

## External Evidence, Round 2

- The official `0.5.5` README at `https://github.com/LlamaLad7/MixinExtras/blob/0.5.5/README.MD` describes MixinExtras as a small companion library for Mixin, not a replacement for Mixin.
- That README's ForgeGradle section targets Forge 1.18.2+ and recommends `compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:<version>"))` plus `implementation(jarJar("io.github.llamalad7:mixinextras-forge:<version>"))` with a Jar-in-Jar version range. Its displayed example uses `0.5.4`, so the example version is not proof that `0.5.5` is already published to Maven Central.
- The same README recommends early `MixinExtrasBootstrap.init()`, usually from an `IMixinConfigPlugin.onLoad` method. The Forge platform source at tag `0.5.5` implements that exact plugin hook and ships `mixinextras.init.mixins.json` with the plugin entry.
- `@WrapOperation` source docs at tag `0.5.5` cover method calls, field access, constants, object construction, `instanceof`, comparisons, array access, casts, and expressions. The handler receives an `Operation` and can pass original or modified arguments to it.
- `@ModifyExpressionValue` changes the result of an expression and `@ModifyReturnValue` changes a method's return value. Both source docs explicitly state that their injectors chain across multiple mods, unlike `@Redirect`, `@ModifyConstant`, or return-value mutation through `@Inject`.
- `@WrapMethod` wraps an entire method and chains unlike `@Overwrite`; its docs note special `@Share` support. Its `order` attribute, like the corresponding attributes on the other wrapper injectors, is only respected on Mixin 0.8.7+.
- `@Local` provides explicit or implicit local-variable capture and `LocalRef` read/write access. Its source docs explicitly warn that `name` targeting does not work on obfuscated Minecraft code; ordinal or index is required for such targets.
- `@Cancellable` supplies cancellable callback information to non-`@Inject` injectors and documents shared cancellation state through a chain of `@WrapOperation` handlers.

## External Evidence, Round 3

- The `0.5.5` source `com.llamalad7.mixinextras.versions.MixinVersion` selects the highest supported compatibility implementation whose version is less than or equal to the runtime Mixin version. Its ordered candidates are `0.8.7`, `0.8.6`, `0.8.4`, `0.8.3`, and `0.8`.
- Because Forge 1.20.1 in this project uses Mixin `0.8.5`, the `0.5.5` runtime selection is deterministically the `v0_8_4` compatibility layer. There is no separate `v0_8_5` module, but the source explicitly covers the range through the `v0_8_4` implementation.
- The `0.5.5` root build targets Java 8 for the library; its Forge platform module compiles against Forge 1.18.2 / Java 17 and packages a Forge GAMELIBRARY jar with `mixinextras.init.mixins.json` in the manifest.
- Direct Maven Central metadata at `https://repo.maven.apache.org/maven2/io/github/llamalad7/mixinextras-forge/maven-metadata.xml` reports `0.5.5` as both `<latest>` and `<release>`, with `lastUpdated=20260827192526`. The earlier search endpoint was stale.
- The `0.5.5` Forge and common POMs have no transitive Maven dependencies. The Forge artifact is intended to carry its runtime contents itself, while the README separately instructs users to use the `-all` jar when bundling manually.
- GitHub release `0.5.5` at `https://github.com/LlamaLad7/MixinExtras/releases/tag/0.5.5` is a stable release published `2026-08-28`; its release note says it fixes a rare incompatibility with redirects compiled against Fabric Mixin 0.17.4.

## Local/Artifact Verification

- The published `mixinextras-forge:0.5.5` jar contains `com/llamalad7/mixinextras/platform/forge/MixinExtrasConfigPlugin.class`, `mixinextras.init.mixins.json`, and `META-INF/jars/MixinExtras-0.5.5.jar`.
- With the project's existing ForgeGradle configuration, `dependencyInsight --dependency org.spongepowered:mixin --configuration compileClasspath --no-daemon` succeeded under Microsoft JDK 17 and resolved `org.spongepowered:mixin:0.8.5` through `net.minecraftforge:forge:1.20.1-47.4.9`.
- The machine's default `JAVA_HOME` currently points to a missing Eclipse Adoptium JDK 21 path, so Gradle fails before configuration unless `JAVA_HOME` is overridden to the verified Microsoft JDK 17 path. This is an environment fact, not a MixinExtras compatibility failure.

## Fit To Current Source

- Best first migration candidate: `LivingEntityMixin.travel` currently redirects `BlockState.getFriction`. `@WrapOperation` can keep the receiver and call arguments, return `0.989F` for the frozen case, and call `Operation<Float>.call(...)` otherwise. It would preserve the behavior while allowing another mod's wrapper to chain.
- Secondary candidates: the two `LivingEntityRendererMixin` redirects around `setupRotations` and `scale`. `@WrapOperation` can pass the `LivingEntityRenderer` receiver and all original arguments through an `Operation<Void>`, which can remove the need for the two `@Shadow` methods. This should be a deliberate compatibility cleanup, not a required dependency migration.
- Return-value candidates: `LivingEntityMixin.getScale`, `PlayerMixin.getDimensions`, `PlayerMixin.getStandingEyeHeight`, and `AttributeInstanceMixin.getValue` can use `@ModifyReturnValue` to return the transformed value without a cancellable callback. The main benefit is chaining with other return-value modifiers.
- `@ModifyExpressionValue` is less suitable for the friction case because the current logic needs the targeted `BlockState` call's receiver and arguments; `@WrapOperation` exposes those directly. It could fit future pure value adjustments where only the resultant expression is needed.
- There is no current `@Overwrite`, `@ModifyConstant`, or `LocalCapture` use in the scanned source. `@WrapMethod`, `@Local`, `@Share`, and `@Cancellable` should therefore remain optional tools for future cases, not reasons to refactor unrelated code now.
- MixinExtras does not change the need for stable target descriptors, refmaps, side-safe mixin configs, or careful injection order. Chainability reduces one major `@Redirect` conflict mode, but it does not make the bytecode target or behavior automatically version-proof.

## Recommendation

- Adopt MixinExtras for this project if the goal is better compatibility between independent mixins. The project already has the required Mixin 0.8.5 runtime and Java 17 toolchain, and the verified 0.5.5 Forge artifact packages its own initialization path and nested runtime.
- Do not add a second standalone Mixin runtime. Use the upstream ForgeGradle split, pin the version, and keep `common` compile-only while using the Forge platform artifact for runtime Jar-in-Jar:

  ```gradle
  def mixinExtrasVersion = "0.5.5"

  dependencies {
      compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:${mixinExtrasVersion}"))
      implementation(jarJar("io.github.llamalad7:mixinextras-forge:${mixinExtrasVersion}")) {
          jarJar.ranged(it, "[${mixinExtrasVersion},)")
      }
  }
  ```

- The snippet is research output only and has not been applied to `build.gradle`. With the official Forge artifact, the embedded `MixinExtrasConfigPlugin` and `mixinextras.init.mixins.json` should perform bootstrap initialization; manual `MixinExtrasBootstrap.init()` is for packaging/platform arrangements that do not provide that Forge bootstrap path.
- The first implementation experiment should convert only `LivingEntityMixin`'s friction redirect to `@WrapOperation`, with a test that the non-frozen branch calls `original.call(...)` and the frozen branch still returns `0.989F`. Then verify client and dedicated-server startup and the existing freeze/scale behavior.
- A later pass may convert the renderer redirects and return-value injections if compatibility pressure justifies it. Do not use `@WrapMethod`, `@Local`, or expression injection without a concrete target that needs them.
- Because this project runs Mixin 0.8.5, do not rely on MixinExtras' `order` attribute for deterministic ordering; the official annotation docs limit that feature to Mixin 0.8.7+.
- This investigation verified static sources, published artifact contents, Maven metadata/POMs, and dependency resolution of the existing Mixin. It did not apply MixinExtras or run a game with the new dependency, so runtime startup remains a required implementation-phase check.

## Primary Sources

- `https://github.com/LlamaLad7/MixinExtras/blob/0.5.5/README.MD`
- `https://github.com/LlamaLad7/MixinExtras/releases/tag/0.5.5`
- `https://repo.maven.apache.org/maven2/io/github/llamalad7/mixinextras-forge/maven-metadata.xml`
- `https://github.com/LlamaLad7/MixinExtras/blob/0.5.5/src/main/java/com/llamalad7/mixinextras/versions/MixinVersion.java`
- `https://github.com/LlamaLad7/MixinExtras/blob/0.5.5/src/main/java/com/llamalad7/mixinextras/injector/wrapoperation/WrapOperation.java`
- `https://github.com/LlamaLad7/MixinExtras/blob/0.5.5/src/main/java/com/llamalad7/mixinextras/injector/ModifyReturnValue.java`

## Resolved/Remaining Questions

- Resolved: use the stable `0.5.5` release line; direct Maven metadata and the published artifact were verified.
- Resolved: `@WrapOperation` is the best first fit; `@ModifyReturnValue` and renderer wrappers are secondary; other extras remain future tools.
- Resolved: ForgeGradle needs the `common` compile-only annotation processor and the Forge platform artifact as runtime Jar-in-Jar.
- Remaining for implementation: run client/server startup with the dependency actually added, then test the first friction-wrapper migration and any interaction with other mods targeting the same call.
## 2026-08-30 Pending Changes Review

- Reviewed all 10 code files selected by the installed `open-code-review` delegate preview; planning documents and miscellaneous untracked files were excluded by the tool's rules.
- The review reported a missing automated test for Frozen fire-damage shattering and an unrelated `MIDAS_TOUCH` item-level change. Both were reviewed against the current work context and confirmed by the user as intentional/non-issues; no code change was required.
- Review conclusion: no confirmed correctness, compatibility, security, or build-blocking issue remains in the current pending changes.
