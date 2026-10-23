# Findings

- Current damage tests use horizontal circle-center contact; renderer uses a seeded twelve-sided outline. Shared remaining coverage will make the painted footprint authoritative for both.
- Current visibility state records only identities and spawn packets contain full circles. Coverage revision tracking and complete clipped snapshots are required for late viewers and reentry.
- Current update packets are unused identity/position placeholders. They can carry age/order/revision/coverage changes without introducing another packet type.
- Exact same-owner repaint must not clear grouped cooldown or move the center. A same-type different-owner stroke must retain the new damage attribution.
- A rejected capacity-limited spawn must not partially erase existing paint. Calculate candidate changes first and apply only after admission succeeds.
- Coverage uses the current rendered irregular polygon rather than a new raster grid. Contact now matches that outline instead of extending to the nominal circle outside the painted edge. The same surface is compared within 0.0001 blocks; separate heights do not erase each other.
- Repainting is exact-center/radius/lifetime and same-owner only. No positional merge tolerance was introduced, so slow movement cannot drag a refreshed circle along or swallow the trail.
- Partial cuts may increase geometric piece count; this change guarantees permanent coverage and removal of fully erased states, not a universal CPU/network speedup. There is no per-frame clipping and no historical eraser chain to retain after expiration.
