# Isaac Disaster Project Findings

## Purpose
This is the canonical durable knowledge base for future Codex sessions working on Isaac Disaster. It stores verified project facts, architecture, extension contracts, workflow constraints, and unresolved risks. It is not a chronological log or a list of instructions copied from old conversations.

## Quick Reboot
1. Read `task_plan.md` for the active long-running task and current phase.
2. Read this file for stable project architecture and verified backlog.
3. Read the tail of `progress.md` for recent edits, errors, and test evidence.
4. For a subsystem task, open the source paths listed under Resources plus one or two representative `impl` classes; do not scan every implementation.
5. Treat `codex/项目记忆.md` as legacy migration input and `codex/初步诊断.md` as historical diagnosis. Current source and this file take precedence.

### Current Snapshot
- Stable/completed architecture: scale, Curios idempotency, flight ownership, selective lemon cloud, attack selector/combinations, loot generation modes, point-blank tear collision, Isaac bomb profiles, familiar registry/reconciliation, and the first Mom's Knife implementation.
- Active known player-facing bug: Hook Worm trajectory fails or misorients at many launch angles.
- Confirmed maintenance backlog: pill effect-history NBT reversal, item-pool getter mutation, Golden Pill probability, enchanted trinket checks/value selection, incomplete item-ID mapping, shallow module snapshots, and permissive network-version acceptance.
- Planned but not implemented: Frame Block/coating system; see `codex/plans/FrameBlock第一版规划.md`.

## Requirements
- Target Minecraft Forge `1.20.1`.
- Current Forge version is `47.4.9`; Java toolchain is 17; mappings are Parchment `2023.09.03-1.20.1`.
- Recreate items and systems from *The Binding of Isaac* while adapting them to Minecraft's 3D and multiplayer environment.
- Preserve addon-mod compatibility through registries and public contracts where practical.
- Avoid exhaustive reading of repetitive implementation classes; inspect base classes and representative samples first.
- Use the planning-with-files three-file workflow for long tasks and persistent memory.

## Research Findings
- Legacy sources `codex/项目记忆.md` and `codex/初步诊断.md` were classified during the 2026-07-12 migration. They remain useful historical context, while current source and this file are canonical.
- The repository contains many repetitive implementations under ability/effect `impl` packages, so subsystem ownership is better learned from registries, base classes, capabilities, managers, helpers, and events.
- The user prefers comments with real information gain: complex logic, public utility APIs, hidden protocols, side effects, and boundary conditions deserve Javadoc; simple numeric item implementations and obvious overrides do not.
- Session recovery is available through the bundled Python runtime at `C:/Users/16136/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe`; neither `python` nor `py` is available on PATH.
- The initial catch-up found only three unsynced tool calls from creation of these planning files, with no missing architectural decisions.
- Source-path audit verified all 42 local paths referenced by this file.
- Legacy memory confirms a broad Item -> Ability -> Effect/Module layering, player/entity capabilities, custom registries, loot-table-backed item pools, attack types, pills, pedestals, client synchronization, and configuration systems. These summaries must be checked against current source before becoming canonical.
- `codex/初步诊断.md` now contains only one unresolved code diagnosis: Hook Worm trajectory behavior at most firing angles. Its ContextKey section is an accepted design direction rather than a confirmed bug.
- Several old-memory warnings may be stale because the related systems were changed later in the conversation: attack selection, loot generation modes/modifiers, Curios slot state, familiar entities, flight, scale, and passive/swallowed capability consolidation. Current source takes precedence.
- The old memory initially listed several code-smell candidates. Current inspection confirmed the `PlayerItemUseRecord` NBT direction, mutable pool-set, Golden Pill probability, trinket enchanted-value, and shallow module-copy issues; remaining unverified historical concerns are not treated as current bugs.

## Technical Decisions
| Decision | Rationale |
|----------|-----------|
| Keep stable facts in this file and session history in `progress.md` | Prevents resolved experiments from polluting architectural memory. |
| Cite concrete source paths for subsystem summaries | Lets future sessions verify claims without rereading the entire repository. |
| Record unresolved issues in a dedicated section | Avoids treating historical failed attempts as current design guidance. |
| Mark the old project memory as a legacy archive instead of deleting it | Prevents two competing canonical memories while retaining historical migration context. |

