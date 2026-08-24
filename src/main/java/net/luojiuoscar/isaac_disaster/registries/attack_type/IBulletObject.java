package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerCounts;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerType;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitSequence;
import net.luojiuoscar.isaac_disaster.registries.attack_type.util.DamagedEntities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public interface IBulletObject {
    @NotNull
    SplitSequence getSplitSequence();

    AttackContext getAttackContext();

    @NotNull
    SplitTriggerCounts getSplitTriggerCounts();

    void recordSplitTrigger(SplitTriggerType type);

    float getDamage();

    Vec3 getVelocity();

    double getTraveled();

    int getTotalLifeTick();

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

    Map<ResourceLocation, Integer> getTrajectories();

    CompositeTrigger getTriggers();

    DamagedEntities getDamagedEntities();


}
