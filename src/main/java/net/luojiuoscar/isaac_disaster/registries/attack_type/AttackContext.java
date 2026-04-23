package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

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

    private final Entity shooter;
    private final LivingEntity owner;

    public AttackContext(){
        this.colorRl = ModBulletColor.BASE.getId();
        this.trajectories = new HashMap<>();
        this.trigger = new CompositeTrigger();
        this.pos = Vec3.ZERO;
        this.xRot = 0.0f;
        this.yRot = 0.0f;
        this.shooter = null;
        this.owner = null;
    }

    public AttackContext(LivingEntity owner, Entity shooter,
                         ResourceLocation colorRl,
                         CompositeTrigger trigger,
                         Map<ResourceLocation, Integer> trajectories,
                         Vec3 pos, float xRot, float yRot) {
        this.owner = owner;
        this.shooter = shooter;
        this.colorRl = colorRl;
        // 避免外部修改影响 AttackContext 内部
        this.trigger = trigger;
        this.trajectories = new HashMap<>(trajectories);
        this.pos = pos;
        this.xRot = xRot;
        this.yRot = yRot;
    }

    public AttackContext(LivingEntity owner, Entity shooter,
                         ResourceLocation colorRl,
                         CompositeTrigger trigger,
                         Map<ResourceLocation, Integer> trajectories,
                         Vec3 pos, float xRot, float yRot, Double damage) {
        this.owner = owner;
        this.shooter = shooter;
        this.colorRl = colorRl;
        // 避免外部修改影响 AttackContext 内部
        this.trigger = trigger;
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
        return new AttackContext(
                this.owner,
                this.shooter,
                this.colorRl,
                this.trigger,
                new HashMap<>(this.trajectories),
                this.pos,
                this.xRot,
                this.yRot,
                this.damage
        );
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

    public float getDamage() {
        if (this.damage == null){
            if (this.owner == null){
                this.damage = 1.0;
            }else {
                this.damage = this.owner.getAttributeValue(Attributes.ATTACK_DAMAGE);
            }
        }

        return damage.floatValue();
    }

    @Override
    public String toString() {
        return "atkctxdmg: "+damage;
    }
}
