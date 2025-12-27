package net.luojiuoscar.isaac_disaster.manager;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public class ModDamageType {
    public static final ResourceKey<DamageType> TEAR =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "tear")
            );

    public static final ResourceKey<DamageType> LASER =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "laser")
            );
}
