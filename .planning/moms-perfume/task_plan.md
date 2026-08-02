# Task Plan: Mom's Perfume

## Goal
Implement the confirmed Forge 1.20.1 passive item `Mom's Perfume` with +0.5 fire rate and luck-scaled fear tears, using the existing projectile-trigger and potion executable-effect pipelines.

## Phases

### Phase 1: Skill And Persistent Plan
- [x] Preserve and verify the existing potion-pipeline guidance in the Isaac item skill.
- [x] Record implementation findings and progress in this scoped plan.

### Phase 2: Rule Tests
- [x] Add failing tests for the luck-to-fear probability boundaries.
- [x] Run the focused test and confirm the expected pre-implementation failure.

### Phase 3: Registrations And Runtime Behavior
- [x] Append `MOMS_PERFUME` without reordering `ItemId`.
- [x] Register the passive item, ability, trigger module, executable-effect entry, and fear bullet color.
- [x] Implement stat, set, module lifecycle, bullet attachment, and potion-profile execution behavior.

### Phase 4: Assets And Pools
- [x] Add localization, model, verified PNG icon, and Treasure/Mom's Chest/Old Chest entries.

### Phase 5: Verification
- [x] Run focused tests, `runData`, resource checks, and `git diff --check`.
- [x] Review the scoped diff and report any verification limitation.

## Decisions
- Wiki original item: `Mom's Perfume` (`5.100.228`).
- Mod ID: append `MOMS_PERFUME(2)`; preserve existing ordinals.
- Fear duration: 80 ticks.
- Fear chance: `min(1.0, 15.0 / (100.0 - floor(luck)))`.
- Fear color: `0xFFFF55`, alpha `1.0`, priority `20`.
- Target effect chain: `AbilityEffectEntry -> APPLY_EFFECT_TO_SECONDARY_LIVING_ENTITY -> POTIONS -> PotionProfile(PANIC, 80, 0)`.
- No direct `addEffect` in the Mom's Perfume-specific executable-effect entry.

## Errors Encountered
| Error | Attempt | Resolution |
| `MomsPerfume` missing during `compileTestJava` | 1 | Expected RED result; implement the production class and rerun the focused test. |
