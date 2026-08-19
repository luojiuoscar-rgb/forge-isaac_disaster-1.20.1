package net.luojiuoscar.isaac_disaster.registries.revive_module;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IExecutableEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public abstract class ReviveModule {
    private static final ResourceLocation DEFAULT_HUD_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "textures/hud/revive_module/totem_of_undying.png");

    private final IExecutableEffect reviveEffect;

    protected ReviveModule(IExecutableEffect reviveEffect) {
        this.reviveEffect = reviveEffect == null ? IExecutableEffect.EMPTY : reviveEffect;
    }

    public double getPriority() {
        return 0.0;
    }

    public ResourceLocation getHudTexture() {
        return DEFAULT_HUD_TEXTURE;
    }

    public IExecutableEffect getReviveEffect() {
        return reviveEffect;
    }

    public SoundEvent getSound() {
        return SoundEvents.TOTEM_USE;
    }

    public ItemStack getReviveDisplayItem() {
        return new ItemStack(Items.TOTEM_OF_UNDYING);
    }
}