## Current Architecture
### Bootstrap And Registries
- `IsaacDisaster.java` is a thin composition root. It registers custom registries first, then attributes, item families, blocks/block entities/entities, sounds/effects, loot types/modifiers, gamerules, networking, common config, and the client-only config-screen registration.
- `ModRegistries.java` currently creates addon-facing Forge registries for attack trajectories, bullet colors, trigger modules, recursive modules, passive/active/trinket/set/pickup abilities, attack types, attack-combination rules, familiar entity descriptors, and executable effects.
- The legacy registry list was stale because it predated the `familiar_entity` registry; the current source list above is canonical.

### Item And Ability Layering
- Passive collectibles are split into a native `Item` registration (`ModPassiveItems`) and a registered `PassiveAbility` implementation (`ModPassiveAbility`). The item delegates lifecycle behavior to the ability.
- `PassiveAbility.onObtain` posts the ordinary obtain event and applies repeatable effects first. If a non-null stack has not been used, it then posts the first-obtain event and applies one-time effects. `onRemove` posts removal before inverse cleanup.
- Simple numeric passive items should modify attributes/state through `StatManager` in `handleObtain` and apply exact inverse values in `handleRemove`. One-time healing/spawns belong in `handleFirstObtain`.
- `ItemId` still derives numeric IDs from enum ordinal. New built-in constants must only be appended; inserting or reordering constants changes persisted IDs. Quality/level is stored separately in each enum entry.
- `ItemListManager` static lists drive datagen for item models, tags, and pool tables. A newly registered passive item must be added to `PASSIVE_ITEM_LIST` or generated resources will be incomplete.
- Common numeric attribute behavior is centralized in `StatManager`, including configurable per-unit bonuses, stable modifier UUIDs, min/max constraints, display text, capability-backed totals, and side effects such as scale refresh or flight reconciliation.
- Active abilities own a `CompositeTrigger`; `onUse` builds a standard context and uses amplifier 2 instead of 1 when Car Battery is present. Context customization should normally be expressed through `AbilityEffectEntry` rather than bypassing the trigger pipeline.
- Pickup abilities also own a `CompositeTrigger`, default amplifier to 1, and consume one stack after firing for non-creative players.
- Trinket abilities expose first-equip/equip/unequip lifecycle methods and receive a `TrinketAbilityContext` carrying stack-specific data.
- Set abilities are threshold-based registry objects. Crossing the required count invokes obtain/remove effects; first obtain is separately remembered in `PlayerIsaacItems`, and client set-count cache drives synergy progress text.

### Pedestals, Chests, And Identifiers
- Pedestals and Isaac item chests persist an item loot-table ID and use shared display/loot behavior. `ItemDisplayAddEvent` allows effects such as alternate item displays to extend the candidate list before presentation.
- `BlockData` is world SavedData for item-display block positions and typed identifier positions. Getter methods return copies.
- Identifier block entities register their typed `ResourceLocation -> BlockPos` membership, and block removal unregisters it. `PlayerHelper.teleportToNearestIdentifier` consumes this indexed data rather than scanning arbitrary generated structures.
- This identifier system only knows explicit Isaac identifier blocks; it is not a registry of every vanilla/modded structure in the world.

### Capability And Event Ownership
- Current player capabilities are `PlayerIsaacItems`, `PlayerAbility`, `PlayerStatModifier`, `PlayerItemPools`, `PlayerItemUseRecord`, and `PlayerFamiliarData`. The old `PlayerPassiveItem` name is obsolete; passive items and swallowed trinkets are now represented by `PlayerIsaacItems`, while familiar requirements/runtime IDs remain separate.
- Living entities receive `EntityEffect`, `EffectModules`, and `ExtraData` capabilities.
- `ForgeEvents` owns capability attachment, player-login synchronization, scale refresh, Curios reconciliation, attack-type cache refresh, permanent trigger-module installation, clone copying, and command registration.
- On death clones, all major player/entity capabilities are copied after `reviveCaps`; familiar requirements are also copied for non-death clones. Runtime familiar entities are managed separately by the familiar subsystem.
- Login synchronization currently covers set counts, pill identification records, and passive-item counts; changes to persistent server data must consider whether a matching client packet/cache update is needed.
- `PlayerIsaacItems` stores full `ItemStack` copies for the extra passive backpack, swallowed trinkets, and active Curios slot snapshots; it returns defensive copies for public collection access. Set counts and obtained-set IDs are stored alongside those item collections.
- Curios effect idempotency is keyed by `CurioSlotKey -> ItemStack`, not by mutable stack identity. This protects equip/unequip effects from duplicate Curios callbacks while preserving per-stack NBT for item-specific removal behavior.
- `PlayerFamiliarData` stores `ResourceLocation -> FamiliarEntry` in insertion order. Requirement counts persist, runtime entity UUIDs do not. Missing familiars are selected through a non-persistent round-robin cursor, and count changes saturate to `0..Integer.MAX_VALUE`.
- Familiar capability copies intentionally drop runtime UUIDs so replacement entities can be recreated after clone/login instead of referencing unloaded entities.

