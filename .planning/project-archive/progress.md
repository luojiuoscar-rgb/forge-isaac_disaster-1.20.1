# Archive Progress

## Completed

- Unified projectile scale and collision calculations.
- Reworked continuous entity/block collision and reflection behavior.
- Unified tracking origins around dynamic AABB centers.
- Added scale-aware shatter particles with material selection for tear, rotten
  flesh fetus fallback, and bone-block skeleton fetus.
- Calibrated shatter particle count to 20% of the previous curve and restored
  vanilla-like gravity/friction/lifetime behavior.
- Added damage cooldown bypass tags and projectile-specific knockback responses.
- Added client visual resolver and projectile translucent RenderType.
- Merged shatter helper calculations into `ProjectileCollisionHelper`.
- Compiled Java successfully and ran focused collision/shatter tests.

## Git/Workspace Notes

- The index contains the current shatter/rendering files and some earlier staged
  projectile tests. The working tree also contains unrelated unstaged changes.
- Preserve all unrelated changes. Do not use destructive reset/checkout.
- A stray untracked `NUL` file and untracked `codex/` notes were present at
  archival time; leave them untouched unless explicitly requested.

## Next Session Checklist

- Re-read this archive and inspect source before modifying anything.
- Confirm staged versus unstaged versions of every projectile file.
- Run `git diff --cached --check` after any index operation.
- Use Microsoft JDK 17 path:
  `C:\Program Files\Microsoft\jdk-17.0.11.9-hotspot`.
- For render changes, compile first, then perform client/game validation before
  claiming transparency or depth-ordering behavior is fixed.
