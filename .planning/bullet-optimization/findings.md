# Findings and Decisions: Bullet Optimization v1

## Requirements
- Forge 1.20.1 / Java 17.
- Branch `codex/bullet-optimization`; no commit or merge.
- No trajectory implementation in this version.
- Support 10,000+ transient bullets with server authority and smooth client prediction.
- Preserve existing `IBulletObject`, hit events, split modules, and one-shot effect behavior through adapters.

## Research Findings
- Existing TearBullet is a full Minecraft Entity and performs movement, trajectory parsing, block collision, and `getEntitiesOfClass` living collision in its tick path.
- Existing bullets are already not saved to world; the optimization can avoid Entity/UUID/NBT/tracker lifecycle entirely.
- Client protocol must send immutable visual/kinematic spawn state once, then batched lifecycle/events and sparse corrections; trigger modules remain server-side.
- MadParticle research supports borrowing queue partitioning, batching, caching, and instanced rendering ideas, but not async access to mutable server world state.

## Open Questions
- Exact existing attack/split/effect interfaces and client packet bootstrap still need source inspection.
- Compatibility path for complex Fetus/Skeleton visuals must be selected after renderer inspection.
- Ordinary and C-Section attacks now use BulletState through BulletRuntime; legacy entity registrations remain only for compatibility.

## Technical Decisions
| Decision | Rationale |
|----------|-----------|
| Object-mode BulletState first | User prioritizes clarity; hot-field SoA can follow profiling |
| 3D voxel DDA for block sweep | Avoids high-speed tunneling; complex shapes fall back to VoxelShape |
| 2x2x2 EntityGrid with dedup markers | Bounds candidate scans without allocating per-bullet sets |
| Segment/AABB slab test with approximate fast path | Keeps common collision cheap while retaining precise fallback hooks |
| Keep legacy Entity spawn path unchanged until event/effect adapters exist | Prevents silently dropping gameplay effects while the new state layer is validated |
| Use batch spawn/despawn packets and a render-state collector | Keeps network and draw submission proportional to changes rather than entity count |
| Send only periodic homing corrections | Avoids continuous position packets for deterministic ordinary tears while reconciling server-only steering |

## Remaining Verification Gaps
- Automated tests cover kinematics, slot-generation reuse, client correction blending, DDA, swept AABB, and event compatibility. They do not create a live Forge level with real blocks, entities, or registry-loaded split modules.
- The required 1k through 15k real-server/client benchmark has not been run. Network bytes per tick and frame time therefore remain unmeasured.
- The Fetus default player-model renderer is connected to `BulletRenderState`; the specialized skeleton visual is not yet represented by the lightweight renderer.

## Issues Encountered
| Issue | Resolution |
|-------|------------|
| `python` command is not installed on host PATH | Use PowerShell/Gradle-managed Java tooling; do not block implementation |

## Compatibility follow-up findings
- The legacy entity tick decrements `lifeTick` before movement; an optimized state that moves on its final lifetime tick travels one step farther than `TearBullet`.
- Legacy swept collision moves the projectile to each contact point before dispatching hit effects and stops the current movement segment when an after-hit event is canceled. The lightweight path now mirrors both behaviors.
- Fetus candidates are collected before hit events are dispatched. The post-hit cooldown must therefore be checked again in the handler to prevent multiple same-tick damages.

## Compatibility Review Decisions (2026-09-01)
- The user confirmed that deferred split execution after event dispatch is intentional. This ordering is excluded from the compatibility fixes; collision-time position/velocity snapshots and generation guards remain required.
- All other confirmed regressions are in scope: render/collision size coupling, Fetus hit cooldown, global collision ordering, dimension-unaware block sweep, four-tick homing cadence, controllable fallback steering, late-spawn catch-up, all-player broadcast, and avoidable hot-path collection allocation.
- Trajectory remains disabled and is not part of this repair round.
