# Progress

- 2026-09-01: Created scoped planning files and began inventory.
- 2026-09-01: Added `IExecutableEffect#getRequiredTriggerType()` with the
  `EMTPY` default and annotated all unambiguous single-trigger effects.
- 2026-09-01: Added public-interface tests for the default and representative
  overrides. `compileJava` passed with the Gradle-managed JDK 17; the first
  focused test compile exposed a missing test import, which was corrected.
- 2026-09-01: Made `AbilityEffectEntry` delegate the requirement to its wrapped
  effect; composite effects remain empty because their children may differ.
- 2026-09-01: Full `test` passed; `git diff --check` passed with only normal
  Git line-ending warnings.
