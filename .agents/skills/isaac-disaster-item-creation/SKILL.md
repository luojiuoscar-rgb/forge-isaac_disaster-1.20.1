---
name: isaac-disaster-item-creation
description: Use when creating or modifying a passive collectible item for the Isaac Disaster Forge 1.20.1 project, including its Repentance behavior, texture, ability, localization, item ID, and item-pool entry. Do not use for active items, trinkets, cards, pills, or other pickups.
---

# Isaac Disaster Passive Item Creation

Implement each passive item as an item instance plus a passive ability. Read current project patterns before editing; do not treat this skill as a frozen code map. Write all user-facing specifications, questions, plans, and reports in Chinese; preserve English names, identifiers, paths, and Wiki facts verbatim.

## Require Plan Mode

- Require the user to enable Plan Mode before creating or modifying any passive item. If asked to implement directly outside Plan Mode, say that this skill requires Plan Mode; do not modify mod code.
- In Plan Mode, handle a single item and every item in a batch through the same per-item loop. Do not mix their values, modules, assets, or questions.
- Use the platform's interactive input window for concise unanswered questions when available. Otherwise ask a concise text question. Never stop with an unstructured list of confirmations.

## Plan Each Item

For the current item only:

1. Read current comparable implementations: `ItemId`, `ModPassiveItems`, `ModPassiveAbility`, `registries/ability/passive/impl`, `StatManager`, existing modules and set abilities, localization, and target pool JSON files.
2. Compare the Chinese Wiki (`https://isaac.huijiwiki.com/wiki`) and English Wiki (`https://bindingofisaacrebirth.fandom.com/wiki/Binding_of_Isaac:_Rebirth_Wiki`) for Repentance behavior, values, level, and Repentance+ pool membership. Use Repentance by default. If Repentance+ changes behavior, or the Wikis conflict or omit a needed fact, show the difference and ask before coding.
3. Prepare a Chinese item specification summary containing: original behavior; registry, texture, Java, and module names; next `ItemId` and level; each numeric value and ratio; ability and module classification; tear behavior and color; lifecycle; set; image and other assets; localization; and pools.
4. Ask concise follow-up questions only for this item's unresolved or user-controlled fields. Confirm every gameplay-affecting number, every module behavior and technical name, special-character naming, Minecraft-only fields, color priority, `ItemStack` data, set name, missing assets, and Wiki conflicts. Complete this item's confirmation before researching the next batch item.

Classify nontrivial behavior before asking:

- Use a direct `StatManager` change for persistent state or stats without an event or repeated execution.
- Propose a trigger module for a defined gameplay event and a recursive module for continuous or periodic execution.
- Reuse a module only for a complete semantic match. Otherwise explain whether the proposed module is item-specific or reusable.
- For every trigger or recursive module, confirm reuse versus creation, reusable scope, event or cadence, conditions and exclusions, target and range, values, chance/cooldown/duration, stacking, lifecycle and cleanup, server/client ownership, and bullet-trigger interaction. Do not infer unknown behavior from a name or partial Wiki description.
- If existing or reasonably new modules cannot express the effect accurately, do not distort the effect to force module reuse. First determine whether isolated item-specific logic in the passive ability is sufficient. If it needs a shared executor, manager, event hook, persistent data, or other shared architecture, stop and add a focused single-item implementation proposal to its specification: why modules do not fit, ownership, integration points, lifecycle/state, and validation. Wait for user approval before coding.

## Numeric Ratios

Use `StatManager` interfaces and show every value in the item specification as original value, ratio conversion, and proposed Minecraft value. Do not choose balance values independently.

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

Ask before entity reach, block reach, attack speed, block-breaking speed, knockback, projectile size, multiplicative size scaling, or any unlisted number such as duration, cooldown, chance, or spawn count. Projectile size is a special-bullet effect and applies only when required.

## Approve The Batch Plan

- After every item has completed its own confirmation loop, present one Chinese implementation plan and wait for explicit user approval before editing mod code.
- List every item separately with its stat changes and ratios; reused and new ability, trigger module, recursive module, bullet trigger, and `bullet_color`; image and non-image asset status; registrations, localization, set, and pools.
- If implementation reveals a new unconfirmed fact, return only to that item's confirmation loop and add the new question. Do not reopen approved items unnecessarily.

## Implement Approved Items

Implement approved items one at a time, in the order of the approved plan:

- Append `ItemId`; its numeric ID derives from `ordinal()`, so never insert or reorder constants. Use the original item level.
- Put abilities in the correct `registries/ability/passive/impl` location, register them in `ModPassiveAbility`, bind them in `ModPassiveItems`, and add static objects to the existing passive-item list for datagen.
- Use `StatManager` for stat changes, standardized descriptions, trigger modules, recursive modules, set changes, and inverse removal. Add module `Type`, `IExecutableEffect`, and bullet trigger support when the current pattern requires them.
- Use `StatManager` interfaces for spiritual, homing, piercing, and controllable tears. Controllable tears are this project's 3D crosshair-style adaptation, not an original Isaac tear type.
- Determine projectile-color changes and register `bullet_color` using current patterns. Follow documented game color priority; otherwise implement only the user-confirmed priority.
- Put health recovery and spawned drops in first obtain. Preserve obtain lifecycle order unless a confirmed change is necessary for correctness, safety, or initialization.
- For a missing set, create a no-effect set with requirement three and the confirmed set name. Default to no `ItemStack` data; persistent per-stack data requires the confirmed focused plan.
- Do not add comments to ordinary overrides or inherited implementations. Add concise inline comments only for genuinely complex logic; write Javadoc only for reusable utility methods.

## Resources, Text, And Pools

- Obtain images only from the English Wiki. Use the Repentance icon, verify PNG, and save it using the confirmed technical texture name. Do not fall back to another version; report a missing icon. Confirm every technical name when the original name contains punctuation or special characters.
- Request every non-image asset from the user, including sound, animation, model, particle, or UI assets. Do not download or substitute them.
- Insert localization immediately before `\"item end\": \"\"`; use `StatManager` text helpers for common wording and add translations only for unique effects.
- Add each item directly to every applicable Repentance+ pool in `src/main/resources/data/isaac_disaster/loot_tables/pools/item/` using the established JSON format without weights.

## Validate And Report

- Run `runData` after a single approved item. For a batch, finish all approved code and image work, then run it once. Check its exit result and investigate generated-model, tag, or data errors.
- Do not compile or run `build`.
- Report in Chinese: changed files, reused and new modules, image source, pools, `runData` result, and non-image assets still required.
