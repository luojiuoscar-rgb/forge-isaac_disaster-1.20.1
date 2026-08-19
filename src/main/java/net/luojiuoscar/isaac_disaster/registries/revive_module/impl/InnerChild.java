package net.luojiuoscar.isaac_disaster.registries.revive_module.impl;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.revive_module.ReviveModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class InnerChild extends ReviveModule {
    private static final ResourceLocation HUD_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "textures/hud/revive_module/inner_child.png");

    public InnerChild() {
        super(ModExecutableEffects.INNER_CHILD_REVIVE_EFFECT.get());
    }

    @Override
    public ResourceLocation getHudTexture() {
        return HUD_TEXTURE;
    }

    @Override
    public ItemStack getReviveDisplayItem() {
        return new ItemStack(ModPassiveItems.INNER_CHILD.get());
    }
}
