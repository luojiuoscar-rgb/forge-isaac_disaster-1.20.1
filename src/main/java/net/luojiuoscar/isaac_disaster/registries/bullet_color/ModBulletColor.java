package net.luojiuoscar.isaac_disaster.registries.bullet_color;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModBulletColor {

    public static final ResourceKey<Registry<BulletColor>> BULLET_COLOR_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "bullet_color"));

    public static final DeferredRegister<BulletColor> BULLET_COLOR_REGISTRY =
            DeferredRegister.create(BULLET_COLOR_KEY, IsaacDisaster.MOD_ID);

    /** 不一定是白色。如果颜色为Base，对应攻击方式会选择自己的默认颜色。 */
    public static final RegistryObject<BulletColor> BASE =
            BULLET_COLOR_REGISTRY.register("base", () -> new BulletColor(0xFFFFFF, 1.0f, 0));

    public static final RegistryObject<BulletColor> SPOON_BENDER =
            BULLET_COLOR_REGISTRY.register("spoon_bender", () -> new BulletColor(0x7A33C0, 1.0f, 1));

    public static final RegistryObject<BulletColor> BLOOD_TEAR =
            BULLET_COLOR_REGISTRY.register("blood_tear", () -> new BulletColor(0xCC171F, 1.0f, 5));

    public static final RegistryObject<BulletColor> POISON =
            BULLET_COLOR_REGISTRY.register("poison", () -> new BulletColor(0x5CA45C, 1.0f, 20));

    public static final RegistryObject<BulletColor> CHARM =
            BULLET_COLOR_REGISTRY.register("charm", () -> new BulletColor(0xFF33FF, 1.0f, 20));

    public static final RegistryObject<BulletColor> IPECAC =
            BULLET_COLOR_REGISTRY.register("ipecac", () -> new BulletColor(0x5CA45C, 1.0f, 100));

    public static final RegistryObject<BulletColor> SHOOP_DA_WHOOP =
            BULLET_COLOR_REGISTRY.register("white", () -> new BulletColor(0xFFFFFF, 1.0f, 100));

}
