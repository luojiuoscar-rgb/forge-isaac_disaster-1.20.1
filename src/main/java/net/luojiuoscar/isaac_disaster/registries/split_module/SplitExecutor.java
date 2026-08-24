package net.luojiuoscar.isaac_disaster.registries.split_module;

import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.BulletSplitEvent;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackExecutor;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.phys.Vec3;

/** Publishes and executes one split boundary for a runtime bullet object. */
public final class SplitExecutor {
    private SplitExecutor() {
    }

    /**
     * Posts a split event and executes all requests produced by the event's sequence.
     * A cancelled event produces no child attacks.
     */
    public static void execute(@NotNull IBulletObject parent, @NotNull SplitTriggerType triggerType) {
        if (parent.getOwner() == null) return;

        SplitSequence sequence = parent.getSplitSequence();
        if (sequence == null || sequence.isEmpty()) return;

        AttackContext referenceContext = parent.getAttackContext();
        referenceContext.setPos(parent.getPosition());
        referenceContext.useExactSpawnPosition();
        referenceContext.setDamage(parent.getDamage());
        Vec3 velocity = parent.getVelocity();
        if (velocity.lengthSqr() > 1.0E-8) {
            referenceContext.setDirection(velocity);
        }
        BulletSplitEvent event = new BulletSplitEvent(parent, sequence, referenceContext, triggerType);
        if (MinecraftForge.EVENT_BUS.post(event)) return;

        for (var request : event.getSplitSequence().createChildRequests(event)) {
            AttackExecutor.perform(request);
        }
    }
}
