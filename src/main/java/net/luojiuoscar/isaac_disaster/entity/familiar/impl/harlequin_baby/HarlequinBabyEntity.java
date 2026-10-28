package net.luojiuoscar.isaac_disaster.entity.familiar.impl.harlequin_baby;

import net.luojiuoscar.isaac_disaster.entity.familiar.base.baby.AbstractTearBabyFamiliarEntity;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.BulletColor;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public final class HarlequinBabyEntity extends AbstractTearBabyFamiliarEntity {
    public HarlequinBabyEntity(EntityType<? extends HarlequinBabyEntity> type, Level level) { super(type, level); }

    @Override protected int attackIntervalTicks() { return 15; }
    @Override protected double tearDamageCoefficient() { return 3; }
    @Override protected RegistryObject<BulletColor> tearColor() { return ModBulletColors.BASE; }

    @Override
    protected List<Vec3> tearDirections(Vec3 aim) {
        Vec3 side = aim.cross(new Vec3(0, 1, 0));
        if (side.lengthSqr() < 1e-12) side = Vec3.directionFromRotation(0, getYRot() + 90);
        Vec3 forward = aim.scale(Math.cos(Math.PI / 12));
        Vec3 spread = side.normalize().scale(Math.sin(Math.PI / 12));
        return List.of(forward.add(spread), forward.subtract(spread));
    }
}
