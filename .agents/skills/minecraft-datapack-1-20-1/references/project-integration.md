# Isaac Disaster / Forge integration

Use this note when a datapack consumes the current mod's items or commands.

## Resolve IDs and behavior from current code

The mod namespace is `isaac_disaster` (check `gradle.properties` and the current
entrypoint if the project changes). The pack's own namespace can be `isaac_hg`
or the user's chosen name. Binding of Isaac's numeric collectible ID, a Java
enum value and a Minecraft registration ID are different identities.

Search the relevant registrations and consumers, rather than scanning every
ability:

```text
rg -n "<item name>|<enum name>" src/main/java src/main/resources
rg -n "register|ItemId|registry" src/main/java/net/luojiuoscar/isaac_disaster/item src/main/java/net/luojiuoscar/isaac_disaster/helper/IsaacItemRegistryHelper.java
```

Locate `ModItems`, `ModPassiveItems`, `ModActiveItems` and their registration
helpers in the actual current tree if these paths change. Read any inventory,
pickup, equip or collectible-acquisition handler affecting the requested item.
Check `/give` completion or live registries when a test instance is available.

Giving an `ItemStack` through `/give` or a loot table **does not prove** that the
mod's passive ability activates, its item pool is consulted, or acquisition
events run as intended. Test the actual pickup/use/equip path. A normal loot
table chooses among explicit registered entries; it does not automatically
access Isaac Disaster's Java item pools. A custom loot function or server-side
bridge is a separate implementation when existing APIs cannot supply behavior.

## What datapacks can coordinate

- Player/team state, scoreboards, tags, round phases, spawn commands, world
  border changes, chest loot assignments and scheduled pressure waves.
- Vanilla commands and existing mod commands, after checking their exact
  syntax and availability in this project's 1.20.1 command dispatcher.
- JSON data already supported by the game's or mod's registered serializers.

Datapacks cannot implement a new tear projectile, ability module, client HUD or
network packet on their own. Keep that work in the relevant mod skill when it
is part of the user's request. Do not infer item registration or abilities from
localized names or textures.

## Round and reload behavior

Load functions execute again on `/reload`; decide how existing round state and
scheduled waves should survive. Tick tags execute every tick. Avoid full-map
entity scans on each tick; scope selectors and run slower work on a controlled
cadence. Timer schedules use server ticks, and delayed functions need their own
player/position context.

Test chest contents, acquisition behavior, death/spectator transitions, wave
cleanup and reload transitions according to the actual change. Report static
checks separately from Minecraft tests; Maven/JUnit or Forge compilation does
not establish that a standalone datapack loads.
