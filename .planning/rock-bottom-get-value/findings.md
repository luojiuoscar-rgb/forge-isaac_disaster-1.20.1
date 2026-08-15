# Findings: 谷底石 GetValue 重构

## Confirmed Plan Decisions

- The item is Isaac `Rock Bottom`, Wiki original ID `485`.
- Existing mod identity is authoritative: `ItemId.ROCK_BOTTOM`, registry ID
  `rock_bottom`, level `3`; no enum insertion or reordering is allowed.
- The seven target attributes are `Attributes.ATTACK_DAMAGE`,
  `Attributes.MOVEMENT_SPEED`, `Attributes.LUCK`, `ModAttributes.BULLET_SPEED`,
  `ModAttributes.TEARS`, `ModAttributes.TEARS_CORRECTION`, and
  `ModAttributes.BULLET_RANGE`.
- The hook must compare the vanilla final value, including all modifier
  operations, with a historical maximum and return the larger value.
- The server stores authoritative history in `ExtraData`; clients receive a
  synchronized cache through a new S2C packet.
- The final Rock Bottom removal clears history immediately. Re-obtaining it
  starts a new history.
- The old recursive module, executable effect, compensating modifier UUID, and
  migration path are removed because this mod has not been released and does
  not require save compatibility.

## Initial Source Map

- Passive entry: `src/main/java/net/luojiuoscar/isaac_disaster/registries/ability/passive/impl/RockBottom.java`
- Old recursive behavior: `src/main/java/net/luojiuoscar/isaac_disaster/registries/recursive_module/impl/RockBottom.java`
- Old value and modifier effect: `src/main/java/net/luojiuoscar/isaac_disaster/registries/ability_effect/impl/normal/RockBottom.java`
- Mixin configuration: `src/main/resources/isaac_disaster.mixin.json`
- Existing common Mixin surface includes `LivingEntityMixin`, `MobMixin`, and
  `PlayerMixin`; the new owner-binding path must remain common-side.
- Forge `AttributeInstance` has no owner reference. `AttributeMap` is owned by
  `LivingEntity`, so the binding must bridge `LivingEntity -> AttributeMap ->
  AttributeInstance`.
- Current registration identity is present in `ItemId`, `ModPassiveAbility`,
  `ModPassiveItems`, `ModExecutableEffects`, and `ModRecursiveModule`; these
  registrations must not be renamed or reordered.
- The old effect imports `ExtraDataProvider`, `AttributeInstance`, and the
  custom `ModAttributes` path, confirming that existing history is entity
  capability data rather than player-only capability data.
- `ClientDataManager` is a singleton cache used by client-side ability and HUD
  code, but Rock Bottom's authoritative history must remain separate from
  item-count cache semantics unless the existing packet contract supports it.

## Required Behavior Matrix

| Situation | Expected result |
| --- | --- |
| No history | Save vanilla final value and return it |
| Current value higher | Replace history and return current value |
| Current value lower | Return history; do not create a modifier |
| Non-target attribute | Return vanilla value unchanged |
| Non-player entity | Return vanilla value unchanged |
| No Rock Bottom holder | Return vanilla value unchanged |
| Several copies | One shared history per player/entity |
| Last copy removed | Clear server history and client cache |

## Compatibility Notes To Verify In Source

- `ExtraData` NBT serialization and clone handling must be used rather than a
  second persistent capability.
- Login packet registration and client cache conventions should be reused.
- Attribute modifiers must be inspected by operation and UUID so old saves can
  be cleaned without removing unrelated modifiers.
- Recursive module removal must follow existing `StatManager` and passive
  lifecycle conventions.
- The final GetValue interception must avoid re-entering itself while querying
  owner state or migration helpers.

## Source Findings From Initial Read

- `ExtraData` already persists arbitrary `ResourceLocation -> double` values
  under the `doubleValues` compound and is copied during entity cloning. The
  new history can reuse this store without adding a capability.
- Rock Bottom history cleanup belongs to `RockBottomState` and is idempotent;
  normal passive removal calls the state helper after the applied count has
  already been updated.
- `ModMessages` allocates packet IDs by registration order. The new packet must
  be appended to the registration sequence to avoid shifting existing IDs.