### Familiar Framework And Mom's Knife
- `AbstractIsaacFamiliarEntity` extends plain `Entity`, not `Mob`; familiars have no health/AI goals, disable physics/gravity, reject damage/targeting/collision, and return `shouldBeSaved() == false`.
- Owner UUID is synchronized through entity data. Server validity checks run at randomized 20-40 tick intervals and require a live `ServerPlayer`, matching capability membership, and distance within 64 blocks. Invalid entities unlink their UUID and discard themselves.
- Formation index is runtime server state; the owner's capability remains authoritative for UUID membership/order. Concrete familiars receive an assigned formation index from reconciliation rather than deriving ownership from client state.
- Mom's Knife implements the generic `FamiliarStateMachine` interface with `IDLE -> APPROACH -> ALIGN -> RAISE -> SLASH -> RECOVER -> RETURN` states.
- Knife formation is a rear semicircle split into expanding rings. Ring/angle are cached when the assigned formation index changes; idle height and radius adapt to the owner's bounding-box size.
- Contact damage is active only during `SLASH`, every four ticks, clears target invulnerability time, deals twice the owner's current total attack-damage attribute, and uses the knife as direct source with the owner as indirect player source.
- Knife movement follows the server post-movement plan: execute the previously planned movement, compute the next movement, and publish a predicted absolute visual position. Synched data stores a `BlockPos` origin plus local `Vector3f` offset to preserve far-coordinate precision, along with sequence and yaw.
- Client rendering interpolates server-published predicted visual positions; it should not independently simulate formation or attack movement.
- `FamiliarEvents` admits at most one missing familiar per living player tick and runs cleanup/excess removal/index synchronization every 20 ticks. Player death immediately discards runtime entities while preserving requirement counts for respawn.
- `FamiliarHelper` is the sole reconciliation/spawn owner. It validates that a familiar descriptor ID exactly matches its native `EntityType` ID, creates the entity, sets owner/initial position/index, records its UUID, and rolls back the record if world insertion fails.
- Cleanup only discards a resolved entity when owner and familiar type match, avoiding deletion of another player's entity when UUID data is corrupt.
- `FamiliarStateMachine` is intentionally a minimal generic state contract; timing, transitions, and animation parameters remain concrete-familiar responsibilities.
- `MomKnifeRenderer` uses the vanilla baked iron-sword model at scale `0.75`, corrects the vanilla diagonal orientation, and offsets the model so animation rotations pivot near the handle tail. Do not replace this with a flat texture or duplicate voxel model.

### Player Scale
- Scale is an attribute-backed system exposed through `ScaleUtils`. Changing the base scale calls `refreshDimensions`; server/client tick guards and a refresh packet ensure dimension recalculation after synchronization.
- Player collision dimensions and eye height are modified through `PlayerMixin`; living-entity eye height and client rendering are scaled separately. This combination is required for appearance, first-person camera, collision, and passage through low spaces to agree.
- The scale implementation is intentionally not a pure render transform. Future changes must verify all four surfaces: renderer scale, dimensions, eye height, and pose/collision behavior.
- `LivingEntityMixin` modifies the Forge/vanilla `getScale` result using the custom attribute, while `PlayerMixin` scales pose-dependent dimensions and standing eye height from that result. Server and local-client caches call `refreshDimensions` only when the value changes.
- The required mixin configuration currently contains only `LivingEntityMixin` and `PlayerMixin`, with `defaultRequire: 1`. This keeps the compatibility-sensitive injection surface narrow.

### Curios Integration
- `CuriosHelper` owns idempotent equip/unequip processing for passive-item and trinket slots. The authoritative record is the persisted `CurioSlotKey -> ItemStack` snapshot in `PlayerIsaacItems`.
- Duplicate callbacks are ignored when the recorded and current stacks match after removing the legacy `on_curios` tag. Different stacks cause old-item unequip followed by new-item equip, preserving per-stack NBT.
- `syncIsaacCurioSlot` and `syncAllIsaacCurios` repair missing, stale, changed, or removed slot records; login and slot-count changes invoke full synchronization.
- Legacy stack-local equipped markers are migration input only and are cleared after migration. New code must not restore them as the source of truth.

