# Forge BulletVisual Registry With Client Adapters

## Status

- [x] Convert PlayerAbility and AttackContext to carry visual-ID candidates only
- [x] Replace the client-only visual map with a common Forge BulletVisual registry
- [x] Add client adapter factories keyed by the existing RegistryObject entries
- [x] Migrate tear, player fetus, and skeleton visuals and update renderer resolution
- [x] Compile, run tests, and verify legacy references are removed

## Verification

Final verification completed with `gradlew test build` on JDK 23,
`git diff --check`, and an empty tracked-log search.

The Forge registry is now the single source of visual IDs, targets, priorities,
and data-only material/model descriptors. Client code binds render adapters to
the same registry entries without maintaining a second ID registry.

The follow-up review fixes are complete: renderer cache hits now use the raw
synced signature and cached best visual, Fetus maintains separate slim/wide
models selected from `PlayerInfo`, and the old `AD` index state was normalized
with `git add -u`. Two pre-existing untracked `codex/*.md` files remain
unmodified and un-staged.
