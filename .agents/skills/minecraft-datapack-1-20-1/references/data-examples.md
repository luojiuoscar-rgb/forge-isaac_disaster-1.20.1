# Java 1.20.1 examples

These are shapes to adapt, not a request to create or activate an entire pack.
Example namespace: `isaac_hg`. Only read the sections relevant to the task.

## Metadata, entry points and functions

`pack.mcmeta`:

```json
{"pack":{"pack_format":15,"description":"Isaac Hunger Games — 1.20.1"}}
```

`data/minecraft/tags/functions/load.json`:

```json
{"values":["isaac_hg:load"]}
```

`data/minecraft/tags/functions/tick.json`:

```json
{"values":["isaac_hg:tick"]}
```

`data/isaac_hg/functions/load.mcfunction`:

```mcfunction
scoreboard objectives add hg_state dummy
execute unless score #initialized hg_state matches 1 run function isaac_hg:init
```

`data/isaac_hg/functions/init.mcfunction`:

```mcfunction
scoreboard players set #running hg_state 0
scoreboard players set #initialized hg_state 1
```

`data/isaac_hg/functions/tick.mcfunction`:

```mcfunction
execute if score #running hg_state matches 1 run function isaac_hg:round_tick
```

`data/isaac_hg/functions/round_tick.mcfunction` may contain only a comment until
the requested round logic exists. Re-adding an existing objective during reload
can print a harmless failure; subsequent commands still run. Do not reset an
active round's state on every reload unless the requested behavior says so.

### Scheduling and execution context

```mcfunction
schedule function isaac_hg:pressure_wave 30s replace
```

Create `data/isaac_hg/functions/pressure_wave.mcfunction` before scheduling it.
Scheduled functions run from the server command context, not the original
player's position or identity. Re-establish context inside the function, e.g.:

```mcfunction
execute as @a[tag=hg_alive] at @s run function isaac_hg:spawn_pressure
```

Create `spawn_pressure.mcfunction` with the requested spawning rules. Cancel the
schedule when a round ends; re-schedule deliberately if repetition is required.
Scheduled server ticks do not advance while a single-player world is paused.

## Items and chest loot

The old item **argument** is `item{NBT}`. The old serialized **stack** has a
separate `tag` field. These two forms are not interchangeable:

```mcfunction
give @s minecraft:diamond{display:{Name:'{"text":"Arena Reward","italic":false}'}} 1
summon minecraft:item ~ ~1 ~ {Item:{id:"minecraft:diamond",Count:1b,tag:{display:{Name:'{"text":"Arena Reward","italic":false}'}}}}
```

`data/isaac_hg/loot_tables/chests/arena.json`:

```json
{
  "type":"minecraft:chest",
  "pools":[{
    "rolls":{"type":"minecraft:uniform","min":1,"max":3},
    "entries":[
      {"type":"minecraft:item","name":"minecraft:diamond","weight":1,
       "functions":[{"function":"minecraft:set_nbt",
         "tag":"{display:{Name:'{\"text\":\"Arena Reward\",\"italic\":false}'}}"}]},
      {"type":"minecraft:item","name":"minecraft:bread","weight":4}
    ]
  }]
}
```

```mcfunction
setblock ~ ~ ~ minecraft:chest{LootTable:"isaac_hg:chests/arena"}
loot give @s loot isaac_hg:chests/arena
```

Loot generally generates when the container is first accessed, consuming its
loot-table assignment. Refill/reset logic must explicitly assign another table
or replace the container; changing a JSON file alone does not refill opened
chests. For Isaac items, replace the vanilla entry only after confirming its
actual registry ID and pickup/equip behavior; read the project integration note.

## Recipes

`data/isaac_hg/recipes/reward.json`:

```json
{
  "type":"minecraft:crafting_shaped",
  "pattern":["DDD","D D","DDD"],
  "key":{"D":{"item":"minecraft:diamond"}},
  "result":{"item":"minecraft:diamond_block","count":1}
}
```

A tag ingredient is `{"tag":"minecraft:logs"}`; an ingredient with alternatives
is an array of ingredient objects. Shapeless recipes use an `ingredients` array
and the same result object. A vanilla smelting recipe uses:

```json
{
  "type":"minecraft:smelting",
  "ingredient":{"item":"minecraft:beef"},
  "result":"minecraft:cooked_beef",
  "experience":0.35,
  "cookingtime":200
}
```

In this Forge project, the patched cooking serializer also accepts a legacy
stack object result with `item` and `nbt`. Confirm its serializer when using
extensions; vanilla crafting NBT is not equivalent to arbitrary Forge serializers.
Smithing transform recipes use ingredient objects in `template`, `base`, and
`addition`, plus `result: {"item":"minecraft:netherite_sword"}`.

Stonecutting keeps a string result and a required count, including on Forge:

```json
{"type":"minecraft:stonecutting","ingredient":{"item":"minecraft:stone"},"result":"minecraft:stone_slab","count":2}
```

An empty recipe `{}` is not a valid disabling method. To filter a lower-priority
vanilla recipe, merge this sibling of `pack` into `pack.mcmeta`:

```json
{"filter":{"block":[{"namespace":"minecraft","path":"recipes/piston\\.json"}]}}
```

Filters do not remove resources in the filtering pack itself. Do not replace an
existing filter wholesale when adding a new entry.

## Advancements and predicates

`data/isaac_hg/advancements/reward.json`:

```json
{
  "display":{
    "icon":{"item":"minecraft:diamond"},
    "title":{"text":"Arena Reward"},
    "description":{"text":"Obtain a diamond"},
    "frame":"task","show_toast":true,"announce_to_chat":false,"hidden":false
  },
  "criteria":{"obtained":{"trigger":"minecraft:inventory_changed",
    "conditions":{"items":[{"items":["minecraft:diamond"]}]}}},
  "rewards":{"function":"isaac_hg:on_reward"}
}
```

Create `data/isaac_hg/functions/on_reward.mcfunction`. The reward function runs
as the player. Advancement completion is persistent; revoke it explicitly if
the requested behavior should trigger again. Ingredient objects in recipes and
item predicates in advancements have different schemas; do not interchange them.

`data/isaac_hg/predicates/daytime.json`:

```json
{"condition":"minecraft:time_check","value":{"min":0,"max":12000},"period":24000}
```

```mcfunction
execute if predicate isaac_hg:daytime run say It is daytime
```

Predicate context matters. A loot predicate referencing `this` entity needs an
entity context; a command run from the server does not automatically have a
player. Use `execute as … at @s` where the desired entity/position is required.

## Tags and item modifiers

`data/isaac_hg/tags/items/rewards.json`:

```json
{"replace":false,"values":["minecraft:diamond","minecraft:emerald"]}
```

Function tags live under `tags/functions`; block tags under `tags/blocks`.
`{"id":"other_pack:optional","required":false}` is a valid optional tag entry.
Use `replace: false` to append and `replace: true` only for intended replacement.

`data/isaac_hg/item_modifiers/reward_name.json`:

```json
[{"function":"minecraft:set_nbt","tag":"{display:{Name:'{\"text\":\"Arena Reward\"}'}}"}]
```

```mcfunction
item modify entity @s weapon.mainhand isaac_hg:reward_name
```

For additional SNBT and predicate fields, check the version reference and local
1.20.1 codecs rather than copying a current Wiki example without its history.