### Config UI
- The custom config screen is registered through Forge's Mods-page `ConfigScreenFactory`, not a main-menu injection.
- Config editing is allowed only when no client world/player is loaded. In-world access opens `IsaacConfigUnavailableScreen` rather than exposing editable controls.
- `IsaacConfigCatalog` is a manual typed catalog over `ForgeConfigSpec` values, grouped into player stats, item-related, miscellaneous, and coin categories. Adding a config field does not automatically expose it in the UI; the catalog and localization must also be updated.
- The accepted policy is not to repair attribute differences caused by deliberately editing configuration between equip and unequip. The UI guard prevents ordinary in-world editing; external file manipulation is treated as intentional cheating/admin action.

### Networking And Client Cache
- `ModMessages` uses one Forge `SimpleChannel` with explicit packet directions. Current packets cover passive-item clearing/counts, flight HUD, right-click state, set counts, pill records, Isaac item screen opening, charge HUD, and scale refresh.
- `ClientDataManager` is the client-side cache for passive counts, set counts, pill identification, flight units, pill quality, and charge progress. Tooltips/HUD must not read unsynchronized server capabilities directly.
- Packet handlers and registration order are coupled through incrementing numeric IDs. New packets must be registered deterministically on both sides.

### Compatibility Helpers
- Flight deliberately reuses vanilla `mayfly`/`flying` rather than implementing custom flight physics. `FlightHelper` centralizes every Isaac flight grant/revoke and records whether this mod changed permission plus the preexisting `mayfly` value in `PlayerStatModifier`.
- Isaac flight removal restores the recorded preexisting value and only stops active flying when this mod owned a previously-false permission. Creative/spectator flight is never managed. This is a best-effort shared-boolean ownership model; it cannot perfectly attribute later writes by arbitrary mods.
- Lemon effects use a registered `LemonEffectCloud` subclass. Vanilla `AreaEffectCloud` provides lifetime/radius/particles, while server-side custom ticking applies per-use damage, owner attribution, `EntityHelper.isFriendly` filtering, and player PvP checks.
- Lemon cloud custom damage is persisted in NBT and the entity uses Forge's spawning packet. New selective cloud behavior should extend this entity rather than replace vanilla cloud internals or apply effects solely through generic potion entries.

### Isaac Bombs
- `IsaacBomb` still subclasses `PrimedTnt` for entity lifecycle/rendering but delegates explosion behavior to `IsaacBombExplosion`.
- Built-in `BombData` profiles are `SMALL`, `NORMAL` (default), and `MEGA`. Profiles independently carry render size, fuse, center damage, damage radius, block power/resistance, fire, and block interaction.
- Current center damages are 30/60/120 for small/normal/mega. Explosion `power` expresses the block-destruction radius/category and does not derive entity damage.
- `IsaacBombExplosion` mirrors vanilla ray-shell block sampling, exposure-based damage falloff, blast-protection knockback, Forge explosion hooks, finalization, and client packets, while separating center damage/damage radius from block power.
- Explosion damage uses an explosion damage source attributed to bomb and owner. Effects may adjust individual bomb fields after applying a profile.

### The World Time Stop
- Time stop is implemented as a MobEffect plus a global `LivingTickEvent` hook. If any player has The World effect, living entities without the effect have their tick canceled; effect holders remain mobile.
- Frozen living entities have hurt/invulnerability timers cleared so they can still receive repeated damage. Running mob goals are stopped and creeper swelling is reset.
- Current accepted limitation: this is not Minecraft's later `/tick freeze`; non-living entities, block entities, scheduled ticks, weather, and other world systems are not globally frozen.

### Attack Selection And Execution
- `PlayerAbility` stores owned attack types, colors, and trajectories by `ResourceLocation` counts. It caches the selected attack implementation plus the selected candidate's tier and priority; the cache is rebuilt when attack ownership changes and refreshed on login.
- `AttackSelector` considers both single attack types and registered combination rules. Ranking is deterministic: priority tier, then numeric priority, then number of required attacks, then attack-type ID, then combination-rule ID.
- `AttackType` accepts raw `int priorityTier` and `double priority`; addon mods are not constrained to this mod's convenience enums. Selection always compares tier first and priority second.
- `AttackCombinationRule` is registry-driven, requires a set of attack IDs, may return any registered result attack type, and has its own candidate tier/priority. Single and composite attacks therefore compete in one candidate list.
- `AttackType.isActive(AttackSelectionContext)` is the extension point for temporarily disabling an attack. Combination candidates are rejected if any required attack is inactive.
- Delegating attack types such as Neptunus/Cursed Eye request the strongest lower candidate while explicitly skipping other `DelegatingAttackType` results, preventing recursive delegation chains.
- Attack execution remains directly available on each concrete `AttackType`: callers can obtain contexts with or without `GetAttackContextEvent`, pass contexts to `performAttack`, or invoke the concrete shooting path. The selector only chooses/caches a type; it does not replace implementation classes.

