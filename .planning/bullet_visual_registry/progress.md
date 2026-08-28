## 2026-08-27

- Replaced static renderer visual map with a Forge custom registry definition.
- Added client-only binding layer and migrated default tear/fetus rendering.
- Removed old visual base classes and obsolete remove/resolve APIs.
- Verified `gradlew test build` successfully.
- Replaced BulletVisual record with server-safe abstract registry class.
- Added top-level client renderer interface and removed obsolete client binding interface/base classes.
- Connected TEAR/FETUS visual selection to attack context creation.
- Ordered BulletColor and BulletVisual capability maps on writes for constant-time best lookup.
- Moved default visual resolution into Forge registry entries returning common render data.
- Unified renderer now performs Tear quad and Fetus player-model drawing from render data.
- Removed client visual registry/interface and legacy client visual classes.
## 2026-08-28

- Resumed the client-only registry migration from the partially converted worktree.
- Confirmed PlayerAbility and AttackContext already carry visual ID candidates, while BulletRenderer still used the obsolete common RenderData route.
- Began replacing that renderer path and cleaning obsolete registry files.
- Added complete client `DefaultFetusVisual` and migrated `BulletRenderer` to a
  WeakHashMap candidate-signature cache shared by `render` and texture lookup.
- Added synchronized owner UUID data so Fetus skin lookup works on the client.
- Removed common Forge visual registry, RenderData records, old client binding
  classes, and unused material helpers.
- `gradlew test build` passed on JDK 23; `git diff --check` passed; no tracked
  `latest.log` or `logs/latest.log` entries remain.

## 2026-08-28 Formatting follow-up

- Reformatted the compressed `DefaultTearVisual` implementation and normalized
  `BulletVisualRegistry` and `BulletVisualTarget` declarations.
- Scanned the client visual package for remaining lines over 140 characters;
  none remain.

## 2026-08-28 Review fixes

- Changed `BulletRenderer` to compare the raw synchronized visual signature and
  return the cached `bestVisual` without parsing candidates on cache hits.
- Added a read-only `TearBullet#getVisualIdsSignature()` accessor.
- Added separate wide/slim player models and selects between them from
  `PlayerInfo#getModelName()` while preserving wide default-skin fallback.
- Verified `compileJava` succeeds on JDK 23.
- Git index normalization could not be performed because `.git/index` was
  write-protected and the escalated `git add -u` request was rejected by the
  approval service; no workaround was used.

## 2026-08-28 Archive handoff

- Retried `git add -u` after approval was enabled; it succeeded and removed the
  prior `AD` mixed index state without staging the two untracked `codex/*.md`
  files.
- Final `gradlew test build` succeeded on JDK 23.
- `git diff --check` succeeded and `git ls-files '*latest.log' '*logs/latest.log'`
  returned no entries.
- The review-fix implementation is complete and the planning folder now
  contains the final architecture, findings, progress, and verification state.
