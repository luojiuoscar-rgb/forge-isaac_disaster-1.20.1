package net.luojiuoscar.isaac_disaster.registries.revive_module.impl;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.revive_module.ReviveModule;

public class TotemOfUndying extends ReviveModule {
    public TotemOfUndying() {
        super(ModExecutableEffects.TOTEM_OF_UNDYING_REVIVE_EFFECT.get());
    }
}
