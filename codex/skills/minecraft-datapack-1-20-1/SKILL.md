---
name: minecraft-datapack-1-20-1
description: Use when creating, editing, or debugging Minecraft Java Edition 1.20.1 datapacks, including pack.mcmeta, mcfunction files, loot tables, recipes, advancements, predicates, tags, and datapacks consuming Isaac Disaster mod items. Also use for older-format path or item-NBT errors in this Forge 1.20.1 project. Do not use for other Minecraft versions, resource packs, or Java mod implementation.
---

# Minecraft Datapack — Java / Forge 1.20.1

Target **Minecraft Java 1.20.1**, data pack format **15**. Preserve that target
throughout the file tree, commands, examples and checks. Write user-facing
explanations in Chinese; preserve identifiers and command syntax.

This is the project's own version-specific skill. Its maintained source lives
beside the item creation skill in `codex/skills/`; its discoverable copy lives in
`.agents/skills/`. Changes to this skill should synchronize the complete folder
and verify relative file names and SHA-256 hashes.

## Version contract

| Surface | Use in 1.20.1 |
|---|---|
| Metadata | `"pack_format": 15`, plus `description` |
| Resource folders | `functions`, `advancements`, `recipes`, `loot_tables`, `predicates`, `item_modifiers`, `structures` |
| Legacy tag folders | `tags/functions`, `tags/items`, `tags/blocks`, `tags/entity_types`, `tags/fluids`, `tags/game_events` |
| Engine entry points | `data/minecraft/tags/functions/load.json` and `tick.json` |
| Other registries | Preserve their actual 1.20.1 names, e.g. `damage_type`, `dimension_type`, `worldgen/biome`; these are already singular |
| Item arguments | `minecraft:item{NBT}`; serialized stacks use `id`, byte `Count`, and `tag` |
| Recipes / advancement icons | Legacy `item` fields, not modern `id` / `components`; ingredients are objects or arrays of objects |
| Functions | Ordinary command lines without leading `/`; no `$` macros, `function … with …`, or backslash continuations |
| Command boundaries | No `/random`, `/tick`, `@n`, `execute if items/function`, item component brackets, or `return run/fail`; **`return <integer>` is available** |

Do not port a newer pack by changing only its metadata. Do not pluralize every
registry directory. For exact fields and version boundaries, read
[references/version-and-sources.md](references/version-and-sources.md).

## Work with the actual pack

1. Locate the requested pack and inspect `pack.mcmeta`, namespace and existing
   entry points. For a new pack, use the user's destination and namespace; keep
   development fixtures under `temp/isaac-hunger-games/`. A standalone world
   pack has `pack.mcmeta` at its root and goes in `<world>/datapacks/<pack>/`.
   Mod-bundled data instead goes under the project's existing resource tree.
2. Read only the relevant sections of
   [references/data-examples.md](references/data-examples.md) for registry JSON,
   item NBT and scheduling. Keep existing conventions where they match 1.20.1.
3. For Isaac Disaster items, commands or custom serializers, read
   [references/project-integration.md](references/project-integration.md).
   Confirm registration IDs and actual capabilities from current source or the
   running game. A datapack cannot create Java items, abilities or networking.
   Use `minecraft-modding` for necessary Java changes and the existing item
   creation skill when a passive collectible itself is in scope.
4. Write complete files and matching references. Keep tick work bounded; use
   scores, tags, predicates and scheduled functions for state and timing.
   A custom-namespace `load`/`tick` tag is valid but does not auto-run.
5. Verify the affected files using the evidence-based procedure below. Resolve
   uncertain rules against the matching game/mod version before changing files.

## Verify the datapack

This skill includes no validation scripts. Review the actual files and use
version-specific evidence and available tools. AI reasoning alone does not
prove correctness; neither a clean tool report nor agreement with an example
establishes that a pack loads or behaves correctly.

### File and version checks

- Confirm the pack root, `pack.mcmeta` object, `pack` object, description and
  integer `pack_format: 15`. Parse the affected JSON with an available standard
  JSON parser to catch syntax errors; distinguish JSON parsing from Minecraft
  schema validation. Check the relevant paths against the version contract,
  including namespace/resource names and `minecraft` load/tick entry points.
- Compare changed registry fields with a working **1.20.1** vanilla definition
  or its actual serializer. Read the linked Wiki history for version boundaries;
  do not apply current Wiki examples indiscriminately. Check recipe ingredients
  and serializer-specific result fields, advancement icons/criteria, loot
  functions, predicates, item modifiers and serialized item NBT as relevant.
  Preserve supported Forge/custom extensions after reading their serializers.
- Review each changed command's arguments, selectors, SNBT, execution context
  and version availability. `return` accepts a signed 32-bit integer in 1.20.1.
  Do not approximate the command grammar with token counting or regexes and
  then claim a parse result. Use the 1.20.1 dispatcher for actual command parsing;
  if unavailable, identify which syntax remains untested.

### References and behavior

- Trace affected function/tag calls, schedules, predicate and loot references,
  advancement rewards and mod IDs. Resource locations without a namespace
  default to `minecraft`. Resolve dependencies across the mod and enabled packs,
  including packs sharing a namespace; absence from one folder is not proof of
  a missing resource. Check required/optional tag entries, replacement behavior
  and function-tag cycles. Function-call recursion is distinct from tag cycles;
  assess its execution cost and termination instead of rejecting all recursion.
- Trace the requested behavior through load, tick, scheduled and player-triggered
  paths. Check selector scope, server/player position and identity, predicate
  context, reload persistence, advancement retriggering, chest regeneration and
  cleanup where relevant. A loot-table item entry must name an actual registered
  item, and obtaining that stack does not prove its Isaac ability activates.

### Runtime checks and reporting

When a suitable test instance/world is available within the requested work, use
the commands relevant to the change. For example:

```text
/reload
/datapack list enabled
/function <namespace>:load
/loot give @s loot <namespace>:chests/arena
```

Run only the commands relevant to the change and inspect `latest.log` for load,
parse, registry and reference errors. Test important state transitions and item
behavior, including after reload. An available Minecraft MCP can execute these
checks; its presence alone does not establish a game connection or test result.
Use the actual 1.20.1 game/mod parser and observed behavior to resolve conflicts
with documentation or helper tools; investigate the discrepancy before editing
working files merely to satisfy a diagnostic.

Report what was inspected, the evidence or tool used, what actually ran and
what remains unresolved. Separate JSON parsing, source/document review, game
loading and gameplay tests. If no runtime check ran, report **static review
only; not tested in Minecraft**. Do not present AI self-review or old script
test results as runtime verification or as a guarantee of correctness.

For worldgen, start from the complete **1.20.1** vanilla definition or a working
1.20.1 mod definition. Check codec changes against that version; do not build an
incomplete biome/noise-settings file from a small fragment.

## Maintain this skill

- Check skill metadata and local reference links after editing. The installed
  skill-creator `quick_validate.py` checks skill packaging, not datapack validity.
- Keep the source and `.agents/skills/` copy identical, including references,
  UI metadata and removal of obsolete files. Never treat the downloaded
  upstream copy as this project's supported 1.20.1 implementation.

The upstream basis, checked Wiki sources, local game evidence and verification
limits are recorded in [references/version-and-sources.md](references/version-and-sources.md).
