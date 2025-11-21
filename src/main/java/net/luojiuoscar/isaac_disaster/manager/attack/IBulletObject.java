package net.luojiuoscar.isaac_disaster.manager.attack;

import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public interface IBulletObject {
    TriggerModuleQueue getTriggerModules();

    float getDamage();

    Vec3 getVelocity();

    double getTraveled();

    Vec3 getPosition();

    @Nullable
    LivingEntity getOwner();

    double getStartYRot();

    double getStartXRot();

    boolean noGravity();

    boolean isHoming();

    boolean isSpectral();

    boolean isControllable();

    boolean isPiercing();
}
