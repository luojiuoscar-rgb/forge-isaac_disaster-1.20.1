package net.luojiuoscar.isaac_disaster.registries.attack_type.combination;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPrio;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public class ModCombinationRules {
    public static final ResourceKey<Registry<AttackCombinationRule>> ATTACK_COMBINATION_RULE_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "attack_combination_rule"));

    public static final DeferredRegister<AttackCombinationRule> ATTACK_COMBINATION_RULE_REGISTRY =
            DeferredRegister.create(ATTACK_COMBINATION_RULE_KEY, IsaacDisaster.MOD_ID);

    public static final RegistryObject<AttackCombinationRule> NEPTUNUS_CURSED_EYE =
            ATTACK_COMBINATION_RULE_REGISTRY.register(
                    "neptunus_cursed_eye", () -> new AttackCombinationRule(
                            Set.of(ModAttackType.CURSED_EYE, ModAttackType.NEPTUNUS),
                            ModAttackType.NEPTUNUS, AttackPrio.NEPTUNUS.getPriority()));
}
