package net.luojiuoscar.isaac_disaster.entity.familiar.base.chain;

import net.luojiuoscar.isaac_disaster.entity.familiar.base.AbstractIsaacFamiliarEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Ordered following, movement interpolation and recall, independent of attacks and familiar category. */
public abstract class AbstractChainFamiliarEntity extends AbstractIsaacFamiliarEntity {
    private final ChainFamiliarMotion motion = new ChainFamiliarMotion();

    protected AbstractChainFamiliarEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && isRecallPending()) resetAction();
    }

    @Override
    protected final void tickFamiliar() {
        if (level().isClientSide) {
            setPos(motion.tickPosition(position()));
            motion.turnTo(getYRot());
            tickChainClient();
            return;
        }
        if (!(getOwner() instanceof ServerPlayer owner) || !owner.isAlive()) {
            resetAction();
            return;
        }
        refreshFormationIndex(owner, getFormationIndex());
        Vec3 next = motion.follow(position(), followLink(owner).target());
        Vec3 movement = next.subtract(position());
        setDeltaMovement(movement);
        setPos(next);
        tickChainServer(owner, movement);
    }

    /** Runs after the server commits this tick's movement. Pure followers need no extra behavior. */
    protected void tickChainServer(ServerPlayer owner, Vec3 movement) {
        if (movement.horizontalDistanceSqr() > 1e-8) setYRot(yaw(movement));
    }

    protected static float yaw(Vec3 direction) {
        return (float) (Mth.atan2(-direction.x, direction.z) * Mth.RAD_TO_DEG);
    }

    /** Presentation hooks deliberately leave bone layout and animation timing to the concrete familiar. */
    protected void tickChainClient() { }
    protected void resetChainAction() { }
    protected void onClientPositionReset() { }

    private void resetAction() {
        motion.reset();
        setDeltaMovement(Vec3.ZERO);
        resetChainAction();
    }

    @Override
    protected final void onRecalled() {
        if (getOwner() instanceof ServerPlayer owner) refreshFormationIndex(owner, getFormationIndex());
        resetAction();
    }

    @Override
    public final void lerpTo(double x, double y, double z, float yaw, float pitch, int steps, boolean absolute) {
        Vec3 target = absolute ? new Vec3(x, y, z) : getPositionCodec().decode(0, 0, 0);
        if (!level().isClientSide || motion.acceptPosition(position(), target, steps, absolute)) {
            super.lerpTo(x, y, z, yaw, pitch, steps, absolute);
            motion.reset();
            motion.turnTo(yaw);
            onClientPositionReset();
            setOldPosAndRot();
            return;
        }
        // Rotation-only packets use the codec position, preserving a pending movement target.
        setYRot(yaw);
    }

    public final float getRenderYaw(float partialTick) {
        return level().isClientSide ? motion.renderYaw(partialTick) : getYRot();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distanceSquared) {
        double distance = 128 * getViewScale();
        return distanceSquared <= distance * distance;
    }

    @Override
    protected int resolveFormationIndex(ServerPlayer owner, int typeLocalIndex) {
        return followLink(owner).slot();
    }

    private FamiliarChain.Link followLink(ServerPlayer owner) {
        return FamiliarChain.link(owner, this);
    }

    @Override
    protected Vec3 ownerPlacement(Player owner) {
        return owner instanceof ServerPlayer player ? followLink(player).placement() : super.ownerPlacement(owner);
    }

    @Override
    protected boolean exceedsRecallDistance(Player owner) {
        if (!super.exceedsRecallDistance(owner)) return false;
        return !(owner instanceof ServerPlayer player)
                || position().distanceToSqr(followLink(player).target()) > MAX_VALID_DISTANCE * MAX_VALID_DISTANCE;
    }
}
