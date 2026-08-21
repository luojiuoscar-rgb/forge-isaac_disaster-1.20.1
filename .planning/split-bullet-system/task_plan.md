# Task Plan: AttackPattern Subsystem v1

## Goal
Add a reusable, registry-backed AttackPattern abstraction for generating independent AttackContext instances, implement the generic ring pattern, and migrate the three approved fixed-ring callers.

## Current Phase
Phase 6: semicircle follow-up

## Phases

### Phase 1: Persistent execution setup
- [x] Recover prior planning state and inspect the approved implementation plan.
- [x] Confirm Forge 1.20.1 / Java 17 project conventions and current working-tree changes.
- [x] Convert the stale split-bullet exploration plan into this implementation plan.
- **Status:** complete

### Phase 2: Core Pattern API and registry
- [x] Add AttackPattern and AttackPatternContext.
- [x] Add ModAttackPattern and register the custom Forge registry.
- [x] Register the generic ring pattern only.
- **Status:** complete

### Phase 3: Generation and caller migration
- [x] Implement three-dimensional ring generation and count edge cases.
- [x] Make AttackContext.copy() isolate CompositeTrigger state.
- [x] Migrate Tammy's Head, Sad Bomb, and Loki's Horns.
- **Status:** complete

### Phase 4: Tests
- [x] Add public-interface tests for ring counts, spacing, 3D directions, immutability, vertical fallback, and trigger isolation.
- [x] Validate the migrated caller counts through the shared Pattern contract; no existing integration fixture exposes the three effect classes without a live Forge entity/event context.
- **Status:** complete

### Phase 5: Verification and handoff
- [x] Run targeted tests and the full project test/build verification available in this checkout.
- [x] Inspect the final diff for unrelated changes and update progress/findings.
- **Status:** complete

### Phase 6: Semicircle follow-up
- [x] Add a generic semicircle Pattern that reuses the shared three-dimensional direction math.
- [x] Migrate Loki's Horns to a reversed-reference semicircle with three extra contexts.
- [x] Re-run verification after resolving the cached Gradle/plugin dependencies.
- **Status:** complete

## Decisions
| Decision | Rationale |
|----------|-----------|
| Keep AttackPattern as a sibling of AttackType | AttackType chooses execution; Pattern generates contexts and can be reused by attacks, active effects, and future SplitModule. |
| AttackPatternContext exposes only reference AttackContext and bullet count | Geometry-specific controls belong to concrete Pattern implementations. |
| Register only ring in v1 | No current caller requires a semicircle Pattern. |
| Use a generic 360/count ring formula | Four and twelve bullets are caller-selected counts, not special Pattern branches. |
| Preserve the existing GetAttackContextEvent -> performAttack flow | Trigger semantics are not stable enough to redesign in this subproject. |
| Deep-copy CompositeTrigger in AttackContext.copy() | Generated contexts must not share mutable trigger containers. |
| Add SemicircleAttackPattern only for the Loki follow-up | The current request only needs the reversed half-circle variant; AttackType remains unchanged. |
| Append Loki's extra contexts instead of replacing the event list | Loki should preserve the preexisting fired contexts and add its own three-shot pattern on top. |

## Errors Encountered
| Error | Attempt | Resolution |
|-------|---------|------------|
| Initial local path for executing/verification skill files was missing | 1 | Read the installed plugin paths under `plugins/cache/openai-api-curated/superpowers/11c74d6b/skills`. |
| `python.exe` was not found on PATH or in the usual `.codex` locations | 1 | Used the Codex workspace dependency runtime to run session catch-up. |
| apply_patch rejected delete-and-add operations for the same file in one patch | 1 | Split file replacement into separate delete and add patches. |

## Notes
- SplitModule priority, override, extra-split, and child-inheritance policies remain out of scope.
- Existing unrelated working-tree changes must be preserved.
