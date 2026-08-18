# TearBullet Position Jump Progress

## 2026-08-14

- Created a task-scoped planning directory so the existing project memory is not overwritten.
- Read the requested planning, Minecraft modding, and OCR delegation skill instructions.
- Confirmed the workspace is Forge 1.20.1 / 47.4.9 and contains extensive unrelated uncommitted changes.
- Initial search located TearBullet, trajectory, and laser consumers.
- Ran OCR delegate preview over the workspace. Its 35 reviewable code files do not include the bullet or trajectory subsystem; entity visual-state sync remains to be checked as the only potential nearby diff.
- Inspected the movement, trajectory, spawn, event, and Forge tracker packet paths. Confirmed that custom spawn packets include initial `deltaMovement`, while the server-only trajectory plan is delivered later through Forge tracking packets.
- Completed OCR rule resolution and accounted for all 35 previewed files; no current workspace modification affects TearBullet movement or synchronization.
- Diagnosed the jump as a delayed Forge correction of client prediction after server-only trajectory/steering planning. Documented the no-trajectory edge case and the narrow condition under which the current source has no matching cause.
- Verification: `git diff --check -- .planning/tearbullet-position-jump` passed before final status update.

## Follow-up: Tick Design Clarification

- Confirmed the double-buffer movement order is logically consistent with the desired model: both sides move the stored plan, then the server computes the next plan.
- Identified the distinct network condition the model needs: the client must receive the next plan before consuming it. Current entity tracking at `updateInterval(2)` and normal packet latency do not provide that guarantee, so the client instead uses the latest received plan and requires correction after server-only trajectory/steering changes.
- No mod source was changed.
