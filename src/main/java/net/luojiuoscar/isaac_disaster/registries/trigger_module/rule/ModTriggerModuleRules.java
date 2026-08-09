package net.luojiuoscar.isaac_disaster.registries.trigger_module.rule;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;

public final class ModTriggerModuleRules {
    public static final ResourceKey<Registry<TriggerModuleRule>> TRIGGER_MODULE_RULE_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(
                    IsaacDisaster.MOD_ID, "trigger_module_rule"));
    public static final DeferredRegister<TriggerModuleRule> TRIGGER_MODULE_RULE_REGISTRY =
            DeferredRegister.create(TRIGGER_MODULE_RULE_KEY, IsaacDisaster.MOD_ID);

    private ModTriggerModuleRules() {
    }
}
