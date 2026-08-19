# Findings & Decisions

## Requirements
- User wants to design a "revival" system next.
- This turn is for project discovery first, not implementation.
- If planning files are needed, they should live under `.planning/<system-name>/`.
- The current system design name for this track is `revival-system`.
- Revival should behave like Isaac: multiple revive sources coexist, each source has a priority, available revive count, and revive-specific side effects.
- On player death, exactly one revive effect should be consumed according to queue priority; the chosen revive source loses one charge/count and performs its revive behavior.
- Revival should intercept death before the normal clone/respawn path completes.
- Queue entries should be independent entries rather than a grouped charge counter.
- Example semantics already named by user:
  - Nine Lives: multiple charges, revives at previous room, forces health to one red heart.
  - 1UP: another revive source with its own priority and revive behavior.

## Research Findings
- Project is a Forge 1.20.1 mod using Java 17, mod id `isaac_disaster`, and Parchment mappings.
- The mod is not item-only; it already contains multiple reusable subsystems under `system/`, notably flight, freeze, and rock-bottom.
- Player-owned gameplay state is primarily stored in Forge capabilities attached in `ForgeEvents.onAttachCapability`.
- Death persistence is centralized in `ForgeEvents.onPlayerCloned`, which revives original caps on death, copies capability data into the new player, then invalidates the old caps.
- Existing clone-copied player data includes passive items, stat modifiers, player ability state, item pool state, pill/item use records, familiar requirements, flight state, entity effects, trigger modules, and extra data.
- Client refresh after login/respawn is packet-driven. `ForgeEvents.syncAllDataToClient` handles several player caches, and feature-specific systems send their own packets as needed.
- `PlayerHelper.teleportPlayerToSpawn` already provides a reusable respawn-location teleport helper.
- Existing systems distinguish persistent requirements from transient runtime state:
  - Familiar system discards runtime familiar entities on death but preserves requirement data for later reconciliation.
  - Flight system clears transient thrust state on death, respawn, and dimension change while keeping persistent enablement data.
- Server-global persistent data uses `SavedData` classes in `manager/data`; player-specific state uses capabilities instead.
- No dedicated revival/resurrect system was found yet. Current death-related code mostly handles cleanup, clone copying, or vanilla respawn helpers.
- The user is deciding between two seams:
  - Reuse the existing trigger event/module machinery directly.
  - Introduce a dedicated revival queue module and let items populate it on obtain.
- Design pressure points that make revival special:
  - Deterministic priority ordering between heterogeneous revive sources.
  - Charge consumption tied to death/respawn lifecycle.
  - Revive-source-specific post-respawn behavior.
  - Possible room-position semantics that do not map cleanly to generic trigger execution.
- The user identified an important MC-specific tension:
  - If revive sources fully repopulate after every real death, items such as Nine Lives can become too strong.
  - If revive side effects such as max-health restriction persist forever after a true death, they become punitive in the wrong way.
- Final semantic split:
  - **Revival source state**: what the player owns or has unlocked as a revive provider.
  - **Available queue entries**: the currently banked extra lives that can intercept fatal damage.
  - **Life-scoped aftermath**: temporary state applied by the revive that consumed the current life, such as health cap, teleport target, or temporary effects.
- Final lifecycle rule for this design:
  - `handleFirstObtain` grants current-life `consumer` entries immediately.
  - `handleObtain` and `handleRemove` update `provider` only.
  - `provider -> consumer` synchronization happens only after a true, non-intercepted death.
  - Current-life entries therefore cannot be refreshed by wearing and removing the same source during the same life.
  - Life-scoped aftermath from consumed revive entries is cleared on the next true death before the next-life `consumer` rebuild.
- Current trigger abstractions are side-effect-oriented, not decision-oriented:
  - `TriggerModule.fire(context, type)` returns `void`.
  - `CompositeTrigger.fire(context, type)` returns `void`.
  - That makes them a poor primary interface for "should this fatal hit be intercepted, and what revive result should be produced?"

## Technical Decisions
| Decision | Rationale |
|----------|-----------|
| Use project discovery to map persistence boundaries before asking design questions | Revival behavior will be wrong if it ignores existing clone, capability, and sync flows |
| Treat `system/flight` and `system/rockbottom` as architecture references | They show current patterns for state ownership, transient reset, and client synchronization |
| Provisional recommendation: model revival as a dedicated deep module behind a small queue-oriented interface | That keeps ordering, consumption, and respawn semantics local instead of scattering them into generic trigger code |
| Keep trigger modules and item abilities as producers/adapters, not as the revival queue itself | This preserves compatibility with the current item system without forcing trigger abstractions to own revive state |
| Prefer composition over inheritance for revive behavior reuse | Composite trigger/effect logic can live inside a revive implementation without forcing revive sources to adopt the `TriggerModule` interface |
| Store `provider` and `consumer` as internal `ReviveSequence` state, not as separate public classes | The user wants one deep module with a small interface, not more public surface area |
| Rebuild `consumer` from `provider` only after true death | This preserves immediate first-obtain value while blocking same-life wear/remove refresh loops |
| Treat revive aftermath as life-scoped unless explicitly marked persistent | This avoids permanent penalties such as Nine Lives forcing one-heart gameplay forever |
| Intercept revival in `LivingDeathEvent`, not in generic trigger events | The death event provides the cleanest seam after vanilla/Forge totem protection has already had a chance to succeed |
| Sync only HUD preview data to the client | The client only needs top-entry textures, not the full provider/consumer state |

