# Findings: Mom's Perfume

- The workspace already contains user modifications to `.agents/skills/isaac-disaster-item-creation/SKILL.md`, its `codex` copy, and untracked Codex notes. These must remain untouched unless required by this task.
- The skill already contains the confirmed potion pipeline guidance: `PotionProfile`, `ContextKeys.POTIONS`, `ModExecutableEffects.POTIONS`, and secondary-target delegation.
- `MomsEyeshadow` is the closest projectile-trigger pattern: `attachToBullet` adds a `BULLET_HIT_ENTITY_BEFORE` trigger and sets a bullet color.
- `ModExecutableEffects.MOMS_EYESHADOW` and `THE_COMMON_COLD` use `AbilityEffectEntry` with `APPLY_EFFECT_TO_SECONDARY_LIVING_ENTITY`, then delegate actual `addEffect` to `POTIONS`.
- `ItemId` currently ends with `CONTRACT_FROM_BELOW(2)`; `MOMS_PERFUME(2)` must be appended after it.
- Existing MOM pools are `moms_chest.json` and `old_chest.json`; `treasure.json` already contains `moms_eyeshadow`.
- `runData` generates the expected basic item model for `moms_perfume` from `ItemListManager.PASSIVE_ITEM_LIST`.
- The Wiki CDN returns WebP by default; `&format=original` returns a valid PNG and was used for the committed texture.
