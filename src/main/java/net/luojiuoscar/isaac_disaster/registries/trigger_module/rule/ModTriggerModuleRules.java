package net.luojiuoscar.isaac_disaster.registries.trigger_module.rule;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.rule.impl.BlackCandleCursedEyeRule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.rule.impl.BlackCandleCurseOfTheTowerRule;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModTriggerModuleRules {
    /** Forge registry that allows add-on mods to register trigger module rules. */
    public static final ResourceKey<Registry<TriggerModuleRule>> TRIGGER_MODULE_RULE_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "trigger_module_rule"));
    public static final DeferredRegister<TriggerModuleRule> TRIGGER_MODULE_RULE_REGISTRY =
            DeferredRegister.create(TRIGGER_MODULE_RULE_KEY, IsaacDisaster.MOD_ID);

    public static final RegistryObject<TriggerModuleRule> BLACK_CANDLE_CURSED_EYE =
            TRIGGER_MODULE_RULE_REGISTRY.register("black_candle_cursed_eye", BlackCandleCursedEyeRule::new);
    public static final RegistryObject<TriggerModuleRule> BLACK_CANDLE_CURSE_OF_THE_TOWER =
            TRIGGER_MODULE_RULE_REGISTRY.register("black_candle_curse_of_the_tower", BlackCandleCurseOfTheTowerRule::new);

    private ModTriggerModuleRules() {
    }
}
