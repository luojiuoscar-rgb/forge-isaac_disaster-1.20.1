# Task Plan: C-Section + Laser Performance Diagnosis

## Goal
Explain, from the current source, why C-Section plus Laser drops frames at roughly one hundred active bullets while ordinary tears can reach thousands. State average and worst-case complexity for the three cases, including repeatable trigger/module work. No production code changes are in scope.

## Phases

### Phase 1: Recover And Scope
- [x] Read the requested planning, Forge 1.20.1, and delegated-review skill instructions.
- [x] Confirm Forge 1.20.1 and preserve the existing dirty working tree.
- [x] Set up a separate task record.
- **Status:** complete

### Phase 2: Trace Execution
- [x] Trace a normal TearBullet tick, collision path, and module dispatch.
- [x] Trace CSectionAttack/FetusBullet state and Laser module activation.
- [x] Identify repeated work, allocation, entity queries, and recursive attack calls.
- **Status:** complete

### Phase 3: Model Cost
- [x] Derive per-tick average and worst-case costs using explicit parameters.
- [x] Separate server simulation, client rendering, and networking costs.
- **Status:** complete

### Phase 4: Report
- [x] State the most likely bottleneck chain and its evidence.
- [x] List only source-supported mitigation directions; do not implement them.
- **Status:** complete

### Phase 5: Fix Trigger Ownership
- [x] Confirm the parent-trigger alias is the root cause.
- [x] Add a regression test proving AttackContext owns a trigger snapshot.
- [x] Make the minimal defensive-copy fix.
- [x] Run focused and compile verification.
- **Status:** complete

### Phase 6: Enforce Secondary Laser Trigger Inheritance
- [x] Add a regression test that requires the secondary laser to select the raw, no-prepare pipeline.
- [x] Change only the FetusBullet secondary-laser request to use that pipeline.
- [x] Run the focused tests and inspect this scoped diff with OCR delegation.
- **Status:** complete

## Constraints
- The working tree contains unrelated in-progress attack-pipeline changes. Treat them as the current implementation and do not revert them.
- The user requests a diagnosis, not an implementation.

## Errors Encountered
| Error | Attempt | Resolution |
|---|---:|---|
| `python` was unavailable on PATH for planning-session catchup. | 1 | Use the recorded bundled Python path if catchup is still needed; source investigation can continue. |
| Local `ocr` rejected both documented JSON format flags. | 2 | This installed version emits only text; used its text preview and rule resolution successfully. |
| Focused Gradle test did not reach test execution under its initial JDK configuration. | 2 | Located the Gradle-managed JDK 17 and retried; ForgeGradle remapping continued beyond the tool wait window, so source-level verification proceeds while the build remains pending. |
