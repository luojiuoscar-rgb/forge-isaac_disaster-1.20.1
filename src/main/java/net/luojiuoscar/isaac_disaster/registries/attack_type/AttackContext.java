package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitSequence;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

public class AttackContext {
    public ResourceLocation colorRl;
    private final CompositeTrigger trigger;
    public final Map<ResourceLocation, Integer> trajectories;

    private Vec3 pos;
    private float xRot;
    private float yRot;
    private float xRotOffset = 0.0f;
    private float yRotOffset = 0.0f;
    private Double damage = null;
    private SplitSequence splitSequence;

    private final Entity shooter;
    private final LivingEntity owner;

    public AttackContext(@NotNull LivingEntity owner, @Nullable Entity shooter,
                         ResourceLocation colorRl,
                         CompositeTrigger trigger,
                         Map<ResourceLocation, Integer> trajectories,
                         Vec3 pos, float xRot, float yRot) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.shooter = shooter == null ? owner : shooter;
        this.colorRl = colorRl;
        // Keep secondary attack preparation from mutating the caller's bullet trigger state.
        this.trigger = trigger.copy();
        this.trajectories = new HashMap<>(trajectories);
        this.pos = pos;
        this.xRot = xRot;
        this.yRot = yRot;
    }

    public AttackContext(@NotNull LivingEntity owner, @Nullable Entity shooter,
                         ResourceLocation colorRl,
                         CompositeTrigger trigger,
                         Map<ResourceLocation, Integer> trajectories,
                         Vec3 pos, float xRot, float yRot, Double damage) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.shooter = shooter == null ? owner : shooter;
        this.colorRl = colorRl;
        // Keep secondary attack preparation from mutating the caller's bullet trigger state.
        this.trigger = trigger.copy();
        this.trajectories = new HashMap<>(trajectories);
        this.pos = pos;
        this.xRot = xRot;
        this.yRot = yRot;
        this.damage = damage;
    }

    public CompositeTrigger getTrigger() {
        return trigger;
    }

    public AttackContext copy(){
        AttackContext copy = new AttackContext(
                this.owner,
                this.shooter,
                this.colorRl,
                this.trigger.copy(),
                new HashMap<>(this.trajectories),
                this.pos,
                this.xRot,
                this.yRot,
                this.damage
        );
        copy.setXRotOffset(this.xRotOffset);
        copy.setYRotOffset(this.yRotOffset);
        copy.splitSequence = this.splitSequence == null ? null : this.splitSequence.copy();
        return copy;
    }

    /** Returns this context's split sequence, creating an empty sequence when needed. */
    @NotNull
    public SplitSequence getSplitSequence() {
        if (splitSequence == null) splitSequence = new SplitSequence();
        return splitSequence;
    }

    /** Sets a defensive copy of the split sequence carried by this context. */
    public void setSplitSequence(@Nullable SplitSequence splitSequence) {
        this.splitSequence = splitSequence == null ? null : splitSequence.copy();
    }


    public void addSimpleTrigger(SimpleTrigger trigger) {
        this.trigger.add(trigger);
    }

    public Vec3 getPos() {
        return pos;
    }

    public void setPos(Vec3 pos) {
        this.pos = pos;
    }

    public float getXRot() {
        return xRot + xRotOffset;
    }

    public void setXRot(float xRot) {
        this.xRot = xRot;
    }

    public float getYRot() {
        return yRot + yRotOffset;
    }

    public void setYRot(float yRot) {
        this.yRot = yRot;
    }

    /** Sets the absolute firing direction and clears relative rotation offsets. */
    public void setDirection(Vec3 direction) {
        if (direction.lengthSqr() < 1.0E-8) {
            throw new IllegalArgumentException("direction must not be zero");
        }

        Vec3 normalized = direction.normalize();
        this.xRot = (float) Math.toDegrees(Math.asin(-normalized.y));
        this.yRot = (float) Math.toDegrees(Math.atan2(-normalized.x, normalized.z));
        this.xRotOffset = 0.0f;
        this.yRotOffset = 0.0f;
    }

    @NotNull
    public Entity getShooter() {
        return shooter;
    }

    @NotNull
    public LivingEntity getOwner() {
        return owner;
    }

    public void setXRotOffset(float xRotOffset) {
        this.xRotOffset = xRotOffset;
    }

    public void setYRotOffset(float yRotOffset) {
        this.yRotOffset = yRotOffset;
    }

    public float getDamage() {
        if (this.damage == null){
            this.damage = this.owner.getAttributeValue(Attributes.ATTACK_DAMAGE);
        }

        return damage.floatValue();
    }

    @Override
    public String toString() {
        return "atkctxdmg: "+damage;
    }
}
