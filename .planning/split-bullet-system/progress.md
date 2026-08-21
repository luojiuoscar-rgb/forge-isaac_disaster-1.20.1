# Progress Log

## Session: 2026-08-20

### Phase 1: Recovery And Project Context
- **Status:** complete
- **Started:** 2026-08-20
- Actions taken:
  - Read the requested `planning-with-files` skill and the relevant process skills that constrain this conversation to background exploration and design discussion.
  - Read the existing root planning files to confirm they are project-wide memory, not the correct place for a feature-specific plan.
  - Reused the bundled Python runtime path to run the planning skill catch-up script after `python` on PATH failed.
  - Inspected the current projectile pipeline, including attack selection, attack context construction, bullet creation, trigger-module dispatch, and bullet lifecycle events.
  - Confirmed that the repo does not already implement the specific split-tear items currently under discussion.
- Files created/modified:
  - `.planning/.active_plan` (created)
  - `.planning/split-bullet-system/task_plan.md` (created)
  - `.planning/split-bullet-system/findings.md` (created)
  - `.planning/split-bullet-system/progress.md` (created)

### Phase 2: Vanilla Behavior Taxonomy
- **Status:** in_progress
- Actions taken:
  - Started classifying likely Isaac behavior families by trigger timing and parent/child bullet relationship.
  - Separated attack-type priority concerns from bullet-lifecycle decoration concerns in the repo model.
  - Recorded the user's ordered split-effect-list hypothesis as a concrete design candidate to evaluate.
- Files created/modified:
  - `.planning/split-bullet-system/task_plan.md` (created)
  - `.planning/split-bullet-system/findings.md` (created)
  - `.planning/split-bullet-system/progress.md` (created)

### Phase 3: Project Mapping
- **Status:** in_progress
- Actions taken:
  - Confirmed that trigger-module rules can inspect the active module snapshot while gating a candidate module, which means the repo already has a place to express split-effect exclusivity.
  - Refined the design direction from a raw ordered list toward an explicit split-family resolver plus child-inheritance policy.
  - Reframed the user's queue idea as a ranking layer where same-priority entries may stack only when they are intentionally composable.
  - Verified that revive logic in this repo already uses its own registry plus sequence, which is the best local precedent for a dedicated split-module family.
- Files created/modified:
  - `.planning/split-bullet-system/task_plan.md` (modified)
  - `.planning/split-bullet-system/findings.md` (modified)
  - `.planning/split-bullet-system/progress.md` (modified)

## Test Results
| Test | Input | Expected | Actual | Status |
|------|-------|----------|--------|--------|
| Planning catch-up | Bundled Python runtime + `session-catchup.py` | Catch-up runs without PATH-dependent failure | Ran successfully with no additional unsynced context output | ✓ |
| Exact-item search | `rg` for Haemolacria / Parasite / Compound Fracture / Cricket's Body names | Either existing implementation matches or clean absence | No direct implementation matches in current source/resources | ✓ |

## Error Log
| Timestamp | Error | Attempt | Resolution |
|-----------|-------|---------|------------|
| 2026-08-20 | `python` command not found for `session-catchup.py` | 1 | Switched to the bundled absolute runtime path documented in the root findings file. |

## 5-Question Reboot Check
| Question | Answer |
|----------|--------|
| Where am I? | Phase 2, classifying vanilla split-like behaviors before choosing subsystem boundaries. |
| Where am I going? | Map those behavior families onto repo seams, then propose design options. |
| What's the goal? | Frame a split-bullet subproject grounded in both Isaac behavior and this repo's projectile architecture. |
| What have I learned? | The repo already has strong hit/block/end-of-life seams and prioritized trigger-module attachment for bullet behaviors. |
| What have I done? | Read project context, inspected the projectile pipeline, created a scoped plan, and recorded the first-pass findings. |

## Session: 2026-08-21

### AttackPattern implementation
- **Status:** complete
- Actions taken:
  - Loaded the user-approved AttackPattern implementation plan.
  - Read the planning, Minecraft Forge, TDD, execution, and verification instructions.
  - Confirmed the workspace runtime and ran session catch-up successfully with no unsynced output.
  - Replaced the stale exploration task plan with the implementation phases and recorded the final v1 decisions.
  - Added `AttackPattern`, `AttackPatternContext`, `ModAttackPattern`, and the registered `ring` implementation.
  - Added three-dimensional ring generation with count edge cases and a vertical-axis fallback.
  - Made `CompositeTrigger` and `AttackContext.copy()` produce independent trigger containers while preserving rotation offsets.
  - Migrated Tammy's Head to 12 ring contexts, Sad Bomb to its power-derived count, and Loki's Horns to four replacement contexts.
  - Added six public-interface tests covering counts, 360/count spacing, arbitrary directions, vertical fallback, template immutability, and trigger isolation.
  - Verification:
    - Java 17 `gradlew test`: passed.
    - Java 17 `gradlew build`: passed before the final test-only regression addition.
    - Java 17 `gradlew test` after the final regression addition: passed.
    - `git diff --check`: passed with only normal LF/CRLF conversion warnings from Git.

### Semicircle follow-up
- **Status:** complete
- Actions taken:
  - Added `SemicircleAttackPattern` and registered it alongside `ring`.
  - Updated Loki's Horns to reverse the reference direction, generate three semicircle contexts, and append them to the event's existing contexts.
  - Expanded the semicircle tests to cover 3D directions and vertical fallback.
- Verification:
  - Full Gradle 8.8 test passed with Java 17.
  - Full Gradle 8.8 build passed with Java 17.
  - `git diff --check` passed; only normal LF/CRLF conversion warnings were reported.

### AttackContext direction API
- **Status:** complete
- Added `AttackContext.setDirection(Vec3)` with zero-vector validation, normalization, absolute rotation conversion, and offset clearing.
- Updated Ring Pattern, Semicircle Pattern, and Loki's Horns to use the shared API.
- Added tests for normalized direction conversion and zero-vector rejection.
- Re-ran full Java 17 `test` and `build`; both passed.
