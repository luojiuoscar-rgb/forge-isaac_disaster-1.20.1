package net.luojiuoscar.isaac_disaster.registries.split_module.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.split_module.ModSplitModules;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitContext;
import net.minecraft.resources.ResourceLocation;

/** Cricket's Body-enhanced Compound Fracture split. */
public final class EnhancedCompoundFractureSplitModule extends CompoundFractureSplitModule {
    @Override
    public int getBulletCount() {
        return 8;
    }

    @Override
    public boolean shouldInheritChildModule(SplitContext context, ResourceLocation childModuleId,
                                            AttackContext childContext) {
        return !ModSplitModules.CRICKETS_BODY.getId().equals(childModuleId);
    }
}
