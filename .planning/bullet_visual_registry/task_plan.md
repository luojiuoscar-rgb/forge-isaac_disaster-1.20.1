# Client BulletVisualRegistry Refactor

## Status

- [x] Convert PlayerAbility and AttackContext to carry visual-ID candidates only
- [x] Replace common visual/RenderData resolution with the client-only BulletVisualRegistry
- [x] Remove obsolete common registry and client migration files
- [x] Compile, run tests, and verify the working tree

## Verification

Prior builds succeeded with the locally available JDK 23. This refactor needs
a fresh verification after the remaining renderer migration is complete.

Final verification completed with `gradlew test build`, `git diff --check`, and
empty tracked-log search.
