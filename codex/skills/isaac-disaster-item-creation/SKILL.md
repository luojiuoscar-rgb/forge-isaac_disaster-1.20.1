---
name: isaac-disaster-item-creation
description: Use when creating or modifying a passive collectible item for the Isaac Disaster Forge 1.20.1 project, including its Repentance behavior, texture, ability, localization, item ID, and item-pool entry. Do not use for active items, trinkets, cards, pills, or other pickups.
---

# Isaac Disaster Passive Item Creation

Implement passive items as an item instance plus a passive ability. Read relevant current source patterns rather than assuming APIs or architecture. Write specifications, questions, plans, and reports in Chinese; preserve English names, identifiers, paths, and Wiki facts.

## Entry And Approval

- Planning item creation or modification requires Plan Mode. If asked to implement without an approved plan, request Plan Mode before modifying mod code.
- Once the user approves a plan, implement it in execution mode while continuing to use this skill; do not require a return to Plan Mode solely to execute approved work.
- Begin every generated plan with: “执行此计划时也必须使用 `isaac-disaster-item-creation` skill，并遵守其实现与验证规则。”
- Treat explicit decisions in the current request, earlier item confirmations, and the approved plan as settled. Ask only about unresolved fields. During implementation, return only the affected item to confirmation if a new material fact changes its specification; preserve all other approvals.

## Workflow

For both single-item and batch tasks, complete steps 1–6 for one item before starting the next.

