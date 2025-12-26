package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Map;

public interface IBulletObject {
    float getDamage();

    Vec3 getVelocity();

    double getTraveled();

    Vec3 getPosition();

    @Nullable
    LivingEntity getOwner();

    @Nullable
    Object getShooter();

    Vec3 getPrevShooterPos();

    double getStartYRot();

    double getStartXRot();

    boolean noGravity();

    boolean isHoming();

    boolean isSpectral();

    boolean isControllable();

    boolean isPiercing();

    ResourceLocation getColorId();

    TriggerModuleQueue getTriggerModules();

    Map<ResourceLocation, Integer> getTrajectories();
}
