# Trinket Branch

Read this file when the router identifies a trinket. Apply the common research, ratio, module, synergy, asset, and approval rules; use this branch's lifecycle and registration contract.

## Project Contract

- Implement `Trinket` plus `TrinketAbility`; abilities live under `registries/ability/trinket/impl/` and register in `ModTrinketAbilities`. Bind the item in `ModTrinkets` and append its static object to `ItemListManager.TRINKET_LIST`.
- Append to `manager/id/TrinketId`, using its `ordinal()` for the mod numeric ID. Keep Wiki trinket ID, mod numeric ID, and registry `ResourceLocation` distinct. Confirm the project's level; do not invent a collectible-quality correspondence.
- Read `TrinketAbilityContext`, `Trinket`, and relevant `CurioSlotManager` / `PlayerIsaacItems` code for lifecycle or storage-sensitive work. The ability receives a `LivingEntity`; check the required entity type and logical side before applying player-only effects. Context contains `stack` and `isEnchanted`; paths that construct a context without a stack must not be assumed to have one.
- Keep the existing Curios callback route through `IIsaacCuriosItem` and `CurioSlotManager`. Do not add separate equip listeners, capability storage, or recursive slot synchronization for an ordinary trinket.

## Lifecycle And Swallowing

- Current equip order is `onEquipped`, then `onFirstEquipped` if the physical stack is not `consumed`, then setting the `consumed` flag. First-equipped means once per marked stack, not every equip or every player. Place approved one-time rewards here and preserve the existing order.
- Put removable attributes and module contributions in `onEquipped` and reverse the same ordinary/enhanced contribution in `onUnequipped`. The passive branch's first-obtain MaxHealth persistence rule does not automatically apply to trinkets; confirm exceptional permanent effects.
- `consumed` is the first-equipped marker; `swallowing` marks transfer into swallowed storage. Neither means the same thing as ordinary item deletion. `enchanted` is the project's custom enhancement flag, not a vanilla enchantment entry.
- Swallowing transfers an already active trinket from Curios to `PlayerIsaacItems` storage while preserving its effect. Use the existing `CurioSlotManager.swallowTrinkets` route where appropriate; do not remove and reapply its bonuses, duplicate first-equipped rewards, or separately add its modules again. Preserve enhancement, first-equipped state, and approved custom data.
- Removing or clearing a swallowed trinket uses existing removal paths that invoke `onUnequipped`; ensure one contribution is removed without disturbing other equipped or swallowed copies.
- Use existing queries that include both equipped and swallowed trinkets, such as `PlayerHelper.getTrinketCount` and `PlayerIsaacItems.getAllTrinketListFromId`. A physical count is not enhancement strength: an enhanced copy is not automatically counted twice. Inspect the returned data before deriving an amplifier.

## Ordinary And Enhanced Design

- Plan ordinary and enhanced behavior separately. Enhancement is a project design decision and need not reproduce the original game's golden/enhanced behavior exactly. Wiki behavior is a reference; do not invent an alternative without an approved design.
- Prefer existing modules and their established amplification semantics. Where equivalent, multiple ordinary contributions or a larger amplifier may already express the enhanced result; do not create a dedicated enhanced module merely because the trinket is enhanced. Verify that the module actually accumulates contributions and scales the intended fields, and confirm the approach through the common reuse process.
- Do not assume a universal doubling rule. Enhancement may change a value, probability, duration, scope, condition, or mechanic, or have no additional effect. If the original description or current project pattern does not determine the result clearly, ask the user with concrete proposals and explain the difference from the original and from simple module amplification.
- Distinguish an enhanced copy, several ordinary copies, and mixed equipped/swallowed copies. Confirm aggregate strength, proc timing, upper limits, per-copy versus shared counters, and cleanup only where applicable. Never use item-stack count terminology for `ContextKeys.AMPLIFIER` strength.
- Determine where the effect belongs before adding code: a direct stat bonus, a module contribution, or an existing system that queries trinket ownership/state. Empty equip methods can be intentional, as with `AAABattery`; do not duplicate its effect in a new module. Shared modules such as `PENNY_TRINKET` may already service several trinkets.

## Per-Trinket Confirmation And Descriptions

- Include ordinary/enhanced values and ratios, enhancement implementation, first-equipped rewards, reversible effects, swallowed behavior, duplicate-copy interactions, special loss/consumption conditions, project level, and any extra persistent data in the item specification. Ask only about unresolved decisions, using the common per-item confirmation process.
- Reuse `getDesc`, `getSynergyDesc`, `getExtraDesc`, and `TrinketClientHooks`. Show the ordinary effect in normal lore and the approved enhancement difference in its existing contextual description pattern. Enhancement information is distinct from hidden duplicate-copy stacking information.
- Use `StatManager` descriptions with the same approved ratios as the effects. Use dynamic values where probability or strength depends on player state. Specify whether an enhancement line shows the extra amount or the enhanced total so the two cannot be confused; apply the common lore layout check to both.
- Compare implementation and translations before copying a reference. An existing description is not proof that its code implements it; report discrepancies and confirm the intended semantics.
- Standard enhancement/first-equipped/swallowing flags are part of the existing framework. Additional item-owned persistent data requires a focused design and one-at-a-time implementation, as in the other branches.

## Assets And Pools

- Use the confirmed English Wiki Repentance trinket image as a `32x32` PNG. Do not select a `Collectible_` image merely because the name resembles the trinket. Enhanced appearance already uses the existing name-color/foil logic; create separate art only when requested.
- Insert translations before `"trinket end": ""`, following current ordinary and `.enchanted.lore` key patterns. Reuse shared enhancement/action wording and request missing non-image assets.
- `TRINKET_LIST` drives existing model and `isaac_disaster:trinkets` tag datagen. The Curios `isaac_trinket` tag references that tag, and `data/isaac_disaster/loot_tables/pickups/random/trinkets.json` expands it. Register through this route rather than manually creating models or adding every trinket to collectible `pools/item` tables. Confirm any special acquisition exceptions separately.

## Verification

- Check `TrinketId`, ability/item binding, datagen membership, generated model/tag, Curios eligibility, pool reachability, localization placement, and ordinary/enhanced descriptions.
- Inspect ordinary and enhanced equip/remove symmetry, repeated equip, transfer to swallowed storage without duplicate rewards/bonuses, removal of swallowed copies, mixed-copy aggregation, and preservation across save/reload. If slot behavior changes, cover actual equip, unequip, resize, and reload paths through the existing manager.
- Run the common batch `runData` step and report which checks were static, automated, or observed in game. Do not claim unperformed runtime or persistence verification.
