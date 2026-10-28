package net.luojiuoscar.isaac_disaster.entity.familiar.impl.sister_maggy;

import net.luojiuoscar.isaac_disaster.entity.familiar.base.baby.AbstractTearBabyFamiliarEntity;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.BulletColor;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

public final class SisterMaggyEntity extends AbstractTearBabyFamiliarEntity {
    public SisterMaggyEntity(EntityType<? extends SisterMaggyEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected int attackIntervalTicks() { return 15; }

    @Override
    protected double tearDamageCoefficient() { return 3; }

    @Override
    protected RegistryObject<BulletColor> tearColor() { return ModBulletColors.BLOOD_TEAR; }
}
