package net.luojiuoscar.isaac_disaster.client.config;

import net.minecraft.network.chat.Component;

/**
 * Top-level groups shown on the Isaac Disaster config screen.
 */
public enum IsaacConfigCategory {
    ATTRIBUTE_INDICATOR("attribute_indicator", IsaacConfigDomain.CLIENT),
    PLAYER_STATS("player_stats", IsaacConfigDomain.SERVER),
    ITEM_RELATED("item_related", IsaacConfigDomain.SERVER),
    MISC("misc", IsaacConfigDomain.SERVER),
    COINS("coins", IsaacConfigDomain.SERVER);

    private final String id;
    private final IsaacConfigDomain domain;

    IsaacConfigCategory(String id, IsaacConfigDomain domain) {
        this.id = id;
        this.domain = domain;
    }

    public IsaacConfigDomain domain() {
        return domain;
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
