package net.luojiuoscar.isaac_disaster.entity.tnt;

import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeExecutableEffect;
import net.luojiuoscar.isaac_disaster.system.explosion.CustomExplosion;
import net.luojiuoscar.isaac_disaster.system.explosion.ExplosionParameters;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class GigaBomb extends IsaacBomb {
    public GigaBomb(Level pLevel, double pX, double pY, double pZ, @Nullable LivingEntity pOwner, int power, float scale) {
        super(pLevel, pX, pY, pZ, pOwner, power, scale);
    }

    public GigaBomb(EntityType<IsaacBomb> isaacBombEntityType, Level level) {
        super(isaacBombEntityType, level);
    }

    @Override
    protected void explode() {
        if (!(level() instanceof ServerLevel level)) return;

        LevelHelper.explodeCustom(level, position(), level.damageSources().explosion(this, getOwner()),
                ExplosionParameters.giga(getPower()));
        discard();
    }

    /** Compatibility helper retaining Giga's protected-block rules in the shared ray sampler. */
    public Set<BlockPos> calculateExplosionNoResistance(Level level, Vec3 center, float radius) {
        ExplosionParameters parameters = ExplosionParameters.giga(radius);
        Explosion explosion = new Explosion(level, this,
                level.damageSources().explosion(this, getOwner()), null,
                center.x, center.y, center.z, parameters.power(), parameters.causesFire(),
                parameters.blockInteraction());
        return CustomExplosion.calculateBlocksToBlow(level, explosion, center, parameters);
    }

    /** Item attachments are discarded so Giga never participates in regular bomb synergies. */
    @Override
    public CompositeExecutableEffect getCachedEffect() {
        return new CompositeExecutableEffect();
    }
}
