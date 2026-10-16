package net.luojiuoscar.isaac_disaster.client.config;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.config.IsaacClientConfig;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

/** SERVER edits the COMMON template for future worlds; it does not edit an existing world. */
public enum IsaacConfigDomain {
    CLIENT("client"), SERVER("server");

    private final String id;

    IsaacConfigDomain(String id) {
        this.id = id;
    }

    public Component title() {
        return Component.translatable("config.isaac_disaster.domain." + id);
    }

    public ForgeConfigSpec spec() {
        return this == CLIENT ? IsaacClientConfig.SPEC : Config.spec();
    }

    public boolean isAccessible(boolean worldLoaded) {
        return this == CLIENT || !worldLoaded;
    }
}
