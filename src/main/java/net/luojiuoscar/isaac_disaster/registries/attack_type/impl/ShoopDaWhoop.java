package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.helper.GeometryHelper;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ShoopDaWhoop extends BrimstoneAttack{
    public ShoopDaWhoop(int priorityTier, double priority) {
        super(priorityTier, priority);
    }

    public ShoopDaWhoop(double priority) {
        super(priority);
    }

    private static final ResourceLocation SCHEDULE_TYPE =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "shoop_da_whoop");

    @Override
    public void shoot(AttackContext ctx) {
        AttackContext workingContext = ctx.toBuilder().build();
        // 玩家域的schedule
        ScheduledFuncHelper.scheduleForPlayer(ctx.getOwner().getUUID(),
                SCHEDULE_TYPE, 1,1, 26, true, () -> {

                    Entity s = workingContext.getShooter();
                    Vec3 eyePos = s.getEyePosition().add(0, s.getBbHeight() * -0.15, 0);
                    workingContext.setPos(eyePos);

                    if (isControllable(workingContext.getOwner())){
                        workingContext.setMainAxis(GeometryHelper.mainAxisFromRotation(s.getXRot(), s.getYRot()));
                    }

                    super.shoot(workingContext);
                });
    }

    @Override
    public double getRange(LivingEntity entity) {
        return 48;
    }
}
