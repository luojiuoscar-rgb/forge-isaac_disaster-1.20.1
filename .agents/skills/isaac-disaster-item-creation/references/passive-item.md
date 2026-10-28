# Passive Item Branch

Read this file after the router identifies a passive item.

## Project Contract

- Implement the item as `PassiveItem` plus a `PassiveAbility`.
- Place the ability under `registries/ability/passive/impl`.
- Register the ability in `ModPassiveAbility`.
- Register and bind the item in `ModPassiveItems`.
- Add the static item object to `ItemListManager.PASSIVE_ITEM_LIST` so existing datagen creates its model and passive tag entry.
- Check the corresponding passive pool through the project’s current tag and loot-table generation.

## Lifecycle

`PassiveAbility.onObtain` posts the obtain event, calls `handleObtain`, then calls `handleFirstObtain` only when the `ItemStack` has not been marked used. `onRemove` calls `handleRemove`.

- Use `handleObtain` for ordinary acquisition effects.
- Put recovery and spawned drops in `handleFirstObtain`.
- Grant all MaxHealth-related attribute bonuses through the existing `StatManager` interfaces in `handleFirstObtain`, never in `handleObtain`. These bonuses persist after the item is removed: do not subtract or remove them in `handleRemove`. Treat this as an explicit exception to obtain/remove symmetry and record it in the item specification and verification.
- Use `handleRemove` for inverse removal and cleanup of removable effects; preserve first-obtain MaxHealth bonuses.
- Preserve the current order `handleObtain` before `handleFirstObtain`. Ask with a reason before changing it.
- Declare in the specification which lifecycle method changes each provider, consumer, module, attribute, set, or item-owned state.
- For revive items, `handleRemove` must remove both revive `provider` and revive `consumer`, leaving no item-owned state behind.
- A missing set is a no-effect set with three items by default; confirm its name and grant/remove membership through `StatManager`.

## Passive-Specific Confirmation

Confirm the following when applicable:

- persistent stat values and their `StatManager` ratio
- trigger and periodic module names and semantics
- bullet behavior, bullet color, and projectile compatibility
- set name, required count, and set effect
- first-obtain recovery or drop behavior
- Curios unequip behavior and `Config.ALLOW_CURIO_UNEQUIP` implications
- whether `ItemStack` data is unnecessary; if persistent data is required, stop and make a focused plan for one item
- passive-item synergies and whether they are naturally supported or need explicit code

## Passive Tooltip And Effect Rules

- Use the existing passive item description patterns and common `StatManager` descriptions.
- The normal tooltip describes one copy by default. Hide duplicate-item totals and stacking-only values unless the user approves exposing them or the project’s established implementation requires it.
- Run the common lore layout check before localization.
- Implement inverse removal for removable obtain-time stats, modules, sets, providers, and consumers. First-obtain MaxHealth bonuses are excluded and must remain after removal.
