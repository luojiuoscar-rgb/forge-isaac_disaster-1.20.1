---
name: isaac-disaster-item-creation
description: Use when creating or modifying a passive collectible item for the Isaac Disaster Forge 1.20.1 project, including its Repentance behavior, texture, ability, localization, item ID, and item-pool entry. Do not use for active items, trinkets, cards, pills, or other pickups.
---

# Isaac Disaster Passive Item Creation

Implement passive items as an item instance plus a passive ability. Read current project patterns; write user-facing output in Chinese while preserving English names, paths, and Wiki facts.

## Plan Mode

- Require Plan Mode before changing mod code. Outside it, say so and do not implement.
- Plan each batch item separately. Use concise interactive questions when available; otherwise ask concise text questions.
- Put an explicit note at the very beginning of every generated plan that execution must also use this skill. During execution, follow the approved plan for scope and order while continuing to apply this skill's implementation and validation rules; the plan does not replace the skill.

## Per-Item Planning

1. Before any Wiki or project search, resolve identity from the user name, Chinese and English Wiki page names, and the Wiki original item ID. Keep these fields separate and label them explicitly: Wiki original item name, Wiki original ID, mod `ItemId`, registry ID, Java name, texture name, and localization key.
2. If identity is ambiguous or names conflict, ask in Chinese for the intended Wiki page and its Wiki original ID; show at most three candidates with their names, Wiki original IDs, and differences. Do not infer a mapping from a similar English technical name. For example, never automatically map the Chinese name "地狱契约" to `THE_PACT`; ask the user to confirm the Wiki page and Wiki original ID first.
3. The Wiki original ID is used only to identify and cross-check the Isaac item. It must never be written as, substituted for, or used to generate the mod `ItemId`; new mod IDs still append to `manager/id/ItemId` and use its `ordinal()` value without inserting or reordering constants.
4. Use the confirmed identity for every later step, then check the confirmed item in `ItemId`, item/ability registries, localization, textures, and pools. For a creation request, report an existing item and skip it; an explicit modification request continues.
5. Read comparable project patterns and both Chinese and English Wikis for Repentance behavior, every gameplay value, level, and Repentance+ pools. Treat neither Wiki as the sole numeric authority. Compare their numeric values, stat categories, and conversion meaning; when either source conflicts, omits a value, or leaves a value ambiguous (including fire rate versus fire-rate modifier), show the difference and ask the user before choosing an implementation. Use Repentance by default; also report and ask about Repentance+ behavior changes.
6. For non-stat behavior only when it may reduce new code, run a bounded `rg` search using a few event/effect/target/cadence keywords. Inspect snippets first, read only relevant ability/module/helper/context files, and keep at most three candidates. If a candidate is useful, ask before using it, stating similarity, reusable structure, and semantics not to copy; otherwise continue without blocking.
7. Perform the mandatory lore layout check before finalizing the item specification: inspect every descriptive sentence at its rendered length, split overlong lore into two or more ordered short sentences or translation components, and record either the split result or `no split needed`. Do not skip this check because the sentence appears short; keep numbers with units, probabilities with their effects, and conditions with the effects they control.
8. Present a Chinese item specification and confirm all unresolved decisions before moving to the next item. Include separate fields for Wiki original name/ID, mod technical name, next mod `ItemId` and its `ordinal()` source, level, effects and ratios, modules, color, lifecycle, set, assets, localization, pools, dynamic descriptions, lore layout-check result, and approved reference or `none`.

## Confirmation And Modules

- Confirm every gameplay number, special-character technical name, module name, Minecraft-only field, color priority, set name, missing asset, persistent `ItemStack` data, and Wiki disagreement. Do not choose balance values independently.
- Use direct `StatManager` changes for persistent stats, trigger modules for events, and recursive modules for repeated behavior. Reuse only complete semantic matches; for each proposed module confirm scope, event/cadence, conditions, targets, values, chance/cooldown/duration, stacking, lifecycle/cleanup, side, and bullet interaction.
- When a trigger or recursive module represents stacked effect strength through `ContextKey.AMPLIFIER`, prefer `amplifier` in module names and related identifiers instead of `stacks` or `stack`. This distinguishes effect amplification from item `ItemStack` stacking; keep existing project names when extending an established API unless a rename is necessary.
- When creating an `IExecutableEffect`, inspect the existing trigger types first and reuse an existing type by default. If the effect appears to require a new trigger type because no current type expresses its event or execution semantics accurately, state that finding and ask for approval before creating one.
- When an effect applies a `MobEffect` to the current entity or a target entity, prefer the existing `PotionProfile -> ContextKeys.POTIONS -> ModExecutableEffects.POTIONS` pipeline. Inspect and reuse its established representation for potion type, duration, amplifier, target, and removal semantics; propose an alternative only when this pipeline cannot express the confirmed effect, and ask before introducing new plumbing.
- If modules cannot express an effect accurately, do not distort it. Use isolated ability logic when sufficient; otherwise propose the focused shared-architecture change and wait for approval.
- Put recovery and spawned drops in first obtain. Preserve lifecycle order unless a confirmed change is necessary. Missing sets are no-effect sets with three items by default.

