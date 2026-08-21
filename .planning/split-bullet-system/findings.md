# Findings & Decisions

## Requirements
- The user wants a broad background pass and a planned subproject focused on a future split-bullet system.
- The conversation is still exploratory; the output should be design thinking, not code changes.
- The discussion needs both sides: how original Isaac split-like tear items behave, and where those behaviors fit in this repo.
- The examples named by the user suggest at least these item families are in scope: Haemolacria, The Parasite, Compound Fracture, and Cricket's Body. Exact naming can be confirmed later.

## Research Findings
- The repository root already contains project-wide `task_plan.md`, `findings.md`, and `progress.md`; those act as long-lived project memory and should not be overwritten by feature-specific exploration.
- Player trigger modules are dispatched in priority order from `TriggerModuleEvents.dispatch(...)`, using a snapshot of the player's active trigger-module queue.
- During `GET_ATTACK_CONTEXT`, trigger modules can mutate each `AttackContext` before a shot is created. This is how existing modules probabilistically attach bullet effects or recolor bullets.
- `BulletAttack.createBullet(...)` copies the prepared `CompositeTrigger` and trajectory map from `AttackContext` into the spawned `TearBullet`.
- `TearBullet.tick()` posts `BulletTickEvent`, applies steering/trajectory logic, then routes into block collision, entity collision, and end-of-life events.
- Entity hit flow is two-stage: `IsaacAttackBeforeHitEntityEvent` runs before damage, `IsaacAttackAfterHitEvent` runs after damage. Canceling the after-hit event prevents the default non-piercing discard path.
- Block hit flow uses `IsaacAttackHitBlockEvent`; if nobody cancels it, the bullet is discarded. Existing bounce effects cancel this event and rewrite velocity.
- Lifetime expiry uses `TearBulletEndOfLifeEvent`; if nobody cancels it, the bullet is discarded. This is already a clean seam for "spawn children when the parent dies."
- Bullet-side triggers are stored in `CompositeTrigger` and fire in insertion order. Since trigger modules attach during context construction, module priority effectively influences bullet-trigger ordering.
- Existing examples already cover the three main hook styles we care about:
- `MomsEyeshadow` / `IronBar`: attach per-shot bullet triggers during `GET_ATTACK_CONTEXT`.
- `BounceOnEntity` / `BounceOnBlock`: modify bullet continuation behavior after collision and cancel the default discard.
- `Ipecac`: reacts on entity hit, block hit, and end-of-life, showing that one item can bind to multiple bullet lifecycle moments.
- The current repo does not yet contain explicit implementations for Haemolacria, The Parasite, Compound Fracture, or Cricket's Body.
- Attack-type priority and trigger-module priority are separate systems. Attack type chooses the overall firing mode; trigger modules decorate the resulting attack/bullet lifecycle.
- The user's current design hypothesis is: keep an ordered split-effect list, trigger only the first matching split effect when a parent tear should split, and delegate the remaining effects to spawned child tears.
- That hypothesis matches one real need in this repo: collision-split effects probably need an exclusivity layer instead of blindly stacking every module on the same lifecycle event.
- The risky part of the plain linked-list approach is recursion ambiguity. If child tears simply inherit the remaining list mechanically, mixed cases such as Haemolacria plus Parasite plus Cricket's Body can accidentally encode implementation order as gameplay truth.
- A better fit for this codebase is likely "family selection + inheritance policy" rather than a raw ordered list. In practice that means:
- first choose one split family for the current parent event,
- let that family define whether child tears inherit lower-priority split families, all non-split decorators, or neither,
- keep the decision at one explicit resolver point instead of spreading it across independent trigger modules.
- The existing trigger-module rule system can already express part of this exclusivity problem because rules can see the active module snapshot while deciding whether a candidate module is allowed to fire.
- The user's new refinement is a priority-ordered queue where same-priority effects fire together and lower-priority effects pass down to child tears.
- That refinement is reasonable if and only if same-priority entries are treated as one composable layer. It becomes risky if same-priority entries are mutually exclusive split cores, because then one event can spawn duplicate or conflicting child patterns.
- A safer interpretation is:
- same priority = same family or same layer, so effects can stack by design,
- different priority = different layer, so effects are deferred to children or blocked by the resolver,
- one explicit resolver still decides which layer owns the current parent tear.
- That makes the queue a ranking mechanism, not the whole rule system.
- The repo already has a separate module family for revive logic: ModReviveModule plus ReviveSequence.
- That architecture is the closest local precedent for a future split system, because it keeps module registration, ordering, consumption, and UI/state concerns separate from ordinary trigger-module dispatch.
- Reusing the trigger-module registry for split semantics would blur two different jobs: general combat hooks and parent/child tear inheritance.
- A dedicated split module would let you express override, extra-split, and inheritance-policy behavior without overloading unrelated combat modules.