- `ClientDataManager.init()` clears item, set, pill, flight, and charge caches;
  Rock Bottom history needs an explicit cache API and should be cleared with
  client lifecycle reset.
- `isaac_disaster.mixin.json` currently lists only common entity/player Mixins
  plus client renderer Mixins. Attribute owner-binding and value interception
  must be added to the common list.
- `build.gradle` enables JUnit 5 through `useJUnitPlatform()`, so a focused
  pure Java test is available even though the requested final verification must
  not run compile/build tasks.
- Login synchronization is centralized in `ForgeEvents.onPlayerLoggedIn`,
  while all recursive modules are ticked by `ServerTickEvent`; removing the
  Rock Bottom recursive module eliminates its old 20-tick behavior.

## Lifecycle Findings

- `PassiveAbility.onObtain` and `onRemove` invoke the concrete handlers once
  per passive stack, so Rock Bottom removal can use the post-removal item count
  to identify the final copy.
- Player death cloning explicitly copies player capabilities after
  `reviveCaps`, but entity `ExtraData` is not among the shown player clone
  copies; the new history therefore needs an explicit clone path or a helper
  that copies the old entity capability to the new player.
- Existing `LivingEntityMixin` is the common owner-binding point. `PlayerMixin`
  currently handles dimensions only and should not own attribute-value logic.
- `PlayerIsaacItems.getItemCountFromAll` counts both the extra passive-item list
  and equipped Curios passive slots, making it the correct post-removal source
  for the final-copy check.
- Player death cloning already copies `ExtraData` after `reviveCaps`; history
  persistence across death can reuse that path.
- `RecursiveModuleQueue` does not need a Rock Bottom-specific removal path;
  Rock Bottom no longer registers a recursive module or performs migration.
- Curios unequip removes the tracked applied slot record before invoking
  `PassiveItem.tryUnequip`, but the physical Curios inventory may still contain
  the old stack during that callback. Final-copy cleanup must account for this
  event-ordering detail instead of trusting one immediate inventory read.
- Existing S2C packet sources are under `networking/packet`; the earlier lookup
  failure came from a filename read command, not a missing packet directory.
- Forge 1.20.1-47.4.9 mapped artifacts are available in the local Gradle cache,
  so vanilla Mixin signatures can be checked without inventing method names.
- Existing map-based S2C packets clear the corresponding client map before
  applying entries. A Rock Bottom history packet can use the same contract and
  an empty map to represent complete removal.
- New network registration should be appended after existing messages because
  packet IDs are allocated sequentially.
- `LivingEntity` constructor injection plus `AttributeMap#getInstance(Attribute)`
  return injection provides the owner chain without changing vanilla fields.
- `AttributeInstance#getValue()` returns a cached sanitized vanilla final value;
  the new hook runs after that return and therefore sees ADDITION,
  MULTIPLY_BASE, MULTIPLY_TOTAL, equipment, and effect modifiers already
  applied.
- Client history is full-replaced from S2C packets; server history updates are
  written to `ExtraData` only when the vanilla value exceeds the prior maximum.
- Login and recursive ticking no longer perform Rock Bottom migration. The
  passive removal handler and server `getValue()` path clear history when the
  applied Rock Bottom count is zero.
- Resource search found no required texture or pool changes. Only the first
  Rock Bottom lore line needs wording replacement; the existing four-line
  layout remains `no split needed`.
- The old recursive and executable-effect registry entries are removed; only
  the passive item identity and its resources remain.
- The final diff review found the new history packet as an untracked file (it is
  compiled and loaded by `runData`); this is expected until the user stages the
  feature files.

## Lore Layout Check

- Planned wording: `最终属性值保持在历史最高值`.
- Result: `no split needed`.

## Attribute Candidate Scan

- Minecraft 1.20.1 registers 13 vanilla attributes: max health, follow range,
  knockback resistance, movement speed, flying speed, attack damage, attack
  knockback, attack speed, armor, armor toughness, luck, zombie reinforcement
  chance, and horse jump strength.
- Forge 47.4.9 registers exactly 6 attributes: swim speed, nametag distance,
  entity gravity, block reach, entity reach, and step height addition.
- Registry presence is not sufficient for Rock Bottom. Each selected attribute
  must also exist in the player's attribute supplier; the current state guard
  intentionally skips attributes that are absent from the player's map.
