package net.luojiuoscar.isaac_disaster.registries.attack_pattern;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.RingAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.SemicircleAttackPattern;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModAttackPattern {
    public static final ResourceKey<Registry<AttackPattern>> ATTACK_PATTERN_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "attack_pattern"));

    public static final DeferredRegister<AttackPattern> ATTACK_PATTERN_REGISTRY =
            DeferredRegister.create(ATTACK_PATTERN_KEY, IsaacDisaster.MOD_ID);

    public static final RegistryObject<AttackPattern> RING =
            ATTACK_PATTERN_REGISTRY.register("ring", RingAttackPattern::new);

    public static final RegistryObject<AttackPattern> SEMICIRCLE =
            ATTACK_PATTERN_REGISTRY.register("semicircle", SemicircleAttackPattern::new);

    private ModAttackPattern() {
    }
}