## Current Design Memory
- Queue model currently preferred by the user and assistant:
  - `consumer` queue stores the current life's available revive entries and drives fatal-damage interception.
  - `provider` queue stores revive sources granted by equipment/items and represents what should be available on the next life.
  - `FirstObtain` immediately materializes entries into `consumer` for the current life.
  - `handleObtain` and `handleRemove` only update `provider`; they do not refresh or revoke the current life's `consumer` entries.
  - `provider -> consumer` synchronization happens only when the player experiences a true, non-intercepted death, so the next life can be rebuilt from current providers.
- This model is intentionally chosen to satisfy both goals at once:
  - Newly obtained revive sources can help the current life immediately.
  - Repeated wear/remove cycles cannot refresh current-life revive entries.
- Important design consequence:
  - If a revive source is removed during the current life, already-issued consumer entries stay until that life ends.
  - The loss only takes effect on the next true life refresh because provider changes apply only at the life boundary.
- Open implementation caveat to verify later:
  - `FirstObtain` must behave like a stable first-grant event for a source instance or otherwise be implemented with equivalent state, so the same source cannot mint current-life entries repeatedly.
- Implementation status:
  - Implemented as `ReviveSequence` inside `EffectModules`.
  - Registered with a dedicated `ReviveModule` registry.
  - Hooked into `LivingDeathEvent`, clone-time true-death reset/rebuild, and revive HUD sync.
  - Verified with dedicated unit tests and a full `gradle build`.
  - Refined so `TotemOfUndying` and `SimpleRevive` reference registered revive executable effects instead of embedding private static implementations.
  - Final revive-effect seam is hybrid: potion buffs go through `PotionProfile + POTIONS`, while non-potion steps such as `removeAllEffects()` stay in small dedicated revive effect classes.
  - Test-only state helpers in `ReviveSequence` were removed after the tests were rewritten to use NBT/public behavior instead.
  - The temporary `ReviveSequence` test seam was removed again: production code now resolves revive modules directly from the registry, and the dedicated revive-sequence test file no longer exists in the worktree.

## 1up! Follow-up
- `1up!` is now implemented on the same revive queue path as the rest of the system, with first-version HUD still using the default totem icon.
- Its revive behavior is registered as a revive executable effect rather than being hardcoded inside the revive module wrapper.
- The effect uses nearby safe teleport plus full red-heart restore when red-heart capacity exists, and a half soul-heart fallback when it does not.
- The temporary self-drawn `one_up.png` was replaced with the official English Wiki collectible icon `Collectible_1up!_icon.png`, because the item skill now treats this as a passive item texture, not a HUD icon.

## Issues Encountered
| Issue | Resolution |
|-------|------------|
| `planning-with-files` catchup helper could not run because the shell had no `python` command | Switched to manual repo inspection and documented the limitation |

## Resources
- `D:/Oscar/MC/mods/forge-1.20.1-47.4.9-mdk/README.md`
- `D:/Oscar/MC/mods/forge-1.20.1-47.4.9-mdk/docs/documentation.md`
- `D:/Oscar/MC/mods/forge-1.20.1-47.4.9-mdk/src/main/java/net/luojiuoscar/isaac_disaster/event/ForgeEvents.java`
- `D:/Oscar/MC/mods/forge-1.20.1-47.4.9-mdk/src/main/java/net/luojiuoscar/isaac_disaster/event/FamiliarEvents.java`
- `D:/Oscar/MC/mods/forge-1.20.1-47.4.9-mdk/src/main/java/net/luojiuoscar/isaac_disaster/event/IsaacFlightEvents.java`
- `D:/Oscar/MC/mods/forge-1.20.1-47.4.9-mdk/src/main/java/net/luojiuoscar/isaac_disaster/helper/PlayerHelper.java`
- `D:/Oscar/MC/mods/forge-1.20.1-47.4.9-mdk/src/main/java/net/luojiuoscar/isaac_disaster/system/flight/IsaacFlightController.java`
- `D:/Oscar/MC/mods/forge-1.20.1-47.4.9-mdk/src/main/java/net/luojiuoscar/isaac_disaster/system/freeze/EntityFreezeRules.java`
- `D:/Oscar/MC/mods/forge-1.20.1-47.4.9-mdk/src/main/java/net/luojiuoscar/isaac_disaster/system/rockbottom/RockBottomState.java`
- `D:/Oscar/MC/mods/forge-1.20.1-47.4.9-mdk/src/main/java/net/luojiuoscar/isaac_disaster/registries/trigger_module/TriggerModule.java`
- `D:/Oscar/MC/mods/forge-1.20.1-47.4.9-mdk/src/main/java/net/luojiuoscar/isaac_disaster/registries/ability_effect/CompositeTrigger.java`

## Visual/Browser Findings
- No browser or image inspection used in this discovery pass.
