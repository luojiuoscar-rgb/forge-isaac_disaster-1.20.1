# Findings: Final Conversation State

## Rock Bottom Final Architecture

- Current target attributes are attack damage, movement speed, luck, bullet
  speed, tears, tears correction, and bullet range.
- `AttributeInstance#getValue()` sees the vanilla final value after modifier
  operations, equipment, and effects. Rock Bottom compares that value with the
  per-player historical maximum and returns the larger value.
- Only target attributes on player entities with a registered attribute instance
  participate. `AttributeMap.hasAttribute(attribute)` must be checked before
  reading an instance; missing attributes return the original value and do not
  touch history or synchronization.
- Attribute ownership is bridged through Mixins because vanilla
  `AttributeInstance` does not know its owning entity. A null result from
  `AttributeMap#getInstance()` is ignored safely.
- Server history lives in `ExtraData`. Client history is a synchronized cache and
  is never authoritative.
- `rockBottomCount` is a transient cache in `PlayerIsaacItems`. The lifecycle
  callbacks are the update point: obtain increments, remove decrements, and the
  state helper clears history when the count reaches zero.
- Capability loading and death-copy rebuild the count from the persisted passive
  list and applied Curios slots. Container mutation code records item state but
  does not independently adjust the Rock Bottom count.
- `getItemCountFromAll(ItemId.ROCK_BOTTOM.getId())` reads the lifecycle cache;
  other item IDs retain the normal passive-list plus applied-Curios calculation.
- Multiple copies share one history. Removing a non-final copy preserves it.
  Removing the final copy clears server history and sends an empty client packet.
- No sprint-specific branch remains. A sprint-created movement-speed value is
  handled under the same normal history rule as any other current final value.
- The old recursive module, executable effect, compensating modifier UUID, legacy
  migration, `removeSilently`, and compatibility constants are intentionally
  absent because the mod is unpublished.

## Attribute Candidate Scan

- Minecraft 1.20.1 exposes 13 vanilla attributes, including max health, follow
  range, knockback resistance, movement speed, flying speed, attack damage,
  attack knockback, attack speed, armor, armor toughness, luck, zombie
  reinforcement chance, and horse jump strength.
- Forge 47.4.9 exposes six attributes: swim speed, nametag distance, entity
  gravity, block reach, entity reach, and step height addition.
- The mod's custom attributes can be added only after registration and inclusion
  in the player's attribute supplier. Then `AttributeMap.hasAttribute()` should
  be verified before adding an attribute to the Rock Bottom target list.
- No new attribute was selected or added during this conversation.

## PlayerTick And ServerTick

- A `PlayerTickEvent` is emitted for each player each tick. The previous handler
  mistakenly iterated the full server player list inside each player event,
  creating repeated work and multiplayer `O(n^2)` behavior.
- The current split makes player work local to `event.player`, with cadence based
  on `player.tickCount`; global scheduler and token-bucket maintenance execute
  once per server tick.
- This preserves existing flight, familiar, input, attack, recursive-module,
  and size-refresh behavior while removing duplicate processing.

## Freeze Active Damage Root Cause And Fix

- `LivingAttackEvent#getEntity()` denotes the entity being hurt, not the entity
  that initiated the attack.
- Before the fix, frozen-target logic only ran when the victim itself was a Mob
  using low friction. For a Slime touching a player, the victim was the player,
  so the cancellation branch was skipped.
- Freezing Mob AI does not guarantee damage immunity. Contact damage such as a
  Slime's player-touch path can be initiated outside the paused AI step.
- The corrected event handler resolves a Mob attacker from the damage source,
  preferring the causing entity and falling back to the direct entity. It cancels
  the shared `LivingAttackEvent` when `EntityFreezeRules.shouldFreeze(attacker)`
  is true.
- This is a root-level correction for standard Forge/Minecraft damage routed
  through `LivingAttackEvent`, not a Slime-specific visual patch. Damage with no
  Mob attribution or custom code that bypasses `hurt()` remains outside its
  contract.

## Relevant Source Locations

- `src/main/java/net/luojiuoscar/isaac_disaster/registries/ability/passive/impl/RockBottom.java`
- `src/main/java/net/luojiuoscar/isaac_disaster/system/rock_bottom/RockBottomState.java`
- `src/main/java/net/luojiuoscar/isaac_disaster/capability/player/PlayerIsaacItems.java`
- `src/main/java/net/luojiuoscar/isaac_disaster/event/ServerTickEvent.java`
- `src/main/java/net/luojiuoscar/isaac_disaster/event/effect/FreezeEffectEvents.java`
- `src/main/java/net/luojiuoscar/isaac_disaster/system/freeze/EntityFreezeRules.java`
- `src/main/java/net/luojiuoscar/isaac_disaster/mixin/MobMixin.java`

## Resolved Review Decisions

- Do not reintroduce a second applied-count function when the current effective
  count API already defines the desired semantics.
- Do not use a silent removal path for history. Normal lifecycle removal updates
  the count, then the state layer performs idempotent cleanup.
- Do not scan all item containers from the hot `getValue()` path.
- Do not add dedicated sprint exclusions or Slime-specific Mixins unless a new
  requirement demonstrates that the shared event contract is insufficient.
