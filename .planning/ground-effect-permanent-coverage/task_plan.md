# Permanent ground-effect coverage

Branch: `feature/feature-c`. Continuation of the existing uncommitted ground-effect feature; preserve that work and do not commit.

## Approved behavior

New paint permanently removes only the overlapped part of older paint on the same surface. Fully erased states leave the manager and spatial index. Erased damage and visuals never return after the covering state expires or after visibility reentry. Type has no priority. Exact same-owner, same-type, same-center, same-radius water reuses its identity and refreshes its age; its center stays fixed. Moving strokes keep their newly covered area. Damage remains current owner attack damage and produces no knockback. No strips, LOD, persistence, or terrain reprojection in this change.

## Implementation

- [x] First add manager regression tests through `spawnWater`, `remove`, `activeCount`, and `contains`; verify failures before production edits.
- [x] Introduce immutable `GroundEffectCoverage`: cached convex pieces in local XZ coordinates, the existing seeded twelve-sided brush outline, point containment, and convex polygon subtraction. Preserve the outline and use the same remaining pieces for damage and rendering. Subtract only at spawn/repaint, querying existing nearby grid cells. Empty pieces are discarded; separate heights do not erase each other.
- [x] Attach coverage, a revision, and refreshable age/order to server states. Stage overlap results before capacity checks so rejected spawns cannot damage old paint. Refresh preserves identity and cooldown while advancing order/revision. Remove fully erased states with ordinary despawns.
- [x] Include complete remaining coverage and revision in spawn snapshots and update batches. Track each player's last sent revision. Static unchanged states send no updates. Reentry snapshots include clipped shapes. Client updates require matching slot/generation and a newer revision.
- [x] Cache client triangle vertices when coverage changes. Render the remaining pieces only; do not perform per-frame overlap tests or clipping. Preserve transient cleanup and epoch behavior.
- [x] Add geometry, packet, stream, and real Forge contact tests covering holes, unions of erasers, refresh, capacity transactions, height separation, stale updates, and snapshots after the eraser disappears.
- [x] Run Java 17 `compileJava compileTestJava compileGameTestJava`, focused ground-effect JUnit, and Forge `runGameTestServer -PgameTestNamespaces=isaac_disaster_ground_effect`. Check actual test count. Report client rendering separately; only claim visual validation if inspected in a running client.

## Main files

`groundeffect/core/GroundEffectCoverage.java` owns geometry and network encoding; `AbstractGroundEffect` owns server lifetime and revision; `GroundEffectManager` applies paint; `GroundEffectRuntime` sends visibility snapshots and revision changes. Spawn/update packets carry immutable coverage. `ClientGroundEffectState` caches triangulation; `GroundEffectStream` validates identities and revisions; `GroundEffectRenderer` draws cached vertices.
