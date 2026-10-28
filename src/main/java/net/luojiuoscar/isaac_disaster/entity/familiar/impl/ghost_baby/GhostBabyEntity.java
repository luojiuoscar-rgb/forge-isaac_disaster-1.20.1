package net.luojiuoscar.isaac_disaster.entity.familiar.impl.ghost_baby;

import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.entity.familiar.base.baby.AbstractTearBabyFamiliarEntity;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.BulletColor;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

public final class GhostBabyEntity extends AbstractTearBabyFamiliarEntity {
    public GhostBabyEntity(EntityType<? extends GhostBabyEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected int attackIntervalTicks() { return 15; }

    @Override
    protected double tearDamageCoefficient() { return 2; }

    @Override
    protected RegistryObject<BulletColor> tearColor() { return ModBulletColors.GHOST_TEAR; }

    @Override
    protected void configureTear(BulletState.Builder builder) { builder.spectral(true); }
}
