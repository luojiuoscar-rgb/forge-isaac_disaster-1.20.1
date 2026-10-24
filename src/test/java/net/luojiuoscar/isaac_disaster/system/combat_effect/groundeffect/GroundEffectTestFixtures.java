package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect;

import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.AbstractGroundEffect;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.impl.WaterPuddleEffect;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.impl.LemonPuddleEffect;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.server.GroundEffectManager;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.GroundEffectType;
import net.luojiuoscar.isaac_disaster.networking.packet.groundeffect.GroundEffectSnapshotBatchS2CPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

/** Local factories and explicit wire fixtures; no Forge registry is bootstrapped in JUnit. */
public final class GroundEffectTestFixtures {
    public static final GroundEffectType<WaterPuddleEffect> WATER = new GroundEffectType<>(WaterPuddleEffect::new);
    public static final GroundEffectType<LemonPuddleEffect> LEMON = new GroundEffectType<>(LemonPuddleEffect::new);

    public static WaterPuddleEffect water(GroundEffectManager manager, UUID owner, Vec3 position) {
        return water(manager, owner, position, WaterPuddleEffect.DEFAULT_RADIUS, WaterPuddleEffect.DEFAULT_LIFETIME, 0);
    }

    public static WaterPuddleEffect water(GroundEffectManager manager, UUID owner, Vec3 position,
                                         double radius, int lifetime, float damage) {
        return manager.spawn(WATER, owner, position, radius, lifetime, damage);
    }

    public static GroundEffectSnapshotBatchS2CPacket.Entry snapshot(AbstractGroundEffect effect) {
        return new GroundEffectSnapshotBatchS2CPacket.Entry(effect.slot(), effect.generation(), effect.position(),
                effect.radius(), effect.age(), effect.lifetime(), effect.creationOrder(), effect.ownerUuid(),
                ResourceLocation.parse(effect.getType() == LEMON ? "test:lemon" : "test:water"),
                effect.getColor(), effect.shapeSeed(), effect.revision(),
                effect.animationDurationTicks(), effect.animationElapsedTicks());
    }

    private GroundEffectTestFixtures() { }
}
