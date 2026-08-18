package net.luojiuoscar.isaac_disaster.registries.revive_module.impl;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.revive_module.ReviveModule;

public class SimpleRevive extends ReviveModule {
    public SimpleRevive() {
        super(ModExecutableEffects.SIMPLE_REVIVE_EFFECT.get());
    }
}
