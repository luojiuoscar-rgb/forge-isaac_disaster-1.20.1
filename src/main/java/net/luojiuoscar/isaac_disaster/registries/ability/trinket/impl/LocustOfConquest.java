package net.luojiuoscar.isaac_disaster.registries.ability.trinket.impl;

import net.luojiuoscar.isaac_disaster.helper.DescriptionHelper;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.ModPeriodicModules;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.impl.ConquestLocustGeneration;
import net.minecraft.network.chat.Component;

public final class LocustOfConquest extends LocustTrinket {
    public LocustOfConquest(int id, int level) {
        super(id, level, ModPeriodicModules.LOCUST_OF_CONQUEST);
    }

    @Override
    protected Component getGenerationAmount(int amplifier) {
        return Component.translatable("item.isaac_disaster.locust_trinket.count_range",
                DescriptionHelper.dynamicNumber(amplifier),
                DescriptionHelper.dynamicNumber(ConquestLocustGeneration.getMaximumCount(amplifier)));
    }
}
