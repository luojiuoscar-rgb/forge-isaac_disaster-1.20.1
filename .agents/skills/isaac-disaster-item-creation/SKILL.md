---
name: isaac-disaster-item-creation
description: Use when creating or modifying a passive item, active item, or trinket for the Isaac Disaster Forge 1.20.1 project, or when planning or reviewing pickup behavior and preparing card textures. Pickup guidance currently covers confirmed texture and workflow-review constraints, not a complete card or pill creation procedure.
---

# Isaac Disaster Item Creation

Use this skill for passive items, active items, and trinkets. Read current project code before choosing an API. Write plans, questions, and reports in Chinese; preserve English names, identifiers, paths, and Wiki facts.

## Router

1. Determine whether the request concerns a passive item, active item, or trinket.
2. Read exactly one matching branch document before planning or implementation:
   - Passive item: `references/passive-item.md`
   - Active item: `references/active-item.md`
   - Trinket: `references/trinket.md`
   - Pickup planning/review or card texture work: `references/pickups.md` (confirmed constraints only; the complete pickup creation workflow is not yet defined).
3. If the item type is unclear, ask before continuing. Read additional branch documents only when the current item's synergy or shared architecture requires them; do not load every branch by default. In mixed batches, select the matching branch separately for each item.
4. The branch document is mandatory implementation guidance. Do not infer the missing lifecycle, registration, or resource rules from the other branch.
5. For pickups, use the partial branch for its confirmed rules. Do not apply collectible/trinket ID, quality, lifecycle, localization, or pool requirements by analogy; establish the missing contracts from current code and user-confirmed behavior during planning.

## Entry And Approval

- Planning item creation or modification requires Plan Mode. If asked to implement without an approved plan, request Plan Mode before modifying mod code.
- Once the user approves a plan, implement it in execution mode while continuing to use this skill and the selected branch document.
- Begin every generated plan with: “执行此计划时也必须使用 `isaac-disaster-item-creation` skill，并遵守其通用规则和对应道具分支规则。”
- Treat explicit decisions in the current request, earlier item confirmations, and the approved plan as settled. Ask only about unresolved fields. If a new material fact changes one item, return only that item to confirmation.
- If the user explicitly authorizes direct implementation of simple items, do not request another plan or confirmation. If the user forbids planning files, do not use `planning-with-files` or create planning artifacts.

## Workflow

For single-item and batch tasks, complete steps 1–6 for one item before starting the next.

