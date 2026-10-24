package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.impl;

import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.AbstractGroundEffect;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.GroundEffectContact;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.GroundEffectType;
import net.luojiuoscar.isaac_disaster.system.damage.ModDamageSources;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class LemonPuddleEffect extends AbstractGroundEffect {

    public LemonPuddleEffect(GroundEffectType<LemonPuddleEffect> type, int slot, int generation,
                              @Nullable UUID owner, Vec3 position, double radius, int lifetime, float damage, long order, int animationDurationTicks) {
        super(type, slot, generation, owner, position, radius, lifetime, damage, order, animationDurationTicks);
    }

    @Override
    public int getColor() { return ColorManager.LEMON; }

    @Override
    public void onContactDamage(GroundEffectContact contact) {
        var target = contact.target();
        if (!target.isAlive() || target.isInvulnerable() || damage() <= 0.0F) return;
        target.hurt(ModDamageSources.water(contact.level(), contact.owner()), damage());
    }
}