1. **Resolve identity.** Search the Chinese Wiki first, considering page names, aliases, and redirects; then cross-check the English page and Wiki original ID. If identity remains ambiguous, ask for the intended page/ID and show at most three candidates with differences. Never infer “地狱契约” means `THE_PACT`. Clearly separate Wiki original name/ID from mod `ItemId`, registry ID, Java name, texture name, and localization key.
2. **Check existence.** Use the resolved identity to check `ItemId`, item/ability registries, localization, textures, and pools. For creation, report an existing item and skip further research/planning. An explicit modification request continues.
3. **Research behavior.** Consult both [Chinese Wiki](https://isaac.huijiwiki.com/wiki) and [English Wiki](https://bindingofisaacrebirth.fandom.com/wiki/Binding_of_Isaac:_Rebirth_Wiki) for effects, values, level, and synergies. Default to Repentance behavior; ask about Repentance+ behavior changes. Neither Wiki is the sole numeric authority: show conflicts, omissions, ambiguous values or stat categories, then ask. Item pools follow Repentance+.
4. **Assess implementation.** Determine stats, module types, lifecycle, assets, and relevant synergies using the technical rules below. Optional reuse search: pure-stat items normally skip it; for complex behavior, use a few event/effect/target/cadence keywords with `rg`, inspect snippets first, and read only relevant files. Keep at most three candidates from abilities, modules, helpers, `StatManager`, contexts, or lifecycle patterns. Ask before adopting a candidate, explaining similarity, reusable parts, and semantics not to copy. Finding no useful candidate does not block work; do not scan all source files.
5. **Check lore layout.** Draft short sentences/components using the description rules below. Record the split result or “无需拆分” for this item's specification; update values and wording after confirmation if needed.
6. **Confirm the item.** Present its Chinese specification and ask only unresolved questions through concise interactive windows when available, otherwise concise text. Keep each item's questions separate. Confirm all gameplay values with proposed ratios, module names and behavior, synergies, special-character technical names, MC-only attributes, projectile-color priority (unless explicitly specified by the game), set names, missing assets, persistent `ItemStack` data, and Wiki disagreements. Do not choose balance independently. Include the final lore lines and layout-review result before completing this item's specification.

After all items are confirmed:

7. **Submit one approval plan.** List each item separately: Wiki original name/ID; mod technical name, `ItemId` and its `ordinal()` source; level; numeric changes and ratios; new/reused Ability, trigger/recursive modules, bullet triggers and `bullet_color`; lifecycle; set; textures and other asset status; localization and final lore lines; pools; approved references or “无”; synergy decisions and implementation approach. Wait for explicit approval before implementing.
8. **Implement sequentially.** Complete each approved item's texture, ability/modules, registrations, localization, and pools before starting the next. Follow the implementation rules below.
9. **Verify and report.** Apply the final checks below, then report in Chinese.

## Stats And Descriptions

Use `StatManager` interfaces for stat changes and ratio conversion, standardized descriptions, module/set addition and inverse removal, and spiritual/homing/piercing/controllable tears. Controllable tears are this mod's adaptation of effects such as the crosshair item, not an original Isaac tear type.

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

- These conversions guide proposals; gameplay values still require confirmation. Ask about entity/block reach, attack speed, block breaking, knockback, projectile size, multiplicative size, and unlisted durations, cooldowns, chances, or spawn counts.
- Fire rate is `TEARS`; fire-rate modifier is `TEARS_CORRECTION`. Never equate, merge, or substitute them, including when Wiki wording is ambiguous.
- Use `StatManager.<STAT>.description(...)` for stat text and `StatManager.healHealthDescription(ratio)` for every health-recovery description. Use existing standardized wording for common effects and item requirements; add translations only for unique effects. Keep displayed values tied to actual server configuration through existing methods/placeholders.
- Use concise wording and minimal punctuation; retain punctuation needed for grammar, placeholders, values, or meaning. Show known probabilities as percentages. Never invent missing values or hardcode dynamic ones in translations.
- Ordinary tooltips describe one copy only. Hide stacking/per-copy multipliers, duplicate totals, and extra stacking effects. Explicit set requirements and set synergies remain describable.
- Lore layout is a required planning and localization check: inspect the current tooltip/component assembly and compare with relevant existing short lore. Review all supported translations and plausible dynamic-value substitutions. Split long or multi-clause sentences at natural semantic boundaries into ordered components/translation entries; keep numbers with units, chances with effects, and conditions with their governed effects. Show final lines in the plan and check the implemented component order matches them. Use font-width measurement or a screenshot if available; otherwise report static text review and do not claim rendered-width validation. Do not invent a universal character limit.

## Modules, Lifecycle, And Synergies

- Use direct stat changes for persistent stats, trigger modules for events, and recursive modules for repeated behavior. Confirm each module's scope, event/cadence, conditions, targets, values, chance/cooldown/duration, amplification, lifecycle/cleanup, logical side, and projectile interaction. New module technical names require confirmation; reusable effects may need names different from the item.
- Place abilities under `registries/ability/passive/impl` and modules in their corresponding existing `impl` folders. Reuse established `Type`, `IExecutableEffect`, contexts, and bullet-trigger support when their semantics match. If modules cannot express the effect, propose isolated ability logic or a focused shared-architecture change; confirm the approach before implementation.
- New trigger/recursive modules define `private static final CompositeTrigger TRIGGER = ...;` at class level and have constructors call `super(TRIGGER);`. Do not build or inline trigger graphs in constructors.
- For effect strength carried by `ContextKeys.AMPLIFIER`, prefer `amplifier` over `stacks`/`stack` in new identifiers. Preserve established API names unless a rename is necessary.
- Before creating an `IExecutableEffect`, inspect existing trigger types. Reuse them by default; if a new type is needed to express the effect accurately, explain why and ask before introducing it.
- Plan each new `IAbilityEffect.applyEffect(...)` boolean contract: `true` means safe completion, including confirmed acceptable no-ops (ordinary ineligibility, empty input, intentionally skipped derived work); `false` means a required context, event/entity/target type, nested effect, or safe execution condition is invalid or missing. Decide per effect which absences are acceptable. Handle chance in the trigger; a missed roll is not an execution error. Do not swallow unexpected exceptions as `false`. `IExecutableEffect.apply(...)` returns `void`; inspect the current caller before claiming fallback behavior. The previously inspected `IAbilityEffect.apply(...)` fallback branch was unfinished; request separate approval for new fallback infrastructure.
- For `MobEffect` application, prefer `PotionProfile -> ContextKeys.POTIONS -> ModExecutableEffects.POTIONS`; inspect its duration, amplifier, target, and removal semantics. Ask before introducing an alternative when this pipeline cannot express the approved effect.
- Put obtain-time recovery and spawned drops in `handleFirstObtain`. Preserve the current lifecycle order (currently `handleObtain` before `handleFirstObtain`); ask with reasons if a change is necessary for correctness, safety, or semantics. Revive items explicitly declare lifecycle ownership and remove both their revive `provider` and `consumer` in `handleRemove`, without leaving item-owned state behind.
- If a required set is absent, confirm its name and register a no-effect set requiring three items by default; grant/remove membership through `StatManager`.
- Inspect Wiki synergies with partners already implemented in the mod using targeted registry/code checks. Explain the original interaction and whether existing code naturally supports it, special implementation is needed, or MC adaptation is needed; cite the relevant code path for natural compatibility. Confirm retaining, adding, modifying, or excluding each relevant synergy and record the decision per item. Do not implement missing partner items or silently add special cases.

### Compatibility Reference

For probabilistic tear modifiers such as Mom's Eye Shadow, also plan compatibility so player attacks can probabilistically apply the effect. Confirm eligible attack/projectile sources, roll timing, one-roll-per-projectile behavior, amplification, color, and interactions; inspect the current `MomsEyeshadow` implementation rather than assuming all attacks are eligible. Reuse the bullet-trigger/`StatManager` path. Display dynamic chance using the trigger's chance-calculation method, percentage conversion, `DescriptionHelper.dynamicNumber(basePercent, currentPercent)`, and a `%s%%` translation. This reference guides approved reuse; it does not authorize copying or changing behavior.

## Registration And Assets

- Append new entries to `manager/id/ItemId`; the mod ID is `ordinal()`, never the Wiki original ID. Never insert or reorder constants. Use the confirmed original level.
- Register the passive ability in `ModPassiveAbility`, bind it in `ModPassiveItems`, and add the static item object to the existing datagen list. Do not manually create item models; `runData` generates them.
- Register `bullet_color` if projectile color changes. Default to no extra `ItemStack` data; if custom persistent data is needed, pause that item, confirm a focused plan, and implement one such item at a time.
- For mod resource namespaces use `IsaacDisaster.MOD_ID`, e.g. `ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "path")`; preserve required vanilla/external namespaces.
- Do not add comments or Javadoc to routine inherited-method implementations. Add concise inline comments only for complex logic and Javadoc for reusable utility methods.
- Download Repentance item icons only from the English Wiki: actual PNG format, exactly `32x32`. `Collectible_` source names are a useful clue, not proof; verify item identity and version, then save under the confirmed project texture name. Confirm names containing special characters before normalizing them. The same icon contract applies if active-item support is added later.
- Request non-image assets such as sounds from the user. If an icon download or format/size check fails, record the item, concrete reason, and missing file status; report it explicitly at the end. Never silently omit failed textures or present partial asset work as complete.
- Insert localization entries before `"item end": ""`. Add the item to every corresponding Repentance+ pool in `src/main/resources/data/isaac_disaster/loot_tables/pools/item/` without weights.

## Final Verification And Report

- Check each completed item against its approved specification: IDs/level, item and ability registrations/binding, module/Type/executable-effect registrations, obtain/remove symmetry, revive provider/consumer cleanup when applicable, approved synergies, tooltip values/order, textures, and all corresponding pools. Verify claimed natural synergies through their code paths; distinguish static checks from observed runtime behavior.
- Run `runData` once after a single item or once after all items in a batch are ready. Check exit status and generated model/tag/data errors. Do not separately run compilation or `build`. If assets or another required step remain blocked, report what is incomplete and the actual `runData` status.
- Report actual changed files, reused/new modules, synergy outcomes, image sources/status, pools, lore review, `runData` results, failed texture downloads and reasons, missing non-image assets, and remaining work in Chinese. Do not claim unperformed runtime or visual validation.
