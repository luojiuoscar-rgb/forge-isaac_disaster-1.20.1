package net.luojiuoscar.isaac_disaster.loot;

/**
 * Describes why a loot table is being rolled.
 *
 * <p>Minecraft's {@code LootContext} describes data used by the table, such as the entity and
 * position. This enum describes this mod's intent for the roll, so global loot modifiers can
 * decide whether they should participate.</p>
 */
public enum LootGenerationMode {
    /**
     * Vanilla, datapack, or other mod loot generation that did not enter through this mod's helper.
     */
    NATURAL_DROP,

    /**
     * Loot intentionally spawned into the world by this mod.
     */
    SPAWN_DROP,

    /**
     * Items directly given to a player. This mode is reserved for future helper entry points that
     * should not behave like world drops.
     */
    DIRECT_GIVE,

    /**
     * Extra loot generated as a child of another loot effect.
     */
    DERIVED_DROP,

    /**
     * Internal rolls used to replace, reroll, or duplicate an existing loot result.
     */
    REPLACEMENT_ROLL,

    /**
     * Raw table access that should bypass Isaac-specific loot modification.
     */
    RAW_ROLL
}
