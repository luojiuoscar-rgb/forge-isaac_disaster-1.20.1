package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** C Section + Neptunus: the C Section attack with automatic charge and firing. */
public final class CSectionNeptunusAttack extends CSectionAttack {
    public CSectionNeptunusAttack(int priorityTier, double priority) {
        super(priorityTier, priority);
    }

    @Override
    public ResourceLocation getId() {
        return ModAttackTypes.C_SECTION.getId();
    }

    @Override
    public void onTick(ServerPlayer player) {
        tickChargeAndFire(player, false);
    }

    /** Automatic charging makes input transitions informational rather than cancellation events. */
    @Override
    public void onPressed(ServerPlayer player) {
    }

    @Override
    public void onReleased(ServerPlayer player) {
    }
}