### Tear Bullet Movement And Trajectories
- `TearBullet` uses a one-tick planned-movement pipeline: execute the previously stored delta movement first, then on the server fire `BulletTickEvent`, apply steering/registered trajectories, compute the next `finalMove`, collision-test the current-to-next segment, store it as delta movement, and advance traveled distance from velocity.
- A one-time preflight collision check covers both the spawn-context-to-entity gap and the initial motion segment. `BulletAttack` supplies the context position through `setPreflightStart`; this is the completed fix for point-blank bullets passing through entities.
- Trajectories are registry-driven and applied in map iteration order; ordering effects are intentional project semantics because trajectory composition is not commutative.
- Hook Worm currently computes `int traveled = (int) getTraveled()` and then derives `nextIndex` from that already-truncated value. It only rotates on integer segment crossings and uses world-Y/local-right rotations, confirming the retained angle/segment diagnosis.
- `LaserAttack` is an immediate stepped ray simulation, not a persistent laser entity. It implements `IBulletObject` through an internal `LaserProjectile`, applies the same registered trajectories step-by-step, emits particles, and performs block/entity collision during one attack call.
- Tiny Planet and other owner-relative trajectories receive the shooter's current waist position. Laser computes this from the shooter's bounding box at 60% height, so it follows player scale instead of orbiting at fixed world/foot height.
- Homing target lookup now searches all `LivingEntity` instances and filters through `EntityHelper.isFriendly`; target priority prefers hostile/aggro entities but does not exclude passive living types such as bats solely by class.
- Because trajectories are shared by tear and laser `IBulletObject` implementations, changing `BuiltinTrajectory` affects both unless behavior is split explicitly. Hook Worm fixes must preserve arbitrary launch orientation for both consumers or deliberately introduce separate trajectory entries.

### Executable Effects And Modules
- `ExecutableEffectContext` remains a loose typed blackboard keyed by identity-based `ContextKey<T>` objects. `copy` creates a new map but shallow-copies stored values.
- Stable semantic keys exist for stacks, hands, amplifiers, potions, events, bullets, colors, module queues, and trigger views. Deliberately loose keys remain for target position and lists of booleans, doubles, entities, living entities, and resource IDs.
- Trigger modules are registry entries stored as stack-counted, priority-sorted `TriggerModuleInstance` objects. Dispatch copies and locks a queue view, but `TriggerModuleQueue.copy()` currently copies the list only; instance objects remain shared.
- Recursive modules are stack-counted registered periodic behaviors with cooldowns. Removal invokes the module's cleanup hook. `RecursiveModuleQueue.getCopy()` also shares instance objects, so cooldown mutation during copied iteration intentionally affects the live instance.
- Preserve the trigger/recursive distinction: event-reactive behavior belongs in trigger modules; continuously or periodically evaluated behavior belongs in recursive modules; persistent numeric state belongs in `StatManager`/capabilities.

### Loot Generation Modes
- Loot generation now carries an explicit thread-local mode stack through `LootGenerationContext`; unwrapped vanilla/other-mod generation defaults to `NATURAL_DROP`.
- Current modes distinguish natural drops, explicit spawned drops, derived drops created from another drop, and replacement rolls. Helper-generated loot must wrap calls with the correct mode using `run`/`supply` and guaranteed stack cleanup.
- Each global loot modifier declares its own supported mode set. General replacement behavior may include derived drops, while item-pool removal, Sacred Orb, trinket enchantment, and entity-source modifiers generally operate on natural/spawned drops only.
- Replacement effects such as Mom's Key and Petrified Poop use `REPLACEMENT_ROLL` when constructing substitute loot, preventing replacement generation from recursively behaving like a fresh natural drop.
- The complete mode set is `NATURAL_DROP`, `SPAWN_DROP`, reserved `DIRECT_GIVE`, `DERIVED_DROP`, `REPLACEMENT_ROLL`, and `RAW_ROLL`.
- `LootHelper` is the standard wrapper for table rolls and world spawning; callers with non-spawn semantics should pass a mode explicitly.
- `ItemPoolLootModifier` still intentionally supports only single-result Isaac item-pool tables and reads the first loot pool when rebuilding candidates. New item-pool tables must preserve that shape unless the modifier is redesigned.
- `ItemPoolLootModifier` retains `TempPoolManager` for the rebuilt pool, so generation-mode tracking supplements rather than replaces the temporary-pool mechanism.

