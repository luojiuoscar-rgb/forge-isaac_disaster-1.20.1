package net.luojiuoscar.isaac_disaster.registries.bullet_color;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModBulletColors {
    public static final int DEFAULT_TEAR_COLOR = 0xE7C5E0;

    public static final ResourceKey<Registry<BulletColor>> BULLET_COLOR_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "bullet_color"));

    public static final DeferredRegister<BulletColor> BULLET_COLOR_REGISTRY =
            DeferredRegister.create(BULLET_COLOR_KEY, IsaacDisaster.MOD_ID);

    /** 不一定是白色。如果颜色为Base，对应攻击方式会选择自己的默认颜色。 */
    public static final RegistryObject<BulletColor> BASE =
            BULLET_COLOR_REGISTRY.register("base", () -> BulletColor.builder().color(DEFAULT_TEAR_COLOR).alpha(1.0f).priority(BulletColorPrio.BASE).build());

    public static final RegistryObject<BulletColor> SPECTRAL =
            BULLET_COLOR_REGISTRY.register("spectral", () -> BulletColor.builder().alpha(0.75f).priority(BulletColorPrio.SPECTRAL).build());

    public static final RegistryObject<BulletColor> REVELATION =
            BULLET_COLOR_REGISTRY.register("revelation", () -> BulletColor.builder().color(0xFFFFFF).alpha(1.0f).priority(BulletColorPrio.REVELATION).build());

    public static final RegistryObject<BulletColor> SPOON_BENDER =
            BULLET_COLOR_REGISTRY.register("spoon_bender", () -> BulletColor.builder().color(0x7A33C0).alpha(1.0f).priority(BulletColorPrio.SPOON_BENDER).build());

    public static final RegistryObject<BulletColor> DEAD_ONION =
            BULLET_COLOR_REGISTRY.register("dead_onion", () -> BulletColor.builder().color(0x79553A).priority(BulletColorPrio.DEAD_ONION).build());

    public static final RegistryObject<BulletColor> BLOOD_TEAR =
            BULLET_COLOR_REGISTRY.register("blood_tear", () -> BulletColor.builder().color(0xCC171F).alpha(1.0f).priority(BulletColorPrio.BLOOD_TEAR).build());

    public static final RegistryObject<BulletColor> PARASITE =
            BULLET_COLOR_REGISTRY.register("parasite", () -> BulletColor.builder().color(0x9D5328).alpha(1.0f).priority(BulletColorPrio.PARASITE).build());

    public static final RegistryObject<BulletColor> POISON =
            BULLET_COLOR_REGISTRY.register("poison", () -> BulletColor.builder().color(0x5CA45C).alpha(1.0f).priority(BulletColorPrio.POISON).build());

    public static final RegistryObject<BulletColor> CHARM =
            BULLET_COLOR_REGISTRY.register("charm", () -> BulletColor.builder().color(0xFF33FF).alpha(1.0f).priority(BulletColorPrio.CHARM).build());

    public static final RegistryObject<BulletColor> FEAR =
            BULLET_COLOR_REGISTRY.register("fear", () -> BulletColor.builder().color(0xFFFF55).alpha(1.0f).priority(BulletColorPrio.FEAR).build());

    public static final RegistryObject<BulletColor> SACRED_HEART =
            BULLET_COLOR_REGISTRY.register("scared_heart", () -> BulletColor.builder().color(0xFFFFFF).alpha(1.0f).priority(BulletColorPrio.SACRED_HEART).build());

    public static final RegistryObject<BulletColor> IPECAC =
            BULLET_COLOR_REGISTRY.register("ipecac", () -> BulletColor.builder().color(0x5CA45C).alpha(1.0f).priority(BulletColorPrio.IPECAC).build());

    public static final RegistryObject<BulletColor> SHOOP_DA_WHOOP =
            BULLET_COLOR_REGISTRY.register("white", () -> BulletColor.builder().color(0xFFFFFF).alpha(1.0f).priority(BulletColorPrio.SHOOP_DA_WHOOP).build());

    public static final RegistryObject<BulletColor> PETRIFIED =
            BULLET_COLOR_REGISTRY.register("petrified", () -> BulletColor.builder().color(0xB0B0B0).alpha(1.0f).priority(BulletColorPrio.PETRIFIED).build());

    public static final RegistryObject<BulletColor> DARK_MATTER =
            BULLET_COLOR_REGISTRY.register("dark_matter", () -> BulletColor.builder().color(0x191919).alpha(1.0f).priority(BulletColorPrio.DARK_MATTER).build());

    public static final RegistryObject<BulletColor> FIRE_MIND =
            BULLET_COLOR_REGISTRY.register("fire_mind", () -> BulletColor.builder().color(0xFF9933).alpha(1.0f).priority(BulletColorPrio.FIRE_MIND).build());

    public static final RegistryObject<BulletColor> HOLY_LIGHT =
            BULLET_COLOR_REGISTRY.register("holy_light", () -> BulletColor.builder().color(0x99DDFF).alpha(1.0f).priority(BulletColorPrio.HOLY_LIGHT).build());

    public static final RegistryObject<BulletColor> URANUS =
            BULLET_COLOR_REGISTRY.register("uranus", () -> BulletColor.builder().color(0xA7E8FF).alpha(1.0f).priority(BulletColorPrio.URANUS).build());

}
