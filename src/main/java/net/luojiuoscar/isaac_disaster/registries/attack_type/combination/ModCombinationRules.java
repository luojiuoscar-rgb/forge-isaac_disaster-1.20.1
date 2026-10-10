package net.luojiuoscar.isaac_disaster.registries.attack_type.combination;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPrio;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
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
                            Set.of(ModAttackTypes.CURSED_EYE, ModAttackTypes.NEPTUNUS),
                            ModAttackTypes.NEPTUNUS,
                            AttackPrio.NEPTUNUS_CURSED_EYE_COMBO.getTier(),
                            AttackPrio.NEPTUNUS_CURSED_EYE_COMBO.getPriority()));

    public static final RegistryObject<AttackCombinationRule> NEPTUNUS_LASER =
            ATTACK_COMBINATION_RULE_REGISTRY.register(
                    "neptunus_laser", () -> new AttackCombinationRule(
                            Set.of(ModAttackTypes.NEPTUNUS, ModAttackTypes.LASER),
                            ModAttackTypes.NEPTUNUS,
                            AttackPrio.NEPTUNUS_LASER_COMBO.getTier(),
                            AttackPrio.NEPTUNUS_LASER_COMBO.getPriority()));

    public static final RegistryObject<AttackCombinationRule> NEPTUNUS_HAEMOLACRIA =
            ATTACK_COMBINATION_RULE_REGISTRY.register(
                    "neptunus_haemolacria", () -> new AttackCombinationRule(
                            Set.of(ModAttackTypes.NEPTUNUS, ModAttackTypes.HAEMOLACRIA),
                            ModAttackTypes.NEPTUNUS,
                            AttackPrio.NEPTUNUS_HAEMOLACRIA_COMBO.getTier(),
                            AttackPrio.NEPTUNUS_HAEMOLACRIA_COMBO.getPriority()));

    public static final RegistryObject<AttackCombinationRule> BRIMSTONE_CURSED_EYE =
            ATTACK_COMBINATION_RULE_REGISTRY.register(
                    "brimstone_cursed_eye", () -> new AttackCombinationRule(
                            Set.of(ModAttackTypes.BRIMSTONE, ModAttackTypes.CURSED_EYE),
                            ModAttackTypes.BRIMSTONE,
                            AttackPrio.BRIMSTONE_CURSED_EYE_COMBO.getTier(),
                            AttackPrio.BRIMSTONE_CURSED_EYE_COMBO.getPriority()));

    public static final RegistryObject<AttackCombinationRule> C_SECTION_CURSED_EYE =
            ATTACK_COMBINATION_RULE_REGISTRY.register(
                    "c_section_cursed_eye", () -> new AttackCombinationRule(
                            Set.of(ModAttackTypes.C_SECTION, ModAttackTypes.CURSED_EYE),
                            ModAttackTypes.C_SECTION,
                            AttackPrio.C_SECTION_CURSED_EYE_COMBO.getTier(),
                            AttackPrio.C_SECTION_CURSED_EYE_COMBO.getPriority()));

    public static final RegistryObject<AttackCombinationRule> C_SECTION_NEPTUNUS =
            ATTACK_COMBINATION_RULE_REGISTRY.register(
                    "c_section_neptunus", () -> new AttackCombinationRule(
                            Set.of(ModAttackTypes.C_SECTION, ModAttackTypes.NEPTUNUS),
                            ModAttackTypes.C_SECTION_NEPTUNUS,
                            AttackPrio.C_SECTION_NEPTUNUS_COMBO.getTier(),
                            AttackPrio.C_SECTION_NEPTUNUS_COMBO.getPriority()));

    public static final RegistryObject<AttackCombinationRule> C_SECTION_LASER =
            ATTACK_COMBINATION_RULE_REGISTRY.register(
                    "c_section_laser", () -> new AttackCombinationRule(
                            Set.of(ModAttackTypes.C_SECTION, ModAttackTypes.LASER),
                            ModAttackTypes.C_SECTION,
                            AttackPrio.C_SECTION_LASER_COMBO.getTier(),
                            AttackPrio.C_SECTION_LASER_COMBO.getPriority()));

    public static final RegistryObject<AttackCombinationRule> HAEMOLACRIA_C_SECTION =
            ATTACK_COMBINATION_RULE_REGISTRY.register(
                    "haemolacria_c_section", () -> new AttackCombinationRule(
                            Set.of(ModAttackTypes.HAEMOLACRIA, ModAttackTypes.C_SECTION),
                            ModAttackTypes.C_SECTION,
                            AttackPrio.HAEMOLACRIA_C_SECTION_COMBO.getTier(),
                            AttackPrio.HAEMOLACRIA_C_SECTION_COMBO.getPriority()));
}
