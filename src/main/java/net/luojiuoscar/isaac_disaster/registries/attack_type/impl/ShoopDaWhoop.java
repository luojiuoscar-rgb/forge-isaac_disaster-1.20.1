package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ShoopDaWhoop extends BrimstoneAttack{
    public ShoopDaWhoop(double priority) {
        super(priority);
    }
    private static final ResourceLocation SCHEDULE_TYPE =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "shoop_da_whoop");

    @Override
    public void shoot(AttackContext ctx) {
        // 玩家域的schedule
        ScheduledFuncHelper.scheduleForPlayer(ctx.getOwner().getUUID(),
                SCHEDULE_TYPE, 1,1, 26, true, () -> {

                    Entity s = ctx.getShooter();
                    Vec3 eyePos = s.getEyePosition().add(0, s.getBbHeight() * -0.15, 0);
                    ctx.setPos(eyePos);

                    if (isControllable(ctx.getOwner())){
                        ctx.setXRot(s.getXRot());
                        ctx.setYRot(s.getYRot());
                    }

                    super.shoot(ctx);
                });
    }

    @Override
    protected double getRange(LivingEntity entity) {
        return 48;
    }
}
