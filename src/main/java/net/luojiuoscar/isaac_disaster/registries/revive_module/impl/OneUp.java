package net.luojiuoscar.isaac_disaster.registries.revive_module.impl;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.revive_module.ReviveModule;
import net.minecraft.resources.ResourceLocation;

public class OneUp extends ReviveModule {
    private static final ResourceLocation HUD_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "textures/hud/revive_module/one_up.png");

    public OneUp() {
        super(ModExecutableEffects.ONE_UP_REVIVE_EFFECT.get());
    }

    @Override
    public ResourceLocation getHudTexture() {
        return HUD_TEXTURE;
    }

}
