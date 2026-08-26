package net.luojiuoscar.isaac_disaster.registries.split_module;

import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.BulletSplitEvent;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackExecutor;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.BrimstoneAttack;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.LaserAttack;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.phys.Vec3;

/** Publishes and executes one split boundary for a runtime bullet object. */
public final class SplitExecutor {
    private SplitExecutor() {
    }

    public static void executeEntityHit(IBulletObject parent) {
        parent.recordSplitTrigger(SplitTriggerType.ENTITY);
        execute(parent, SplitTriggerType.ENTITY);
    }

    /**
     * Posts a split event and executes all requests produced by the event's sequence.
     * A cancelled event produces no child attacks.
     */
    public static void execute(@NotNull IBulletObject parent, @NotNull SplitTriggerType triggerType) {
        if (parent.getOwner() == null) return;

        SplitSequence sequence = parent.getSplitSequence();
        if (sequence == null || sequence.isEmpty()) return;

        AttackContext.Builder referenceBuilder = parent.getAttackContext().toBuilder()
                .position(parent.getPosition())
                .useExactSpawnPosition()
                .damage((double) parent.getDamage())
                .range(parent.getRange());
        Vec3 velocity = parent.getVelocity();
        if (velocity.lengthSqr() > 1.0E-8) {
            referenceBuilder.direction(velocity);
        }
        AttackContext referenceContext = referenceBuilder.build();
        BulletSplitEvent event = new BulletSplitEvent(parent, sequence, referenceContext, triggerType);
        if (MinecraftForge.EVENT_BUS.post(event)) return;

        for (var request : event.getSplitSequence().createChildRequests(event)) {
            if (request.getAttackType() instanceof BrimstoneAttack brimstone
                    && parent instanceof LaserAttack.LaserProjectile laser) {
                for (AttackContext childContext : request.getProvidedContexts()) {
                    brimstone.shootSingle(childContext, laser.getAttackSequenceIndex());
                }
                continue;
            }
            AttackExecutor.perform(request);
        }
    }
}
