package net.luojiuoscar.isaac_disaster.bullet.core;

import java.util.HashSet;
import java.util.Set;
import net.luojiuoscar.isaac_disaster.registries.attack_type.util.DamagedEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Mutable per-bullet collision memory, isolated from movement and trajectory state. */
public final class BulletCollisionState {
    private final DamagedEntities damagedEntities = new DamagedEntities();
    private final Set<BlockPos> hitBlockPositions = new HashSet<>();
    private int hitCooldownTicks;
    private BlockHitResult lastBlockHit;

    public BulletCollisionState(@Nullable Set<BlockPos> inheritedBlockPositions) {
        if (inheritedBlockPositions != null) {
            for (BlockPos position : inheritedBlockPositions) {
                if (position != null) hitBlockPositions.add(position.immutable());
            }
        }
    }

    public DamagedEntities damagedEntities() {
        return damagedEntities;
    }

    public int hitCooldownTicks() {
        return hitCooldownTicks;
    }

    public void setHitCooldownTicks(int ticks) {
        hitCooldownTicks = Math.max(0, ticks);
    }

    public void tickHitCooldown() {
        if (hitCooldownTicks > 0) hitCooldownTicks--;
    }

    @Nullable
    public BlockHitResult lastBlockHit() {
        return lastBlockHit;
    }

    public void lastBlockHit(@Nullable BlockHitResult value) {
        lastBlockHit = value;
    }

    public Set<BlockPos> hitBlockPositions() {
        return Set.copyOf(hitBlockPositions);
    }

    public boolean markBlockHit(@Nullable BlockPos position) {
        return position != null && hitBlockPositions.add(position.immutable());
    }
}