### Pill System
- `PillEffectManager` registers numeric pill IDs, discovers registered executable effects that implement `PillEffect`, and stores the randomized mapping in overworld `PillShuffleData`.
- World load either restores the SavedData map or shuffles and immediately saves it. Unknown/missing effect IDs fall back to `I_FOUND_PILLS`.
- Player-specific identification records are stored separately in `PlayerItemUseRecord` and synchronized on login; they are not the authoritative world shuffle mapping.

## Extension Contracts
- Custom registries are the primary cross-mod extension mechanism. Addons should register domain objects through the corresponding `DeferredRegister` rather than modify this mod's enums or static lists.
- Familiar entities use a descriptor sub-registry (`familiar_entity`) in addition to the native entity registry, allowing commands and capability reconciliation to enumerate only supported familiar types.
- Curios lifecycle code should use `CuriosHelper` and `CurioSlotKey`; the stack-local "equipped" marker is no longer the authoritative guard.
- Familiar demand must be changed through `PlayerFamiliarData` counts. Direct familiar spawning is not the public ownership model.
- Addon attack types register their own raw tier/priority numbers. Equal values remain deterministic through registry IDs, so item acquisition order must not decide the winner.
- Addon composite attacks should register `AttackCombinationRule` objects instead of hard-coding pairwise blocking in attack implementations.
- Built-in `ItemId` ordinal values are an internal persistence constraint, not an addon registry. Addons should rely on registered item/ability `ResourceLocation` IDs wherever public APIs allow it.

## Workflow And Validation
- Do not run heavy in-game tests unless the user requests them; provide focused manual test steps for game behavior.
- Lightweight static checks, JSON parsing, datagen, and locally available Gradle tasks are acceptable when appropriate.
- Do not modify or discard unrelated dirty-worktree changes.
- Use `StatManager` and existing registries/helpers instead of bypassing project abstractions.
- Comment policy: document complexity and contracts, not obvious implementation mechanics.
- Passive-item creation follows `.agents/skills/isaac-disaster-item-creation/SKILL.md`: research Repentance behavior, confirm all gameplay numbers, append `ItemId`, register ability/item, add localization/PNG/pools, and run `runData` without a separate build.
- Gradle run configurations exist for client, dedicated server, GameTest server, and data generation. `runData` outputs to `src/generated/resources`, which is included in the main resource source set.
- The terminal's inherited `JAVA_HOME` may be invalid. The verified local Java 17 runtime is `C:/Program Files/Microsoft/jdk-17.0.11.9-hotspot`; override `JAVA_HOME` only for the command rather than modifying IDEA/system settings.
- The wrapper uses Gradle 8.8 in the user's Gradle cache. Downloading the wrapper distribution does not rewrite IDEA project settings.
- Curios `5.9.1+1.20.1` is a mandatory mod dependency. JEI APIs are compile-only and JEI is present as a development runtime dependency, but it is not declared mandatory in `mods.toml`.
- In-game behavior remains primarily user-tested. Use GameTest for deterministic server-side scenarios when worthwhile; do not assume visual movement/rendering can be validated by compilation alone.

## Unresolved Issues
- Confirmed current bug: Hook Worm rectangular trajectory is ineffective or misoriented at many firing angles. `BuiltinTrajectory.HOOK_WORM` truncates traveled distance before calculating the next segment and expresses turns through rotations that do not preserve a stable launch-relative 3D frame.
- Architectural debt, not an immediate rewrite: generic `List<Double>` / `List<Boolean>` ContextKeys carry index-based hidden contracts. Preserve the loose blackboard design for future chaos/glitch transformations, but prefer documented accessors or semantic keys for newly stabilized high-frequency contracts.
- Other legacy code-smell candidates remain unverified and must not be presented as current bugs yet.
- Verified design caveat: trigger and recursive queue copy methods share mutable instance objects. Treat them as iteration snapshots of list membership, not deep state snapshots.
- Confirmed bug: `PlayerItemUseRecord.saveNBTData` reads `EffectRecords` from the destination tag and appends them to memory, while `loadNBTData` serializes the currently empty in-memory list back into the input tag. Pill effect history therefore does not persist correctly and saving can mutate runtime state.
- Verified item-pool boundary: dynamic filtering assumes one generated item and the first loot pool. This is an explicit compatibility constraint for current item-pool JSON, not a generic loot-table implementation.
- Confirmed hidden side effect: `PlayerItemPools.getRemoval/getAddition` and `ServerItemPoolsData.getRemoval/getAddition` obtain the actual per-pool mutable set when it exists, then `addAll` the global set into it. Merely querying a pool permanently copies global IDs into that pool's stored set. Return a new union instead when this is fixed.

