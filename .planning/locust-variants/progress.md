# Progress

2026-10-22: Confirmed target branch and clean worktree; inspected existing entities, familiar descriptors, client registration, poison conversion and bomb explosion implementation.
Investigation-only commands: one PowerShell brace-expansion attempt failed; corrected to explicit paths. Project contains only en_us.json, with Chinese display names.

Red phase: all eight new GameTests failed as expected before implementation (old blue damage and missing locust descriptors). Java 17.0.11, existing user Gradle cache; sandbox required escalation for Gradle lock/cache writes. Initial default Gradle home resolved to C:\.gradle, corrected to the actual user cache.
Implemented four fly-derived locust classes, entity/familiar/renderer registrations and translated names. Updated blue-fly and blue-spider regression expectations.
First implementation run: 7/8 GameTests passed; custom poison's immediate first pulse was blocked by hurt cooldown. Changed only the locust duration to 59 ticks so its two pulses fall on ticks 20 and 40. No global poison or cooldown behavior changed.

Full JUnit: 508 tests, 507 pass, existing D12Test fails at water-state exclusion (D12 and its test unchanged). Expanded fly/spider/locust GameTests: 112 complete, 111 pass. All eight new tests pass, including real poison damage at ticks 20/40. The only failure is the existing knife limit test using the obsolete `isd familiar add @s ...` argument order; current command is `isd familiar @s add ...`. Do not fix unrelated tests in this task.
Independent read-only review via requesting-code-review skill found no actionable defects; noted optional Death second-hit-cancellation and first-hit-lethal coverage gaps.
Explosion investigation documented in docs/research/2026-10-22-war-locust.md. Existing API is bomb-specific, uses distance/exposure falloff, normal explosion cooldown, and bomb-entity-specific cached item effects; no red locust implementation added.

Final focused verification succeeded (build/locust-focused.log): 143 relevant JUnit tests, zero failures/errors; 8/8 locust GameTests passed. Production, test and GameTest compilation passed under Microsoft Java 17.0.11. git diff HEAD --check passed. No client session or visual inspection was performed. No commits or branch changes were made.
