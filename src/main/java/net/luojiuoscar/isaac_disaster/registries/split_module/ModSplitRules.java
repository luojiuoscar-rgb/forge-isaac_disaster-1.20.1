package net.luojiuoscar.isaac_disaster.registries.split_module;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;

/** Forge registry for exceptional split composition rules. */
public final class ModSplitRules {
    public static final ResourceKey<Registry<SplitRule>> SPLIT_RULE_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "split_rule"));
    public static final DeferredRegister<SplitRule> SPLIT_RULE_REGISTRY =
            DeferredRegister.create(SPLIT_RULE_KEY, IsaacDisaster.MOD_ID);

}
