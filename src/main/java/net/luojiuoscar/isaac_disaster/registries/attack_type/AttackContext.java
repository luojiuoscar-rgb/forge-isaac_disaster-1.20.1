package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColor;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttackContext {
    public ResourceLocation colorRl;
    private final List<SimpleTrigger> triggers;
    public final Map<ResourceLocation, Integer> trajectories;

    private Vec3 pos;
    private float xRot;
    private float yRot;
    private float xRotOffset = 0.0f;
    private float yRotOffset = 0.0f;

    private final Entity shooter;
    private final LivingEntity owner;

    public AttackContext(){
        this.colorRl = ModBulletColor.BASE.getId();
        this.trajectories = new HashMap<>();
        this.triggers = new ArrayList<>();
        this.pos = Vec3.ZERO;
        this.xRot = 0.0f;
        this.yRot = 0.0f;
        this.shooter = null;
        this.owner = null;
    }

    public AttackContext(LivingEntity owner, Entity shooter,
                         ResourceLocation colorRl,
                         List<SimpleTrigger> triggers,
                         Map<ResourceLocation, Integer> trajectories,
                         Vec3 pos, float xRot, float yRot) {
        this.owner = owner;
        this.shooter = shooter;
        this.colorRl = colorRl;
        // 避免外部修改影响 AttackContext 内部
        this.triggers = new ArrayList<>(triggers);
        this.trajectories = new HashMap<>(trajectories);
        this.pos = pos;
        this.xRot = xRot;
        this.yRot = yRot;

    }

    public List<SimpleTrigger> getTriggers() {
        return triggers;
    }

    public AttackContext copy(){
        return new AttackContext(
                this.owner,
                this.shooter,
                this.colorRl,
                this.triggers,
                new HashMap<>(this.trajectories),
                this.pos,
                this.xRot,
                this.yRot
        );
    }

    public void addSimpleTrigger(SimpleTrigger trigger) {
        this.triggers.add(trigger);
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

    public Entity getShooter() {
        return shooter;
    }

    public LivingEntity getOwner() {
        return owner;
    }

    public void setXRotOffset(float xRotOffset) {
        this.xRotOffset = xRotOffset;
    }

    public void setYRotOffset(float yRotOffset) {
        this.yRotOffset = yRotOffset;
    }
}
