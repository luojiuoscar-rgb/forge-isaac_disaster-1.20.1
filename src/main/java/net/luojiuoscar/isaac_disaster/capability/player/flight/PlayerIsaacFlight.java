package net.luojiuoscar.isaac_disaster.capability.player.flight;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

/** Stores persistent Isaac-flight permission and transient input state for one player. */
@AutoRegisterCapability
public class PlayerIsaacFlight {
    private boolean enabled = true;
    private boolean thrusting;
    private long inputLastSeenTick = Long.MIN_VALUE;
    private boolean poseOwned;
    private double speedMultiplier = 2.0D;
    private double absoluteSpeedCap = 1.0D;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isThrusting() {
        return thrusting;
    }

    public void setThrusting(boolean thrusting) {
        this.thrusting = thrusting;
    }

    public long getInputLastSeenTick() {
        return inputLastSeenTick;
    }

    public void markInput(long gameTime) {
        inputLastSeenTick = gameTime;
    }

    public boolean isPoseOwned() {
        return poseOwned;
    }

    public void setPoseOwned(boolean poseOwned) {
        this.poseOwned = poseOwned;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public double getAbsoluteSpeedCap() {
        return absoluteSpeedCap;
    }

    public void setFlightTuning(double speedMultiplier, double absoluteSpeedCap) {
        this.speedMultiplier = Math.max(0.0D, speedMultiplier);
        this.absoluteSpeedCap = Math.max(0.0D, absoluteSpeedCap);
    }

    public void resetTransientState() {
        thrusting = false;
        inputLastSeenTick = Long.MIN_VALUE;
        poseOwned = false;
    }

    public void copyPersistentStateFrom(PlayerIsaacFlight source) {
        enabled = source.enabled;
        resetTransientState();
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putBoolean("enabled", enabled);
    }

    public void loadNBTData(CompoundTag nbt) {
        enabled = !nbt.contains("enabled") || nbt.getBoolean("enabled");
        resetTransientState();
    }
}