## Issues Encountered
| Issue | Resolution |
|-------|------------|
| The terminal exposes neither `python` nor `py` | Use the workspace dependency locator and invoke Python by absolute path. |
| A guessed Mom's Knife renderer path was wrong | Use `rg --files` to locate renderer classes before reading. |
| A guessed `entity/laser` package did not exist | Locate laser consumers by class name; current laser behavior is not stored in a dedicated entity package. |

## Verified Code-Smell Backlog
| Area | Current evidence | Priority when touched |
|------|------------------|-----------------------|
| Pill effect history NBT | Save/load handling for `EffectRecords` is reversed in `PlayerItemUseRecord` | Correct before relying on Echo Chamber or persisted effect history. |
| Item pool query purity | Pool getter methods mutate persisted per-pool sets while constructing global unions | Correct before expanding item-pool mutation features. |
| Module queue snapshots | Trigger/recursive queue list copies share mutable instances | Document or deep-copy only if true state snapshots become necessary. |
| Golden Pill consumption | Comment says 5% chance not to consume, but code consumes only when random `< 0.05` | Confirm intended probability, then invert/fix the condition. |
| Enchanted trinket lookup | `PlayerHelper.hasTrinket(..., true)` calls vanilla `ItemStack.isEnchanted`, while project enchantment is `Trinket.isEnchanted` NBT | Replace with the project accessor before relying on enchanted-only checks. |
| Trinket value selection | `getValueFromTrinket(normal, enchanted, ...)` returns `normal` when an enchanted trinket is found and `enchanted` otherwise | Swap branches after confirming all callers use parameter names literally. |
| Item pool additions by numeric ID | `ItemId.ID_TO_ITEM` is used by `ItemPoolLootModifier`, but current source registers only Experimental Treatment | Centralize/automate built-in item-to-ID registration before relying on arbitrary pool additions. |
| Network version acceptance | Channel protocol is `1.0`, but both client/server predicates currently accept every remote version | Tighten when packet compatibility across mod versions becomes important. |

