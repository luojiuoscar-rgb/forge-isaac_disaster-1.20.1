# 1.20.1 rules and evidence

Checked **2026-10-12**. This project skill adapts the organization and version-reference
ideas of [Jahrome907/minecraft-agent-skills](https://github.com/Jahrome907/minecraft-agent-skills/tree/dd57c5a97741cdc0eb5bb3a8f876581a4f09eeb0/.codex/skills/minecraft-datapack)
at commit `dd57c5a97741cdc0eb5bb3a8f876581a4f09eeb0`. The upstream skill targets
1.21.x/26.x. Instructions and examples are rewritten for Java/Forge 1.20.1.
This skill ships no validation scripts; verification follows the evidence-based
procedure in `SKILL.md`.

## Source-backed boundaries

| Rule | Source actually read | Consequence |
|---|---|---|
| 1.20 and 1.20.1 use data pack format 15 | [Pack format](https://minecraft.wiki/w/Pack_format), [data pack format table](https://minecraft.wiki/w/Template:Data_pack_format), [Chinese metadata history](https://zh.minecraft.wiki/w/Pack.mcmeta#版本验证) | Use `pack_format: 15` |
| Legacy resource and tag directories were renamed during 1.21 development | [Data pack history](https://minecraft.wiki/w/Data_pack#History); 24w19a and 24w21a entries | Use the listed plural folders; keep other registry names unchanged |
| Macros and backslash continuation arrived in 1.20.2 | [Function history](https://minecraft.wiki/w/Function_(Java_Edition)#History), [1.20.2](https://minecraft.wiki/w/Java_Edition_1.20.2) | No `$` lines or macro call arguments; one command per line |
| `supported_formats` and overlays arrived in 1.20.2 | [1.20.2 technical changes](https://minecraft.wiki/w/Java_Edition_1.20.2) | No later multi-version metadata in this fixed-target skill |
| Item components, component argument brackets and renamed loot functions arrived in 1.20.5 | [1.20.5 command and item changes](https://minecraft.wiki/w/Java_Edition_1.20.5), [old item format](https://minecraft.wiki/w/Item_format/Before_1.20.5) | Preserve item `tag`, byte `Count`, and `minecraft:set_nbt` / `copy_nbt` |
| New recipe component outputs and simplified ingredients are later schemas | [Recipe history](https://minecraft.wiki/w/Recipe_(Java_Edition)#History) | Use legacy ingredients and result fields, cross-checked with the 1.20.1 JAR |
| `return` exists since 1.20, but its newer execution forms do not exist in 1.20.1 | [Return history](https://minecraft.wiki/w/Commands/return#History) and local `ReturnCommand` | Allow `return <integer>`; no `return run/fail` |

The Wiki pages were read as MediaWiki API wikitext where normal pages returned
403. For repeat research, this read-only endpoint can expose the same article:

```text
https://minecraft.wiki/api.php?action=parse&page=Data_pack&prop=wikitext&format=json
```

Use a requested page's history/version sections, not its current examples as a
blanket 1.20.1 schema. Even `Item_format/Before_1.20.5` includes post-1.20.1
changes: potion/effect fields were renamed in 1.20.2. In **1.20.1**, preserve
`CustomPotionEffects`, `CustomPotionColor`, `Id`, `Amplifier`, `Duration`, etc.
as applicable; check the exact 1.20.1 serializer before editing them.

## Local 1.20.1 evidence

The creation checks also read the existing ForgeGradle 1.20.1 `client-extra.jar`
and the project's mapped Forge 47.4.9 source JAR:

| File / class | Fact checked |
|---|---|
| `version.json` | `id: "1.20.1"`, `pack_version.data: 15`, Java 17 |
| `data/minecraft/recipes/diamond_block.json` | Ingredient object with `item`; result object with `item` |
| `data/minecraft/recipes/cooked_beef.json` | Ingredient object; vanilla cooking result is a string |
| `data/minecraft/recipes/netherite_sword_smithing.json` | `template`, `base`, `addition` ingredients and legacy result |
| `data/minecraft/advancements/story/mine_diamond.json` | `display.icon.item`; item predicate `items` is an array |
| `data/minecraft/loot_tables/chests/simple_dungeon.json` | Legacy chest pool and item entry structure |
| `Ingredient`, `ShapedRecipe`, `DisplayInfo` | Actual legacy parsing, including recipe and icon field names |
| `SimpleCookingSerializer` (Forge-patched) | Forge also accepts legacy object results, using `item` and optionally `nbt` |
| `SingleItemRecipe.Serializer` | Stonecutting requires a string `result` and integer `count`; the Forge cooking extension does not apply here |
| `Commands`, `ReturnCommand`, `ExecuteCommand` | Registered 1.20.1 command surface and integer-only return form |

Locate the **1.20.1** cache by version instead of assuming another user's cache
path. For inspecting a needed entry without extracting a whole JAR:

```python
import zipfile
with zipfile.ZipFile(path_to_1_20_1_client_extra_jar) as jar:
    print(jar.read("data/minecraft/recipes/diamond_block.json").decode("utf-8"))
```

A mod's recipe serializer may intentionally extend vanilla JSON. Preserve
working Forge/custom fields, inspect the actual serializer, and identify
unverified cases rather than converting them to modern syntax.

## What the evidence does not establish

Wiki lookups and local file/codec checks do not validate a live datapack or an
Isaac Disaster item's behavior. AI review or successful JSON parsing is not a
game load test, GameTest result, multiplayer test, or performance measurement. Verify the
affected runtime surface separately and report it accurately.
