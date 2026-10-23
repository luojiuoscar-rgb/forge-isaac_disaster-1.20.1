# Ground effect runtime hardening

Approved scope: feature/feature-c; target+owner cooldown; fixed-height cheap contact gate; 48-block interest radius; per-player dimension session epochs; sparse clock calibration and authoritative despawn; byte-budgeted water packets including oversized single snapshots; indexed visibility and cached render order.

Constraints: keep seeded twelve-sided footprints, permanent coverage semantics, current ground projection, 75%-150% / 0.5-second pulse and its enabled visual-test override. Preserve all unrelated staged/unstaged changes. No commits or staging changes.

- [x] Contact/visibility regressions, then manager implementation (contacts agent).
- [x] Packet splitting/fragment reassembly regressions and implementation (network agent).
- [x] Session and clock regressions, then server/client integration (parent).
- [x] Wire synchronization/fragment packets and cache render ordering.
- [x] Run focused JUnit, compileGameTestJava, actual isolated ground-effect GameTests, and whitespace checks.
- [x] Independent review, resolve findings, report exact validation surfaces and remaining limits.

Ruling: use player synchronization sessions, not dimension epochs, so returning to a previously visited dimension always receives a newer session. Require an explicit matching-dimension reset before accepting lifecycle packets after world unload.

Ruling: the server remains the authority for removal. Predicted client age is capped below full disappearance while awaiting despawn, and one clock sample per visible stream per 20 server ticks bounds drift without per-effect update packets.

Ruling: leave bullet behavior unchanged; shared PacketBatching additions apply only to the ground-effect packet variants.

Validation: Microsoft Java 17.0.11 / Gradle 8.8. Initial RED failed at expected missing APIs. GREEN ran 72 focused JUnit tests plus all 10 actual ground-effect GameTests; after adding grid-branch and cached-byte/count validation coverage, the final focused suite contains 75 passing JUnit tests. compileGameTestJava and source whitespace checks pass. Logs: build/groundeffect-hardening-red.log, build/groundeffect-hardening-validation.log, build/groundeffect-hardening-final.log.

Independent spec/quality review found no substantiated P1/P2 defect in the hardening scope. Minecraft MCP status connected=false/processAlive=false; no new live client visuals or real multiplayer dimension round-trip run is claimed. No measured FPS/TPS gain is claimed. No git add/commit was performed by this task.