## Technical Decisions
| Decision | Rationale |
|----------|-----------|
| Default assumption: split tears are bullet-lifecycle behaviors, not standalone attack types | They usually decorate ordinary tears instead of replacing the entire firing mode. |
| Keep room for a smaller set of reusable split families instead of one generic "any child-spawn" switch | Original Isaac effects differ meaningfully by trigger moment, child count, direction source, and whether the parent survives. |
| Prefer existing lifecycle seams over new synchronized per-bullet state | The repo already has hit/block/end-of-life hooks that are cheaper than inventing new networked bullet state. |
| Evaluate collision-split planning as an exclusive family resolver, not "fire every matching trigger" | Multiple child-spawn modules on the same event will otherwise compose accidentally and become hard to reason about. |
| Allow same-priority stacking only for intentionally composable effects | This preserves your queue idea without letting mutually exclusive split cores interfere with each other. |
| Prefer a dedicated split module family over reusing ordinary trigger modules | Revive logic in this repo already shows that distinct module semantics are easier to maintain when they get their own registry and sequence. |

## Issues Encountered
| Issue | Resolution |
|-------|------------|
| Bare `python` is unavailable in this Windows environment | Use the bundled runtime path already documented in the root project findings. |
| The user examples name behaviors colloquially rather than by exact item ID | Carry the likely item mapping for now and confirm only if it affects the design boundary. |

## Resources
- `D:/Oscar/MC/mods/forge-1.20.1-47.4.9-mdk/src/main/java/net/luojiuoscar/isaac_disaster/event/TriggerModuleEvents.java`
- `D:/Oscar/MC/mods/forge-1.20.1-47.4.9-mdk/src/main/java/net/luojiuoscar/isaac_disaster/registries/attack_type/impl/BulletAttack.java`
- `D:/Oscar/MC/mods/forge-1.20.1-47.4.9-mdk/src/main/java/net/luojiuoscar/isaac_disaster/entity/custom/TearBullet.java`
- `D:/Oscar/MC/mods/forge-1.20.1-47.4.9-mdk/src/main/java/net/luojiuoscar/isaac_disaster/registries/trigger_module/TriggerModule.java`
- `D:/Oscar/MC/mods/forge-1.20.1-47.4.9-mdk/src/main/java/net/luojiuoscar/isaac_disaster/registries/trigger_module/TriggerModuleQueue.java`
- `D:/Oscar/MC/mods/forge-1.20.1-47.4.9-mdk/src/main/java/net/luojiuoscar/isaac_disaster/registries/trigger_module/impl/normal/Ipecac.java`
- `D:/Oscar/MC/mods/forge-1.20.1-47.4.9-mdk/src/main/java/net/luojiuoscar/isaac_disaster/registries/ability_effect/impl/normal/BulletBounceOnEntity.java`
- `D:/Oscar/MC/mods/forge-1.20.1-47.4.9-mdk/src/main/java/net/luojiuoscar/isaac_disaster/registries/ability_effect/impl/normal/BulletBounceOnBlock.java`
- `D:/Oscar/MC/mods/forge-1.20.1-47.4.9-mdk/src/main/java/net/luojiuoscar/isaac_disaster/registries/trigger_module/impl/normal/MomsEyeshadow.java`
- `D:/Oscar/MC/mods/forge-1.20.1-47.4.9-mdk/src/main/java/net/luojiuoscar/isaac_disaster/registries/trigger_module/impl/normal/IronBar.java`
- `D:/Oscar/MC/mods/forge-1.20.1-47.4.9-mdk/src/main/java/net/luojiuoscar/isaac_disaster/capability/player/PlayerAbility.java`
- `D:/Oscar/MC/mods/forge-1.20.1-47.4.9-mdk/src/main/java/net/luojiuoscar/isaac_disaster/registries/attack_type/AttackSelector.java`

## AttackPattern implementation findings
- The approved v1 scope is AttackPattern infrastructure, not SplitModule implementation.
- `AttackType` already exposes `getAttackContexts(...)` and `performAttack(...)`; callers can compose a registered Pattern without changing AttackType's abstraction.
- `ModRegistries` uses custom `ResourceKey`, `DeferredRegister`, `RegistryBuilder`, and `register(modEventBus)` blocks for sibling registries.
- `AttackContext.copy()` currently copies the trajectory map but aliases the mutable `CompositeTrigger`; this must be corrected for generated contexts.
- The three approved migrations are Tammy's Head (12 ring contexts), Sad Bomb (8 or 13 ring contexts based on bomb power), and Loki's Horns (4 ring contexts replacing the event list rather than appending four to the original list).
- Ring geometry should derive a three-dimensional forward direction from the reference rotations, choose a stable orthogonal side vector with a fallback axis, and rotate forward around the fixed plane using `360 / count`.
- The ring-only v1 decision has been extended by a separate semicircle follow-up for Loki's Horns.

## Semicircle follow-up findings
- Loki's Horns can reuse the same three-dimensional direction math with a 180-degree arc by flipping the reference direction before pattern generation.
- For Loki, the event path should preserve the existing contexts and append the three semicircle outputs, not replace the list.
- A generic semicircle Pattern is enough for the current request; no AttackType refactor is needed yet.
- Direction-vector to rotation conversion belongs on `AttackContext`; `setDirection(Vec3)` now establishes an absolute direction and clears rotation offsets.

## Visual/Browser Findings
- No browser or image inspection was used in this exploration pass.
