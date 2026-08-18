# Task Plan: Revival System

## Goal
Design and implement a revival system for Isaac Disaster that matches the existing Forge 1.20.1 architecture, with explicit persistence, sync, and HUD behavior.

## Current Phase
Phase 5

## Phases

### Phase 1: Project Discovery
- [x] Confirm platform, version, and core mod structure
- [x] Identify player data ownership and persistence paths
- [x] Inspect death, respawn, and clone handling
- [x] Capture findings in findings.md
- [x] Identify open design constraints for revival semantics
- **Status:** complete

### Phase 2: Requirements Clarification
- [x] Define what "revival" means in this mod
- [x] Define triggers, costs, and consumption rules
- [x] Define what survives death and what resets
- **Status:** complete

### Phase 3: Architecture Options
- [x] Propose 2-3 viable integration approaches
- [x] Compare persistence, event timing, and sync tradeoffs
- [x] Recommend one approach with rationale
- **Status:** complete

### Phase 4: Design Spec
- [x] Define server-side flow and ownership boundaries
- [x] Define capability, event, packet, and helper responsibilities
- [x] Define edge cases and verification strategy
- **Status:** complete

### Phase 5: Implementation Planning
- [x] Break the approved design into concrete code tasks
- [x] Identify touched files and tests
- [x] Prepare handoff for implementation
- **Status:** complete

## Key Questions
1. Should revival be a generic player service, or should it be driven only by specific items/effects/triggers?
2. Should revival prevent full death before clone, or should it restore the player after the standard respawn path? (answered: intercept before death)
3. Which state should survive revival: passive items, temporary effects, familiars, flight state, room state, and position?
4. Does revival need multiplayer-visible feedback such as particles, sounds, messages, or animation hooks?

## Decisions Made
| Decision | Rationale |
|----------|-----------|
| Use `.planning/revival-system/` for this design track | Matches user request to keep planning files under a system-named subfolder |
| Treat existing player capability + clone flow as the first integration surface to study | The mod already persists most player systems through `ForgeEvents.onPlayerCloned` |
| Provisional direction: keep revival as its own module and queue, not as raw trigger-module state | Revival has stronger ordering, consumption, and respawn semantics than the current trigger/event abstractions |
| Do not model a revive source as a `TriggerModule` subtype | The trigger interface is side-effect-only and too weak for pre-death interception and queue resolution |
| Separate revival source, available charges, and life-scoped aftermath into different state layers | This prevents revive items from becoming either permanent free lives or permanent penalties across vanilla respawns |

## Errors Encountered
| Error | Attempt | Resolution |
|-------|---------|------------|
| `planning-with-files` session catchup script could not run because no `python` launcher was available in PowerShell | 1 | Continued with manual repository inspection and recorded the limitation here |

## Notes
- Existing player-specific state is capability-heavy; revival design should respect current ownership boundaries before introducing new managers.
- Existing system modules worth mirroring: `system/flight`, `system/freeze`, and `system/rockbottom`.
- Revival in Isaac terms is now defined as a priority queue with count, consumption, and per-entry revive behavior.
- Revival now explicitly targets pre-death interception instead of post-respawn repair.
- Queue entries are independent, not grouped counters.
- Current preferred memory model: `FirstObtain -> consumer now`, `handleObtain/remove -> provider only`, `provider -> consumer sync only on true death`.
- Implementation completed: revive registry, capability persistence, death interception, clone-time rebuild, HUD sync, and revive HUD overlay are now in place and verified with tests plus a full build.
- Follow-up cleanup completed: revive modules now delegate to registered executable effects, with potion buffs routed through `POTIONS` and simple-revive healing routed through `HEAL`.
- Final cleanup completed: revive tests now exercise public/NBT behavior only, and the remaining production code no longer exposes test-only accessors.
