---
name: isaac-disaster-item-creation
description: Use when creating or modifying a passive or active collectible item for the Isaac Disaster Forge 1.20.1 project, including Repentance behavior, abilities, textures, localization, item IDs, item pools, and project-specific interactions. Do not use for trinkets, cards, pills, or other pickups.
---

# Isaac Disaster Item Creation

Use this skill for passive and active collectible items. Read current project code before choosing an API. Write plans, questions, and reports in Chinese; preserve English names, identifiers, paths, and Wiki facts.

## Router

1. Determine whether the request concerns a passive item or an active item.
2. Read exactly one matching branch document before planning or implementation:
   - Passive item: `references/passive-item.md`
   - Active item: `references/active-item.md`
3. If the item type is unclear, ask before continuing. Read both branch documents only when a confirmed synergy or shared architecture crosses the two item types.
4. The branch document is mandatory implementation guidance. Do not infer the missing lifecycle, registration, or resource rules from the other branch.

## Entry And Approval

- Planning item creation or modification requires Plan Mode. If asked to implement without an approved plan, request Plan Mode before modifying mod code.
- Once the user approves a plan, implement it in execution mode while continuing to use this skill and the selected branch document.
- Begin every generated plan with: “执行此计划时也必须使用 `isaac-disaster-item-creation` skill，并遵守其通用规则和对应道具分支规则。”
- Treat explicit decisions in the current request, earlier item confirmations, and the approved plan as settled. Ask only about unresolved fields. If a new material fact changes one item, return only that item to confirmation.

## Workflow

For single-item and batch tasks, complete steps 1–6 for one item before starting the next.

