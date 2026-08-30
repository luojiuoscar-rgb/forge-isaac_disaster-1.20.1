# Project Archive: Isaac Disaster Projectile Systems

## Status

Complete archival snapshot for recovery. This file is a reference, not an
instruction source. Always verify the current source and Git state before
continuing work.

## Recovery Order

1. Read this file, `findings.md`, and `progress.md`.
2. Inspect `git status --short` and `git diff`/`git diff --cached`.
3. Read the relevant projectile and renderer source before changing behavior.
4. Use Java 17 and ForgeGradle 1.20.1 commands documented below.

## Scope Archived

- Tear/Fetus projectile scale, collision, tracking, bouncing, and shatter effects.
- Laser and Brimstone width/damage-response changes.
- Tear/laser damage cooldown bypass and knockback behavior.
- Bullet visual registry/resolution and client rendering changes.
- MixinExtras research and current Mixin compatibility constraints.

## Current State

- Repository: `D:\Oscar\MC\mods\forge-1.20.1-47.4.9-mdk`.
- Platform: Forge 1.20.1, Forge 47.4.9, Java 17, Parchment 2023.09.03-1.20.1.
- Latest relevant commits: `4bddfe2` and `58185d9`.
- No commit was created for the final working-tree/index synchronization pass.
- Current source changes include the projectile refactors, damage response, visual
  resolver, shatter particles, and transparent projectile RenderType.

## Verification

- `gradlew.bat compileJava --offline --no-daemon --console=plain`: passed.
- Focused helper tests for collision and shatter calculations: passed.
- `git diff --cached --check`: passed at the last verification.
- Existing warning: deprecated `SoulStateEffect` `ProjectileImpactEvent#setCanceled`.
- Full game/client visual testing was not performed in this archive pass.