## Resources
- `task_plan.md`: project-memory migration phases and decisions.
- `progress.md`: chronological session and verification log.
- `codex/项目记忆.md`: legacy broad project memory, retained as migration input.
- `codex/初步诊断.md`: legacy unresolved-diagnosis record, retained as migration input.
- `.agents/skills/isaac-disaster-item-creation/SKILL.md`: passive-item creation workflow.
- `src/main/java/net/luojiuoscar/isaac_disaster/manager/StatManager.java`: central attribute/module/set/familiar mutation API.
- `src/main/java/net/luojiuoscar/isaac_disaster/manager/id/ItemId.java`: built-in ordinal item IDs and quality levels.
- `src/main/java/net/luojiuoscar/isaac_disaster/registries/ability/passive/PassiveAbility.java`: passive lifecycle ordering.
- `src/main/java/net/luojiuoscar/isaac_disaster/manager/ItemListManager.java`: datagen source lists.
- `src/main/java/net/luojiuoscar/isaac_disaster/registries/ability/active/ActiveAbility.java`: active trigger context and Car Battery amplification.
- `src/main/java/net/luojiuoscar/isaac_disaster/registries/ability/pickup/PickupAbility.java`: consumable trigger/consumption lifecycle.
- `src/main/java/net/luojiuoscar/isaac_disaster/registries/ability/set/SetAbility.java`: threshold and first-obtain set lifecycle.
- `src/main/java/net/luojiuoscar/isaac_disaster/block/block_entity/PedestalBlockEntity.java`: pedestal state and loot-table generation.
- `src/main/java/net/luojiuoscar/isaac_disaster/manager/data/BlockData.java`: persisted item-block and identifier position index.
- `src/main/java/net/luojiuoscar/isaac_disaster/IsaacDisaster.java`: mod entry and registration wiring.
- `src/main/java/net/luojiuoscar/isaac_disaster/registries/ModRegistries.java`: custom registry composition.
- `src/main/java/net/luojiuoscar/isaac_disaster/event/ForgeEvents.java`: capability attachment, cloning, and synchronization lifecycle.
- `src/main/java/net/luojiuoscar/isaac_disaster/registries/familiar/`: familiar descriptor registry and built-in familiar entries.
- `src/main/java/net/luojiuoscar/isaac_disaster/capability/player/PlayerIsaacItems.java`: passive items, swallowed trinkets, set counts, and Curios-applied slot state.
- `src/main/java/net/luojiuoscar/isaac_disaster/capability/player/PlayerFamiliarData.java`: familiar requirements and runtime entity tracking.
- `src/main/java/net/luojiuoscar/isaac_disaster/entity/familiar/AbstractIsaacFamiliarEntity.java`: non-persistent familiar ownership/validity base.
- `src/main/java/net/luojiuoscar/isaac_disaster/entity/familiar/MomKnifeEntity.java`: Mom's Knife state machine, formation, damage, and visual-plan publication.
- `src/main/java/net/luojiuoscar/isaac_disaster/helper/FamiliarHelper.java`: authoritative familiar reconciliation and creation.
- `src/main/java/net/luojiuoscar/isaac_disaster/event/FamiliarEvents.java`: per-tick admission, periodic maintenance, and death cleanup.
- `src/main/java/net/luojiuoscar/isaac_disaster/renderer/familiar/MomKnifeRenderer.java`: vanilla sword rendering and pivot transforms.
- `src/main/java/net/luojiuoscar/isaac_disaster/helper/CuriosHelper.java`: Curios slot identity, equip/unequip idempotency, reconciliation, and slot modifiers.
- `src/main/java/net/luojiuoscar/isaac_disaster/system/ScaleUtils.java`: scale attribute lookup and dimension refresh entry point.
- `src/main/java/net/luojiuoscar/isaac_disaster/mixin/PlayerMixin.java`: player dimensions and eye-height scaling.
- `src/main/java/net/luojiuoscar/isaac_disaster/client/config/IsaacConfigScreenRegistration.java`: Mods-page factory and in-world editing guard.
- `src/main/java/net/luojiuoscar/isaac_disaster/client/config/IsaacConfigCatalog.java`: typed list of settings exposed in the UI.
- `src/main/java/net/luojiuoscar/isaac_disaster/helper/FlightHelper.java`: ownership-aware use of vanilla flight flags.
- `src/main/java/net/luojiuoscar/isaac_disaster/entity/custom/LemonEffectCloud.java`: selective, owner-attributed custom cloud damage.
- `src/main/java/net/luojiuoscar/isaac_disaster/entity/tnt/BombData.java`: built-in bomb profiles.
- `src/main/java/net/luojiuoscar/isaac_disaster/entity/tnt/IsaacBombExplosion.java`: vanilla-like custom explosion algorithm.
- `src/main/java/net/luojiuoscar/isaac_disaster/effect/custom/TheWorldEffect.java`: current living-entity time-stop implementation.
- `src/main/java/net/luojiuoscar/isaac_disaster/entity/custom/TearBullet.java`: post-movement planning, preflight collision, trajectories, and collision handling.
- `src/main/java/net/luojiuoscar/isaac_disaster/registries/trajectory/BuiltinTrajectory.java`: built-in trajectory algorithms, including the unresolved Hook Worm implementation.
- `src/main/java/net/luojiuoscar/isaac_disaster/registries/attack_type/impl/LaserAttack.java`: immediate stepped laser simulation and shared trajectory consumption.
- `src/main/java/net/luojiuoscar/isaac_disaster/helper/EntityHelper.java`: friendliness and tracking-target selection shared by attacks/familiars.
- `build.gradle` and `gradle.properties`: Forge/JDK/mappings/dependencies/run configuration.
- `src/generated/resources`: datagen output included in runtime resources.
- `src/main/resources/isaac_disaster.mixin.json`: required mixin list and injection requirement.
- `src/main/java/net/luojiuoscar/isaac_disaster/networking/ModMessages.java`: packet registration and directions.
- `src/main/java/net/luojiuoscar/isaac_disaster/client/ClientDataManager.java`: client-synchronized data cache.

## Visual/Browser Findings
- No visual or browser research was required for this memory migration so far.
