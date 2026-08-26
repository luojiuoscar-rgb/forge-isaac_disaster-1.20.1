# Isaac Disaster Project Findings

## Purpose

This file is the compact, project-wide memory for durable architecture,
extension contracts, workflow constraints, and unresolved issues. Detailed
feature history belongs in `.planning/<task-id>/`.

## Current Constraints

- Target Minecraft Forge `1.20.1`, Forge `47.4.9`, and the configured project
  mappings/toolchain.
- The mod recreates *The Binding of Isaac* effects while adapting them to
  Minecraft's server-authoritative, multiplayer environment.
- Add-on compatibility is maintained through Forge registries and public APIs
  where practical.
- Current source is authoritative. Archived planning files describe intent and
  history, not necessarily the current implementation.
- The user prefers comments only for complex logic, public utility APIs, hidden
  protocols, side effects, and boundary conditions. Simple registrations and
  numeric item implementations do not need full Javadocs.

## Architecture Map

### Bootstrap And Registries

- `IsaacDisaster.java` is the composition root. `ModRegistries.java` creates the
  add-on-facing registries for abilities, effects, attack types, trajectories,
  bullet colors, trigger modules/rules, recursive modules, familiar descriptors,
  and related systems.
- Built-in numeric IDs derived from enum ordinals must be appended, never
  reordered. New passive items must also be added to the corresponding
  `ItemListManager` list for datagen and item-pool resources.

### Item, Ability, And Effect Layers

- Passive items delegate lifecycle behavior to registered `PassiveAbility`
  instances. Obtain/remove operations must be symmetric; one-time behavior
  belongs to first-obtain handling.
- Active, pickup, trinket, set, trigger-module, recursive-module, and executable
  effect registries define the extension boundaries. Prefer existing managers,
  context keys, and lifecycle APIs over direct capability mutation.
- `StatManager` is the common interface for persistent stat changes and module,
  attack, bullet-color, trajectory, familiar, and set-count registration.

### Capabilities And Events

- Player state is split across focused capabilities such as Isaac items,
  abilities, stat modifiers, item pools, item-use records, and familiar data.
  Living entities use effect-module and extra-data capabilities.
- `ForgeEvents` owns capability attachment, player lifecycle synchronization,
  cloning, and other global player integration. Event-specific behavior should
  remain in its owning event subsystem.
- Server state is authoritative. Client HUD/tooltips/rendering use explicit
  synchronization or prediction contracts and must not read unsynchronized
  server capabilities directly.

### Trigger Modules And Effects

- `TriggerModuleEvents` dispatches immutable module snapshots in priority order.
  `SimpleTrigger` conditions should express event/eligibility gates; effect
  implementations should primarily consume validated context and perform the
  effect.
- Necessary input validation, recursion guards, state-machine transitions, and
  effect parameter calculations remain inside effects.
- `TriggerModuleRule` is the extension point for module-combination admission
  rules. A matching rule can reject a module before its full `fire()` call;
  rules must not mutate queues or contexts. Concrete item synergies generally
  remain in effect/module logic unless they prevent an unsafe module combination.
- Recursive-module interactions are separate from trigger-module rules and
  require their own design when migrated.

### Attack And Projectile Pipeline

- Attack types, attack combinations, trajectories, and bullet colors are
  registry-backed extension points.
- Tear bullets use the server post-movement contract: execute the prior plan,
  compute the next plan, then publish it for client rendering/collision use.
  Lasers use an immediate stepped simulation but share the `IBulletObject`
  contract and registered trajectories.
- Changes to shared trajectories affect both tear and laser consumers. Preserve
  arbitrary launch orientation and test both paths.

### Familiar And Mom's Knife

- Familiar requirements and runtime entities are managed by the familiar
  capability/reconciliation subsystem. Runtime UUIDs are not persistent.
- Mom's Knife uses a generic state-machine contract with concrete state timing,
  cached rear-semicircle formation positions, server-published predicted visual
  positions, client interpolation, and slash-only contact damage.
- Familiar client rendering must consume server-published plans rather than
  independently recomputing formation or attack movement.

### Other Major Systems

- Player scale, Curios slot state, loot generation modes, pill effects, Isaac
  bombs, flight, and world time-stop each have feature-specific records under
  `.planning/`. Read the relevant task directory before changing them.
- The planned Frame Block/coating system is not implemented; its design record
  is under `codex/plans/`.

## Extension And Maintenance Rules

- Use the narrowest existing ownership boundary. Avoid adding cross-system
  checks to helpers when the behavior belongs to an item, module, event, or
  capability.
- Preserve server/client side separation and avoid loading client-only classes
  on a dedicated server.
- For new behavior, inspect the base class, registry, event entry point, and one
  representative implementation before editing repetitive classes.
- Use `compileJava`, focused tests, and `git diff --check` for ordinary changes;
  broaden verification according to the touched subsystem.

## Unresolved Or Confirmed Maintenance Items

These are candidates for verification before fixing; they are not automatic
requirements:

- Hook Worm trajectory behavior remains suspect at many firing angles and must
  be verified for both tear and laser consumers.
- Review the pill effect-history NBT reversal path, item-pool getter mutability,
  Golden Pill probability, enchanted trinket checks/value selection, numeric
  item-to-registry mapping, and shallow module-copy behavior against current
  source before opening fixes.
- The current networking/configuration contracts should be rechecked whenever
  new client-visible state or server-authoritative configuration is added.

## Source Entry Points

- `src/main/java/net/luojiuoscar/isaac_disaster/IsaacDisaster.java`
- `src/main/java/net/luojiuoscar/isaac_disaster/registries/ModRegistries.java`
- `src/main/java/net/luojiuoscar/isaac_disaster/manager/StatManager.java`
- `src/main/java/net/luojiuoscar/isaac_disaster/event/ForgeEvents.java`
- `src/main/java/net/luojiuoscar/isaac_disaster/event/TriggerModuleEvents.java`
- `src/main/java/net/luojiuoscar/isaac_disaster/registries/ability_effect/`
- `src/main/java/net/luojiuoscar/isaac_disaster/registries/trigger_module/`
- `src/main/java/net/luojiuoscar/isaac_disaster/entity/custom/TearBullet.java`
- `src/main/java/net/luojiuoscar/isaac_disaster/entity/familiar/`

## Recovery Order

1. Read root `task_plan.md`, this file, and the tail of root `progress.md`.
2. Select the relevant `.planning/<task-id>/` directory and read all three
   files there.
3. Verify important claims against current source and Git state.
4. Update the task directory during work; update this file only with durable
   cross-feature findings.
