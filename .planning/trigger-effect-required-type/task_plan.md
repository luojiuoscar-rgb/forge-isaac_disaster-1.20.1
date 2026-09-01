# Required Trigger Type

## Goal

Add a single-value required-trigger-type contract to executable effects and
annotate existing effects whose implementation is explicitly tied to one
trigger type. Do not implement Error Skill behavior or other future APIs.

## Phases

- [completed] Inventory effect registrations and event-specific checks
- [completed] Add the default interface contract and focused overrides
- [completed] Add/adjust public-interface tests
- [completed] Compile, test, and inspect the scoped diff

## Decisions

- The contract lives on `IExecutableEffect`, because `SimpleTrigger` stores and
  invokes that interface.
- The default value is `ModTriggerTypes.EMTPY`, preserving the existing empty
  trigger placeholder semantics.
- An override is added only when one trigger type is unambiguously required.
- Effects intentionally reused by multiple trigger types remain unoverridden.

## Errors Encountered

| Error | Attempt | Resolution |
|---|---:|---|
| `python` is unavailable on PATH | 1 | Use PowerShell and Gradle/JDK tooling directly |