1. **Resolve identity.** Search the Chinese Wiki first, considering names, aliases, and redirects; then cross-check the English page and Wiki original ID. If identity remains ambiguous, ask for the intended page and ID and show at most three candidates. Never infer “地狱契约” means `THE_PACT`. Separate Wiki original name/ID from mod `ItemId`, registry ID, Java name, texture name, and localization key.
2. **Check existence.** Use the resolved identity to check `ItemId`, the selected item and ability registries, localization, textures, and pools. For creation, report an existing item and skip further research/planning. An explicit modification request continues.
3. **Research behavior.** Consult both [Chinese Wiki](https://isaac.huijiwiki.com/wiki) and [English Wiki](https://bindingofisaacrebirth.fandom.com/wiki/Binding_of_Isaac:_Rebirth_Wiki) for Repentance effects, values, level, and synergies. Ask about Repentance+ changes. Neither Wiki is the sole numeric authority: show conflicts, omissions, ambiguous values, or ambiguous stat categories before choosing.
4. **Assess implementation.** Determine stats, modules, lifecycle, assets, and relevant synergies using the selected branch rules. For complex behavior, perform a bounded `rg` search with a few event/effect/target/cadence keywords, inspect snippets first, and read only relevant files. Keep at most three reuse candidates. Ask before adopting a candidate, explaining similarity, reusable parts, and semantics not to copy. Search failure does not block work.
5. **Check lore layout.** Review every description sentence and plausible dynamic-value substitution against the current tooltip assembly. Split long or multi-clause lore at natural semantic boundaries into ordered components or translation entries. Record the result as the final lines or “无需拆分”. Use font measurement or a screenshot when available; otherwise report static review only.
6. **Confirm the item.** Present a Chinese specification and ask only unresolved questions through concise interactive windows when available, otherwise concise text. Keep each item separate. Confirm gameplay values and ratios, modules, synergies, names, MC-only attributes, projectile-color priority, special assets, persistent `ItemStack` data, Wiki disagreements, final lore, and layout result.

After all items are confirmed:

7. **Submit one approval plan.** List every item separately: Wiki original name/ID; mod technical name and `ItemId.ordinal()` source; level; numeric changes and ratios; selected branch lifecycle; new/reused abilities and modules; bullet triggers and `bullet_color`; synergies and their approved handling; assets; localization and final lore; pools; approved references or “无”. Wait for explicit approval before implementation.
8. **Implement sequentially.** Complete each approved item’s texture, branch-specific item and ability, modules, registrations, localization, and pools before starting the next.
9. **Verify and report.** Apply the common and branch-specific checks, then report actual results in Chinese.

## Common Stats And Descriptions

Use `StatManager` interfaces for stat changes, ratio conversion, standardized descriptions, modules, sets, and inverse removal whenever the selected branch requires them.

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

- Gameplay values still require confirmation. Ask about entity/block reach, attack speed, block breaking, knockback, projectile size, multiplicative size, and unlisted durations, cooldowns, chances, or spawn counts.
- Fire rate maps to `TEARS`; fire-rate modifier maps to `TEARS_CORRECTION`. Never equate, merge, or substitute them.
- Use `StatManager.<STAT>.description(...)` for stat text and `StatManager.healHealthDescription(ratio)` for health recovery. Keep displayed values tied to actual server configuration through existing methods or placeholders.
- Use concise wording and minimal punctuation. Show known probabilities as percentages. Never invent missing values or hardcode dynamic values in translations.
- Describe one copy by default. If the game or current project explicitly exposes a stacking mechanic, identify it and confirm whether the stacking effect should be shown; do not apply the passive rule blindly to active items.

## Common Modules, Synergies, And Code Reuse

- Use direct stat changes for persistent stats, trigger modules for events, and recursive modules for repeated behavior. Confirm scope, event/cadence, conditions, targets, values, chance/cooldown/duration, amplification, lifecycle/cleanup, side, and projectile interaction.
- New trigger or recursive modules define `private static final CompositeTrigger TRIGGER = ...;` at class level and have constructors call `super(TRIGGER);`.
- For effect strength carried by `ContextKeys.AMPLIFIER`, prefer `amplifier` over `stacks` or `stack` in new identifiers.
- Before creating an `IExecutableEffect`, inspect existing trigger types and reuse them by default. Ask before introducing a new trigger type.
- Plan each new `IAbilityEffect.applyEffect(...)` boolean contract. `true` means safe completion, including an approved no-op; `false` means a required context, entity, target, nested effect, or safe condition is invalid or missing. A missed chance is not an execution error. Do not swallow unexpected exceptions as `false`; inspect the current caller before claiming fallback behavior.
- For `MobEffect`, prefer `PotionProfile -> ContextKeys.POTIONS -> ModExecutableEffects.POTIONS`. Ask before introducing alternate plumbing.
- If modules cannot express an approved effect, propose isolated ability logic or a focused shared-architecture change and wait for approval.
- Check relevant synergies with partners already implemented in the mod using targeted registry and code checks. Explain the original interaction and whether it is naturally supported, needs special implementation, or needs MC adaptation. Ask whether to retain, add, modify, or exclude each relevant synergy. Record the decision per item. Do not implement missing partner items or silently add special cases.

## Common Registration And Assets

- Append new entries to `manager/id/ItemId`; the mod ID is `ordinal()`, never the Wiki original ID. Never insert or reorder constants.
- Use `IsaacDisaster.MOD_ID` for mod-owned `ResourceLocation` namespaces.
- Do not add comments or Javadoc to routine inherited-method implementations. Add comments only for complex logic and Javadoc for reusable utility methods.
- Download Repentance item icons only from the English Wiki as actual `32x32` PNG files. `Collectible_` is a useful clue, not proof; verify identity and version, then save under the confirmed project texture name. Confirm names containing special characters before normalizing them.
- Do not manually create item models. Add the item to the correct existing datagen list; `runData` generates models and tags.
- Request non-image assets such as sounds from the user. If an icon download or format/size check fails because of network errors, an unavailable Wiki, an invalid link, an HTTP error, or an unexpected format, record the item, concrete reason, and missing-file status and state it explicitly in the final response. Never silently omit a failed texture.
- Insert localization before `"item end": ""`. Add the item to the corresponding project pool and Repentance+ pool entries without weights, following the current project’s tag and loot-table structure.

## Common Validation And Report

- Check the approved specification against IDs/level, selected branch registrations and lifecycle, module and executable-effect registrations, approved synergies, tooltip values/order, textures, and pools.
- Run `runData` once after a single item or once after all items in a batch are ready. Check exit status and generated model/tag/data errors. Do not run compilation or `build`.
- Report actual changed files, reused/new modules, branch lifecycle, synergy outcomes, image source/status, pools, lore review, `runData` result, failed texture downloads and reasons, missing non-image assets, and remaining work in Chinese. Do not claim unperformed runtime or visual validation.