## Compatibility Reference List

- Chance-based projectile modifiers: For an item that can probabilistically attach an effect to tears, such as Mom's Eye Shadow, plan an explicit compatibility path so player-generated attack projectiles can also roll the confirmed chance and receive the effect. Reuse the existing bullet-trigger and `StatManager` path; confirm roll timing, eligible projectile sources, one-roll-per-projectile behavior, stacking, color, and interactions before implementation. Do not assume every projectile or attack source is eligible when the Wiki or project semantics say otherwise. Display the chance dynamically: follow `MomsEyeshadow` by using the same chance-calculation method as the trigger, converting it to a percentage, and passing `DescriptionHelper.dynamicNumber(basePercent, currentPercent)` to a `%s%%` lore translation. Never hardcode a probability that can change with player state.
- Treat this list as a maintained reference of effect categories, typical cases, and implementation cautions. Use entries to guide targeted reuse searches, not as permission to copy an implementation or alter confirmed behavior without approval.

## Ratios And Description Values

| Effect | Ratio or default |
| --- | --- |
| Red-heart health | 1 red heart = 1 health unit |
| Speed | 0.2 = 1 speed unit |
| Damage | 1.5 = 1 damage unit |
| Luck | 1 = 1 luck unit |
| Size | 0.2 = 1 size unit; additive scaling |
| Range | 1.5 = 1 range unit |
| Fire rate | 0.7 = 1 fire-rate unit |
| Fire-rate modifier | 1 = 1 modifier unit |
| Shot speed | 0.2 = 1 shot-speed unit |
| Pill quality | better = +1; worse = -1 |
| Flight time | flight description = +1 unit |
| Projectile count | follow original behavior |

- Ask before entity/block reach, attack speed, block breaking, knockback, projectile size, multiplicative size, or any unlisted duration, cooldown, chance, or spawn count.
- Treat fire rate and fire-rate modifier as different attributes. Map fire rate to `TEARS` and fire-rate modifier to `TEARS_CORRECTION`; never equate, merge, or substitute one for the other even when Wiki wording is ambiguous.
- Use concise lore wording and minimal punctuation. Keep punctuation only when needed for grammar, placeholders, values, or unambiguous meaning; do not add decorative sentence-ending marks or separators.
- Show explicit numeric descriptions and known probabilities as percentages. Do not invent unknown percentages or hardcode dynamic values in translations.
- Describe the ordinary item tooltip for one copy of the item only. If the item has both single-copy and stacking effects, show only the single-copy effect; keep stacking values, per-copy wording, multipliers, counts, and duplicate-item totals hidden. This does not remove explicit set requirements or set-synergy descriptions.
- Apply the mandatory lore layout check before adding localization. If a line is too long for the item tooltip, split it at a natural semantic boundary into two or more ordered description components or translation entries so the display stays compact.
- Use `StatManager.<STAT>.description(...)` so displayed stat values match server behavior. For every health-recovery description, use the completed `StatManager.healHealthDescription(ratio)` method; do not recreate its calculation or write a separate recovery-value placeholder.

## Implement Approved Items

- Append `ItemId`; its `ordinal()` is the mod ID, never the Wiki ID. Never insert or reorder constants; use original level.
- Add the ability under `registries/ability/passive/impl`, register it in `ModPassiveAbility`, bind it in `ModPassiveItems`, and add its static object to the passive-item list for datagen.
- Use `StatManager` for stats, descriptions, modules, sets, inverse removal, spiritual/homing/piercing/controllable tears, and required `Type`, `IExecutableEffect`, or bullet trigger support. Preserve the distinct `TEARS` and `TEARS_CORRECTION` fields when implementing fire-rate effects.
- Register `bullet_color` when the item changes projectile color. Default to no `ItemStack` data; use it only after the approved focused plan.
- Add concise inline comments only for genuinely complex logic and Javadoc only for reusable utility methods.

## Resources, Text, Pools, And Validation

- Obtain only the 32x32-pixel PNG Repentance icon from the English Wiki, verify both PNG format and dimensions, and save it under the confirmed texture name. Use the same 32x32 PNG requirement for active-item icons if active-item support is added later. Report missing icons; request all non-image assets from the user.
- Insert localization before `\"item end\": \"\"`; use `StatManager` text for common wording and add translations only for unique effects. Add every Repentance+ pool in `src/main/resources/data/isaac_disaster/loot_tables/pools/item/` without weights.
- After every item is confirmed, present one Chinese batch plan and wait for approval. Implement approved items one at a time. Run `runData` once per single item or once after a batch; inspect generated-data errors. Do not compile or run `build`.
- Report changed files, reused/new modules, images, pools, `runData`, and missing non-image assets in Chinese.
