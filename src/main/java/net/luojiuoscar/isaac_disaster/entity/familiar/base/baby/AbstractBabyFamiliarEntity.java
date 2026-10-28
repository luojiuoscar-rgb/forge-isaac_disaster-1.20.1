package net.luojiuoscar.isaac_disaster.entity.familiar.base.baby;

import net.luojiuoscar.isaac_disaster.entity.familiar.base.chain.AbstractChainFamiliarEntity;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Chain-following babies share held-player attack scheduling while defining their own attacks and presentation. */
public abstract class AbstractBabyFamiliarEntity extends AbstractChainFamiliarEntity {
    private long nextAttackTick = Long.MIN_VALUE;
    private long lastAttackTick = Long.MIN_VALUE;

    protected AbstractBabyFamiliarEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    /** Return true only after the concrete attack has actually been created. */
    protected abstract boolean performAttack(ServerPlayer owner, Vec3 direction);

    protected abstract int attackIntervalTicks();

    /** Reference point for position aiming; model-specific familiars may use their neutral head center. */
    protected Vec3 attackOrigin() { return getBoundingBox().getCenter(); }

    /** A one-shot request aimed at a world position, sharing the same cadence as held-player fire. */
    public final boolean attackTowards(Vec3 position) {
        return attackInDirection(position.subtract(attackOrigin()));
    }

    /** A one-shot request; direction magnitude does not change the concrete attack's speed. */
    public final boolean attackInDirection(Vec3 direction) {
        double lengthSqr = direction.lengthSqr();
        if (level().isClientSide || isRecallPending() || !Double.isFinite(lengthSqr) || lengthSqr < 1e-12
                || !(getOwner() instanceof ServerPlayer owner) || owner.isSpectator() || !isValidFamiliar()) return false;
        Vec3 axis = direction.normalize();
        aimAtDirection(axis);
        long now = level().getGameTime();
        if (now == lastAttackTick || now < nextAttackTick || !performAttack(owner, axis)) return false;
        lastAttackTick = now;
        nextAttackTick = now + Math.max(0, attackIntervalTicks());
        return true;
    }

    @Override
    protected final void tickChainServer(ServerPlayer owner, Vec3 movement) {
        // Autonomous targeting is paused: babies currently follow the player's held input.
        if (!owner.isSpectator() && PlayerHelper.isHoldingIsaacHead(owner)
                && owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                .map(ability -> ability.isHoldingRightClick()).orElse(false)) {
            attackInDirection(owner.getLookAngle());
        } else if (!continueAttackAim(level().getGameTime())) {
            stopAttackAim();
            super.tickChainServer(owner, movement);
        }
    }

    protected void aimAtDirection(Vec3 direction) {
        if (direction.horizontalDistanceSqr() > 1e-8)
            setYRot(getYRot() + Mth.clamp(Mth.wrapDegrees(yaw(direction) - getYRot()), -12, 12));
    }

    protected boolean continueAttackAim(long now) { return false; }
    protected void stopAttackAim() { }
    protected void resetBabyAction() { }

    @Override
    protected final void resetChainAction() {
        resetBabyAction();
        // Recalling never grants a ready attack or a second attack in the same tick.
    }
}
