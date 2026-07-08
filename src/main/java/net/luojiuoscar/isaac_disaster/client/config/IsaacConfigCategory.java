package net.luojiuoscar.isaac_disaster.client.config;

import net.minecraft.network.chat.Component;

/**
 * Top-level groups shown on the Isaac Disaster config screen.
 */
public enum IsaacConfigCategory {
    PLAYER_STATS("player_stats"),
    ITEM_RELATED("item_related"),
    MISC("misc"),
    COINS("coins");

    private final String id;

    IsaacConfigCategory(String id) {
        this.id = id;
    }

    /**
     * Returns the stable category id used by translation keys.
     */
    public String id() {
        return id;
    }

    /**
     * Returns the translated category title.
     */
    public Component title() {
        return Component.translatable("config.isaac_disaster.category." + id);
    }
}
