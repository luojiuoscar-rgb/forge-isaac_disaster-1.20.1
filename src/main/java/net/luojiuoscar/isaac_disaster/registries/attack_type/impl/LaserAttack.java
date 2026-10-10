package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.BasicAttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.LaserAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

import java.util.List;

public class LaserAttack extends AbstractLaserAttack implements BasicAttackType {
    private static final LaserAttackPattern PATTERN = new LaserAttackPattern();
    private static final double BASE_LASER_WIDTH = 0.25D;

    public LaserAttack(int priorityTier, double priority) {
        super(priorityTier, priority);
    }

    public LaserAttack(double priority) {
        super(priority);
    }

    @Override
    public List<AttackContext> getAttackContexts(ServerPlayer player, int bulletCount) {
        AttackContext ctx = createAttackContext(player, player);
        if (ctx == null) return List.of();
        return PATTERN.generate(new AttackPatternContext(ctx, bulletCount));
    }

    @Override
    public void performAttack(List<AttackContext> ctxList) {
        performLaserBatch(ctxList, ignored -> 0);
    }

    @Override
    public void makeSound(LivingEntity entity) {
        entity.level().playSound(null, entity.blockPosition(), ModSounds.LASER_SHOT.get(),
                SoundSource.PLAYERS, 0.6f, 1.0f);
    }

    @Override
    protected double getWidth(AttackContext context) {
        return laserWidth(context.getBulletScale(), BASE_LASER_WIDTH);
    }

    @Override
    protected Vector3f getDefaultLaserColor() {
        return new Vector3f(1.0F, 0.0F, 0.0F);
    }

    @Override
    public ResourceLocation getId() {
        return ModAttackTypes.LASER.getId();
    }
}