1. **Resolve identity.** Search the Chinese Wiki first, considering names, aliases, and redirects; then cross-check the English page and Wiki original ID. If identity remains ambiguous, ask for the intended page and ID and show at most three candidates. Never infer “地狱契约” means `THE_PACT`. Separate Wiki original name/ID and item category from the mod numeric ID (`ItemId` for collectibles, `TrinketId` for trinkets), registry ID, Java name, texture name, and localization key. Wiki collectible and trinket IDs belong to different categories.
2. **Check existence.** Use the resolved identity to check the matching ID enum (`ItemId` or `TrinketId`), item and ability registries, localization, textures, and pools. For creation, report an existing item and skip further research/planning. An explicit modification request continues.
3. **Research behavior.** Consult both [Chinese Wiki](https://isaac.huijiwiki.com/wiki) and [English Wiki](https://bindingofisaacrebirth.fandom.com/wiki/Binding_of_Isaac:_Rebirth_Wiki) for Repentance effects, values, level, and synergies. Ask about Repentance+ changes. Neither Wiki is the sole numeric authority: show conflicts, omissions, ambiguous values, or ambiguous stat categories before choosing.
4. **Assess implementation.** Determine stats, modules, lifecycle, assets, and relevant synergies using the selected branch rules. For complex behavior, perform a bounded `rg` search with a few event/effect/target/cadence keywords, inspect snippets first, and read only relevant files. Keep at most three reuse candidates. Ask before adopting a candidate, explaining similarity, reusable parts, and semantics not to copy. Search failure does not block work.
5. **Check lore layout.** Review every description sentence and plausible dynamic-value substitution against the current tooltip assembly. Split long or multi-clause lore at natural semantic boundaries into ordered components or translation entries. Record the result as the final lines or “无需拆分”. Use font measurement or a screenshot when available; otherwise report static review only.
6. **Confirm the item.** Present a Chinese specification and ask only unresolved questions through concise interactive windows when available, otherwise concise text. Keep each item separate. For each stat, show the Wiki original value, proposed project ratio, and configuration-based actual effect as separate fields. Confirm the ratio as the item's balance decision, along with modules, synergies, names, MC-only attributes, projectile-color priority, special assets, persistent `ItemStack` data, Wiki disagreements, final lore, and layout result.

After all items are confirmed:

7. **Submit one approval plan.** List every item separately: category and Wiki original name/ID; mod technical name and matching ID enum's `ordinal()` source; level; numeric changes and ratios; selected branch lifecycle; new/reused abilities and modules; bullet triggers and `bullet_color`; synergies and their approved handling; assets; localization and final lore; pools; approved references or “无”. Include ordinary/enhanced behavior and swallowed-state handling for trinkets. Wait for explicit approval before implementation.
8. **Implement sequentially.** Complete each approved item’s texture, branch-specific item and ability, modules, registrations, localization, and pools before starting the next.
9. **Verify and report.** Apply the common and branch-specific checks, then report actual results in Chinese.

## Common Stats And Descriptions

Use `StatManager` interfaces to apply confirmed project ratios and generate descriptions. Distinguish three quantities: the Wiki original value (research reference), the project ratio (item strength selected for this mod), and the actual Minecraft attribute change (computed by `StatManager` using current configuration).

The following table proposes project ratios from original-game effects. It does not define Minecraft attribute values or the configured size of a project unit. For numeric rows, divide the Wiki value only by the explicitly listed original-game reference amount when proposing a ratio; preserve the sign. Confirm the proposal with the user before implementation.

| Effect | Original-game reference | Proposed project ratio |
| --- | --- | --- |
| Red-heart health | 1 red heart | 1 |
| Speed | +0.2 speed | +1 |
| Damage | +1 damage | +1 |
| Luck | +1 luck | +1 |
| Size | +0.2 size | +1; use additive scaling |
| Range | +1.5 range | +1 |
| Original Isaac “射速” | +0.7 tears | +1 to `TEARS` |
| Original Isaac “射速修正” | +1 fire-rate correction | +1 to `fire_rate` |
| Shot speed | +0.2 shot speed | +1 |
| Pill quality | better / worse pills | +1 / -1 |
| Flight time | flight effect description | +1 |
| Projectile count | original behavior | confirm original count behavior |

- Once approved, the project ratio is independent of `Config` bonus values. For additive `StatManager.apply`, the actual attribute change is `ratio * getBonus()`; changes to configuration scale the effect without changing the item's ratio. Inspect specialized interfaces for their own semantics.
- Never divide a Wiki value by the mod's configured per-unit bonus to preserve the original absolute value. Do not derive ratios by dividing by `getBonus()` or use inverse scaling to cancel the user's balance configuration. Only an explicit user decision to target a fixed Minecraft value may override this policy.
- Example: Wiki damage +1 proposes `ratio = 1`. Use `StatManager.DAMAGE.apply(player, 1)` and `StatManager.DAMAGE.description(1)`. With a configured damage bonus of 1.5, this grants +1.5; with 2.0, it grants +2.0. Do not replace the ratio with `1 / 1.5`, `1 / 2.0`, or `1 / getBonus()`. Wiki damage +0.5 proposes `ratio = 0.5`; do not assume every attribute's Wiki +1 means one ratio.
- Use the same confirmed ratio for effect application and its description. Show configuration-derived values through existing description methods; do not hardcode 1.5 or another per-unit Minecraft bonus in the skill's conversion table or translations.
- For fixed attribute bonuses added through `StatManager.<STAT>.apply(...)`, write the confirmed ratio directly as a numeric literal, e.g. `StatManager.DAMAGE.apply(player, 1)`. Do not introduce an extra scalar variable or constant solely to hold that fixed number. Use the matching literal in descriptions and, for removable bonuses, its negative in removal. This rule concerns fixed bonuses; genuinely dynamic calculations may use the existing calculated value.

- Gameplay values still require confirmation. Ask about entity/block reach, attack speed, block breaking, knockback, projectile size, multiplicative size, and unlisted durations, cooldowns, chances, or spawn counts.
- Keep the two original Isaac attributes separate. An original description labeled “射速” maps to `StatManager.TEARS` / `ModAttributes.TEARS`. An original description labeled “射速修正” maps to the mod's `fire_rate` attribute through `StatManager.FIRE_RATE` / `ModAttributes.FIRE_RATE`. Do not translate “射速修正” as “射速”, and do not map it to the removed or unrelated `TEARS_CORRECTION` name.
- Use `StatManager.<STAT>.description(...)` for stat text and `StatManager.healHealthDescription(ratio)` for health recovery. Keep displayed values tied to actual server configuration through existing methods or placeholders.
- Use concise wording and minimal punctuation. Show known probabilities as percentages. Never invent missing values or hardcode dynamic values in translations.
- Describe one copy by default. If the game or current project explicitly exposes a stacking mechanic, identify it and confirm whether the stacking effect should be shown; do not apply the passive rule blindly to active items.
- Ordinary additive stats may stack repeatedly. Apply duplicate suppression only to multiplier systems that define it; do not add `COPYKEY`-style deduplication for normal stats.
- A literal percent after a placeholder must be escaped (`%s%%`, never `%s%`). Verify dynamic lore through Minecraft's real translation parser, not JSON shape alone.

## Common Modules, Synergies, And Code Reuse

- Existing code is evidence of interfaces and call relationships, not the sole authority for intended behavior. Before adopting a reference, check consistency between events, effect execution, consumption, sound, use records, and return values. Report contradictions with the concrete call path, actual behavior, and proposed correction; confirm unresolved semantics rather than preserving a historical defect as a skill requirement. State the impact of any shared-flow repair and include it in the approved plan. Descriptions of current call order are observations to verify, not mandatory ordering rules for new implementations.
- Use direct stat changes for persistent stats, trigger modules for events, and recursive modules for repeated behavior. Confirm scope, event/cadence, conditions, targets, values, chance/cooldown/duration, amplification, lifecycle/cleanup, side, and projectile interaction.
- Judge Event dependence by semantics: retain concrete Event checks for attack-plan, loot-generation, and hurt-only effects; allow entity/player/target effects to run from other events when their required context is present. Do not replace required Event data with ContextKeys merely to decouple code.
- Separate count-based proc progress from the reusable payoff effect, as in `LeadPencilShotCounter` / `LeadPencilBurst` and `JumperCablesKillCounter` / `JumperCables`. The counter owns event eligibility, source checks, persisted progress, threshold/reset logic, and progress cleanup. The payoff owns only its actual effect and required inputs; do not require the original event, item ownership, or counter state unless the effect itself semantically requires them. Reset progress before invoking the payoff, propagate its execution result, and verify that direct or cross-event invocation does not advance or consume progress.
- Add a ContextKey only for data with real cross-event meaning. Remove keys that merely mirror one specific Event. Do not force extra helpers, profile factories, or copy abstractions when callers can construct the value directly.
- New trigger or recursive modules define `private static final CompositeTrigger TRIGGER = ...;` at class level and have constructors call `super(TRIGGER);`.
- For effect strength carried by `ContextKeys.AMPLIFIER`, prefer `amplifier` over `stacks` or `stack` in new identifiers.
- Before creating an `IExecutableEffect`, inspect existing trigger types and reuse them by default. Ask before introducing a new trigger type.
- Plan each new `IAbilityEffect.applyEffect(...)` boolean contract. `true` means safe completion, including an approved no-op; `false` means a required context, entity, target, nested effect, or safe condition is invalid or missing. A missed chance is not an execution error. Do not swallow unexpected exceptions as `false`; inspect the current caller before claiming fallback behavior.
- For `MobEffect`, prefer `PotionProfile -> ContextKeys.POTIONS -> ModExecutableEffects.POTIONS`. Ask before introducing alternate plumbing.
- Callers construct their own `PotionProfile`; shared effects should provide numeric helpers only. The project's `DEADLY_POISON` is distinct from vanilla poison: snapshot the current panel damage at application time, encode it as amplifier, allow lethal damage and milk cure, and do not restore a source-damage capability.
- If modules cannot express an approved effect, propose isolated ability logic or a focused shared-architecture change and wait for approval.
- Check relevant synergies with partners already implemented in the mod using targeted registry and code checks. Explain the original interaction and whether it is naturally supported, needs special implementation, or needs MC adaptation. Ask whether to retain, add, modify, or exclude each relevant synergy. Record the decision per item. Do not implement missing partner items or silently add special cases.

## Project Semantic Invariants

- Reuse the existing trigger/event system. Cone Head uses the hurt stage and sets hurt amount to zero; fear must also cancel the target's attack behavior. Prefer a direct distance condition for simple auras over a complex geometry abstraction.
- Bullet scale effects modify the player's `BulletScale` attribute directly. Full-charge actions charge all active items in the player's configured inventory scope.
- Runtime coin generation, recognition, payment, and conversion read the configured tier IDs; never hardcode `PENNY`, `NICKEL`, or `DIME` in those paths. Shared action descriptions belong in the action translation group, and explicitly independent projectile art must not reuse an item icon.
- Curios capacity is separate from ordinary item effects: keep passive and trinket capacities independent, treat desired capacity and Curios' actual slots as separate state, and never rescan or synchronize slots recursively from inside an equip callback. Slot tests must physically equip stacks and cover unequip, resize, and reload paths.

## Common Registration And Assets

- Append collectible entries to `manager/id/ItemId` and trinket entries to `manager/id/TrinketId`; the mod numeric ID is the matching enum's `ordinal()`, never the Wiki original ID. Never insert or reorder constants. Confirm a trinket's project level rather than assuming it has an original collectible quality.
- Use `IsaacDisaster.MOD_ID` for mod-owned `ResourceLocation` namespaces.
- Do not add comments or Javadoc to routine inherited-method implementations. Add comments only for complex logic and Javadoc for reusable utility methods.
- Download Repentance icons only from the English Wiki as actual PNG files. The `32x32` requirement applies to passive/active collectible and trinket icons, not pickup assets. Card textures use the `20x20` transparent-canvas contract in `references/pickups.md`; other pickup dimensions require their own confirmed contract. For collectible icons, `Collectible_` is a useful clue, not proof; verify category, identity, and version, then save under the confirmed project texture name. Confirm names containing special characters before normalizing them.
- Do not manually create item models. Add the item to the correct existing datagen list; `runData` generates models and tags.
- Request non-image assets such as sounds from the user. If an icon download or format/size check fails because of network errors, an unavailable Wiki, an invalid link, an HTTP error, or an unexpected format, record the item, concrete reason, and missing-file status and state it explicitly in the final response. Never silently omit a failed texture.
- Insert collectible localization before `"item end": ""` and trinket localization before `"trinket end": ""`. Collectible pools follow Repentance+ without weights; trinkets follow the branch's tag-based trinket pool. Do not place trinkets in collectible pools by default.

## Common Validation And Report

- Check the approved specification against IDs/level, selected branch registrations and lifecycle, module and executable-effect registrations, approved synergies, tooltip values/order, textures, and pools. Verify that stat application and descriptions use the approved ratio and that no inverse configuration scaling cancels the per-unit bonus.
- Run `runData` once after a single item or once after all items in a batch are ready. Check exit status and generated model/tag/data errors. Do not run compilation or `build`.
- Report actual changed files, reused/new modules, branch lifecycle, synergy outcomes, image source/status, pools, lore review, `runData` result, failed texture downloads and reasons, missing non-image assets, and remaining work in Chinese. Do not claim unperformed runtime or visual validation.
- Report `runData`, JUnit, GameTest, client visual checks, and save/reload checks separately. A runtime crash revokes completion status until fixed; refresh audit/database snapshots after source changes.
