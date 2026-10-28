package net.luojiuoscar.isaac_disaster.entity.familiar.impl.brother_bobby;

import net.luojiuoscar.isaac_disaster.entity.familiar.base.baby.AbstractTearBabyFamiliarEntity;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.BulletColor;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

public final class BrotherBobbyEntity extends AbstractTearBabyFamiliarEntity {
    public BrotherBobbyEntity(EntityType<? extends BrotherBobbyEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected int attackIntervalTicks() { return 10; }

    @Override
    protected double tearDamageCoefficient() { return 2; }

    @Override
    protected RegistryObject<BulletColor> tearColor() { return ModBulletColors.BASE; }
}
