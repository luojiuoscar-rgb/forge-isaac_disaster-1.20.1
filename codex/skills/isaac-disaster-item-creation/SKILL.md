---
name: isaac-disaster-item-creation
description: Use when creating or modifying a passive collectible item for the Isaac Disaster Forge 1.20.1 project, including its Repentance behavior, texture, ability, localization, item ID, and item-pool entry. Do not use for active items, trinkets, cards, pills, or other pickups.
---

# Isaac Disaster Passive Item Creation

Implement one passive item as an item instance plus a passive ability. Read the current project patterns before editing; do not rely on this skill as a frozen code map.

## Research And Confirmation

1. Read the relevant current implementations: `ItemId`, `ModPassiveItems`, `ModPassiveAbility`, similar classes in `registries/ability/passive/impl`, `StatManager`, existing modules, set abilities, localization, and the target pool JSON files.
2. Compare the Chinese Wiki (`https://isaac.huijiwiki.com/wiki`) and English Wiki (`https://bindingofisaacrebirth.fandom.com/wiki/Binding_of_Isaac:_Rebirth_Wiki`) for the Repentance effect, values, level, and Repentance+ pool membership.
3. Implement Repentance by default. If Repentance+ changes the effect, or the two Wikis conflict or omit a needed fact, show the difference and ask the user before coding.
4. Ask before assigning technical names when the original name contains punctuation or other special characters. Confirm the registry ID, texture name, Java name, and module name as applicable. Do not normalize automatically.
5. Ask before creating a new module name. Reuse an existing appropriate module; otherwise explain whether the new effect is item-specific or reusable.
6. Before editing, present every gameplay-affecting numeric value for user approval: original value, ratio conversion, and proposed final Minecraft value. Do not choose final balance values independently.

## Numeric Ratios

Use `StatManager` interfaces and its ratio conventions. Show these as proposed values in the numeric confirmation table, even when the conversion is unambiguous:

| Effect | Ratio or default |
| --- | --- |
| Red-heart health | 1 red heart = 1 health unit |
| Speed | 0.2 = 1 speed unit |
| Damage, luck | 1 = 1 unit |
| Size | 0.2 = 1 size unit; use additive scaling |
| Range | 1.5 = 1 range unit |
| Fire rate | 0.7 = 1 fire-rate unit |
| Fire-rate modifier | 1 = 1 modifier unit |
| Shot speed | 0.2 = 1 shot-speed unit |
| Pill quality | better = +1; worse, such as DHP, = -1 |
| Flight time | an item described as granting flight = +1 unit |
| Projectile count | follow the original behavior |

Ask the user before implementing entity reach, block reach, attack speed, block-breaking speed, knockback, projectile size, multiplicative size scaling, or any unlisted number such as duration, cooldown, chance, or spawn count. Projectile size is a special-bullet effect and applies only when the item actually requires it.

## Module Analysis And Confirmation

Before implementation, inspect comparable current modules and classify every nontrivial effect:

- Use a direct `StatManager` change for a persistent stat or state change with no event or repeated execution.
- Propose a trigger module for an effect that reacts to a defined gameplay event.
- Propose a recursive module for an effect that runs continuously or on a periodic/tick schedule.
- Check whether an existing module is a complete semantic match before reusing it. Do not treat a partial match as equivalent.

For every proposed trigger or recursive module, ask the user to confirm a module-effect specification before coding, including: whether to reuse or create a module; intended reusable scope; trigger event or execution cadence; exact conditions and exclusions; affected entity, block, item, or projectile; target selection and range; numeric values; chance/cooldown/duration; stacking and duplicate-item behavior; first obtain/obtain/remove behavior; cleanup or inverse behavior; server/client responsibility; and bullet-trigger interaction when applicable.

Explain the proposed module kind and its reason. If any of those behaviors are unknown, do not infer them from a name or a partial Wiki description. Ask detailed follow-up questions until the effect is implementable. A new module still requires confirmation of its technical name after its behavior is agreed.

## Implement The Passive Item

- Append the new constant to `ItemId`; its numeric ID derives from `ordinal()`, so never insert or reorder existing constants. Use the original item level.
- Put the ability in the correct `registries/ability/passive/impl` location and register it in `ModPassiveAbility`.
- Bind it in `ModPassiveItems` and add the static registry object to the existing passive-item list so existing datagen produces the model and tags.
- Use `StatManager` for stat changes, standardized stat descriptions, trigger modules, recursive modules, set changes, and inverse removal. Add module `Type`, `IExecutableEffect`, and bullet trigger support when the existing pattern requires them.
- Use `StatManager` interfaces for spiritual, homing, piercing, and controllable tear effects. Controllable tears are this project's 3D adaptation of crosshair-style item behavior, not an original Isaac tear type; confirm their detailed behavior through the module specification before implementing them.
- Determine whether the item changes projectile color. If it does, register the corresponding `bullet_color` entry using the current project pattern. Follow an explicitly documented game color priority; otherwise ask the user to choose the priority before implementing it.
- Put health recovery and spawned drops in first obtain. Preserve the existing obtain lifecycle order unless a different order is necessary for correctness, safety, or initialization; explain why and ask first.
- For a missing set, create a no-effect set with a default requirement of three items, but ask the user to choose its name first.
- Default to no `ItemStack` data. If persistent per-stack data is needed, stop and propose a focused plan for that single item before editing.
- Do not add inline or Javadoc comments to ordinary overrides or inherited-method implementations. Add concise inline comments only for genuinely complex logic; write Javadoc only for reusable utility methods.

## Resources And Text

- Use only the English Wiki for images. Obtain the Repentance icon, verify it is a PNG, and save it under the confirmed technical texture name. Do not fall back to another game version; report a missing Repentance icon instead.
- Request all non-image assets from the user, including sound, animation, model, particle, or UI assets. Do not download or substitute them.
- Follow the existing passive-item localization format. Insert new entries immediately before `"item end": ""`; use existing `StatManager` text helpers for common wording and create translations only for unique effects.

## Item Pools And Validation

- Add the item directly to every applicable Repentance+ pool in `src/main/resources/data/isaac_disaster/loot_tables/pools/item/`. Use the established JSON entry format and do not implement weights.
- Run `runData` by default after implementation. For a batch, finish all code and image work first, then run it once and investigate generated-model, tag, or data errors.
- Do not run compilation or `build` as part of this skill.

## Stop And Ask

Stop for user direction when an effect needs a new asset, special-character naming, module classification or behavior clarification, a new module name, a new set name, a changed lifecycle order, `ItemStack` data, any gameplay-affecting number, a special Minecraft-only field, Repentance+/Wiki disagreement, or missing Repentance image/pool information.
