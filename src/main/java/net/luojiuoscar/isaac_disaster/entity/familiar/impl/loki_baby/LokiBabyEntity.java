package net.luojiuoscar.isaac_disaster.entity.familiar.impl.loki_baby;

import net.luojiuoscar.isaac_disaster.entity.familiar.base.baby.AbstractTearBabyFamiliarEntity;
import net.luojiuoscar.isaac_disaster.helper.GeometryHelper;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.BulletColor;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public final class LokiBabyEntity extends AbstractTearBabyFamiliarEntity {
    public LokiBabyEntity(EntityType<? extends LokiBabyEntity> type, Level level) { super(type, level); }

    @Override protected int attackIntervalTicks() { return 15; }
    @Override protected int heldAttackWindupTicks() { return 3; }
    @Override protected double tearDamageCoefficient() { return 2; }
    @Override protected RegistryObject<BulletColor> tearColor() { return ModBulletColors.BASE; }
    @Override public int getAttackDurationTicks() { return 12; }

    @Override
    protected List<Vec3> tearDirections(Vec3 aim) {
        Vec3 side = GeometryHelper.lateralAxis(aim);
        return List.of(aim, aim.scale(-1), side, side.scale(-1));
    }
}
