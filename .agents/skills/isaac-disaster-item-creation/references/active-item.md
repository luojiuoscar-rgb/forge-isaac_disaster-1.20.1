# Active Item Branch

Read this file after the router identifies an active item.

## Project Contract

- Implement the item as `ActiveItem` or `DisposableActiveItem` plus an `ActiveAbility`.
- Place the ability under `registries/ability/active/impl`.
- Register the ability in `ModActiveAbilities`.
- Register and bind the item in `ModActiveItems`.
- Add the static item object to `ItemListManager.ACTIVE_ITEM_LIST` so existing datagen creates its model and active tag entry.
- Check the active pool through `TagManager.ACTIVE_ITEMS`, `ModItemTagGenerator`, and `ModLootTableProvider`; follow the current project’s generated `pools/item/active_items` structure and Repentance+ pool decision.

## Use And Charge Contract

`ActiveItem.use()` posts the cancelable `ActiveItemUseEvent`, checks charge, consumes charge on the server, applies the item cooldown, calls `ActiveAbility.onUse`, calls `onFirstUse` once per `ItemStack`, plays `triggerSFX`, and records the active use.

Confirm these values and semantics for every active item:

- Isaac charge per use and maximum charge
- whether it is a zero-charge item
- normal `ActiveItem` or `DisposableActiveItem`
- whether the effect needs `InteractionHand`, the active `ItemStack`, `ContextKeys.TARGET_POSITION`, or another context key
- whether the effect is immediate, lasts for a duration, schedules work, changes inventory, spawns entities, or changes world state
- whether the effect belongs in the reusable trigger or in `onFirstUse`
- whether `onFirstUse` means once per `ItemStack` or requires a different acquisition model
- whether use requires a target, valid entity, block, position, or environment
- whether charge should be consumed when the event is cancelled, the item lacks charge, the target is invalid, or the effect cannot complete
- whether the item should be consumed when a disposable item use fails
- sound, particle, visual, and other non-image assets

The current implementation has these behavior boundaries that must be considered in planning:

- `ActiveItemUseEvent` is posted before the charge check, so an event-based synergy may observe an attempted use rather than a successful effect.
- Charge is changed before `onUse` executes. If an effect can fail after charge consumption, state whether that is acceptable or propose a focused shared change.
- `DisposableActiveItem.use()` shrinks the held stack after calling `super.use()`. Confirm the desired behavior for cancelled or unsuccessful uses before changing this shared class.
- The ordinary item cooldown is 5 ticks.
- Durability represents charge: the current item base uses 100 durability points per charge.

## Charge And Existing Synergies

Inspect and confirm applicable interactions with the existing systems:

- `Car Battery` changes `ActiveAbility`’s normal amplifier from 1 to 2 for `onUse`; confirm whether the new effect scales naturally.
- `9 Volt` reduces the original charge cost by 20 percent in the current `ActiveItem` implementation.
- `The Battery` permits overcharge through `PlayerHelper.chargeItem`.
- `4.5 Volt` disables automatic active-item restoration.
- `AAA Battery` reduces charge cost through the current player-side calculation.
- Other active, passive, trinket, pill, card, set, and inventory interactions must be checked through targeted code searches.

For each relevant synergy, ask whether to retain the natural implementation, add explicit handling, modify it for Minecraft, or exclude it. Do not silently add special cases.

## Ability And Tooltip Contract

- Follow existing active ability patterns: a class-level `private static final CompositeTrigger TRIGGER = ...;`, a constructor calling `super(TRIGGER, id, level)`, and an effect registered in `ModExecutableEffects` when appropriate.
- Reuse existing `SimpleTrigger`, trigger types, executable effects, attack contexts, `StatManager`, and helpers when their semantics match.
- Implement `triggerSFX` for a known sound only after confirming the sound asset. Request missing sound assets from the user.
- Use `getDesc` for normal lore, `getExtraDesc` for Shift-gated detail, and `getSynergyDesc` for contextual synergy text.
- `ActiveItem.addAdditionalInfo` already supplies the recharge hint based on max charge and configuration. Add extra charge text only when the confirmed design needs it.
- Use dynamic descriptions for probabilities, durations, charges, or effect values that can change with player state or an active synergy.
- Review whether a stacking effect should be shown. Existing active implementations may explicitly expose stackability, so apply the common one-copy rule only after confirming the item’s intended tooltip.
- Plan persistent `ItemStack` data explicitly. The shared active item already uses `has_been_used` and `OverCharged`; any additional data requires a focused plan for one item.
