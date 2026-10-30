package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.TagManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.List;

public final class D10 implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        LivingEntity source = context.getEntity();
        if (!source.isAlive() || !(source.level() instanceof ServerLevel level)) return false;

        List<EntityType<?>> candidates = BuiltInRegistries.ENTITY_TYPE.stream()
                .filter(type -> isCandidate(type, level)).toList();
        if (candidates.isEmpty()) return false;

        Vec3 center = source.position();
        double radius = StatManager.getNearbyRange();
        List<Mob> targets = level.getEntitiesOfClass(Mob.class,
                new AABB(center, center).inflate(radius + 1.0e-6),
                target -> eligible(source, target, level) && inRange(center, target.position(), radius));
        for (Mob target : targets) {
            if (!eligible(source, target, level)) continue;
            EntityType<?> type = candidates.get(level.getRandom().nextInt(candidates.size()));
            replace(level, target, type);
        }
        return true;
    }

    static boolean isCandidate(EntityType<?> type, ServerLevel level) {
        // Forge 1.20.1 getBaseClass() is a stub; inspect an unspawned instance once per type and use.
        return type.canSummon() && DefaultAttributes.hasSupplier(type)
                && !type.is(TagManager.D10_EXCLUDED_ENTITIES) && type.create(level) instanceof Mob;
    }

    static boolean eligible(LivingEntity source, LivingEntity target, ServerLevel level) {
        return target instanceof Mob && target.isAlive() && target.level() == level
                && !target.getType().is(TagManager.D10_EXCLUDED_ENTITIES)
                && !EntityHelper.isFriendly(source, target) && level.hasChunkAt(target.blockPosition());
    }

    static boolean inRange(Vec3 center, Vec3 position, double radius) {
        return center.distanceToSqr(position) <= radius * radius;
    }

    static float replacementHealth(float health, float maximum, float newMaximum) {
        return newMaximum * Mth.clamp(health / maximum, 0, 1);
    }

    static boolean replace(ServerLevel level, Mob original, EntityType<?> type) {
        if (!level.hasChunkAt(original.blockPosition())) return false;
        if (!(type.create(level) instanceof Mob replacement)) return false;
        replacement.moveTo(original.getX(), original.getY(), original.getZ(), original.getYRot(), original.getXRot());
        ForgeEventFactory.onFinalizeSpawn(replacement, level, level.getCurrentDifficultyAt(original.blockPosition()),
                MobSpawnType.CONVERSION, null, null);
        if (replacement.isSpawnCancelled()) return false;
        // addFreshEntity registers only this entity; detach spawn-time riders without a dismount-position search.
        replacement.removeVehicle();
        replacement.getPassengers().forEach(Entity::removeVehicle);
        if (replacement.isPassenger() || replacement.isVehicle()) return false;
        // Initialization can reset rotation, and types such as shulkers snap setPos() to the block grid.
        AABB bounds = replacement.getBoundingBox().move(original.position().subtract(replacement.position()));
        replacement.setPosRaw(original.getX(), original.getY(), original.getZ());
        replacement.setBoundingBox(bounds);
        replacement.setYRot(original.getYRot());
        replacement.setXRot(original.getXRot());
        replacement.setYHeadRot(original.getYRot());
        replacement.setOldPosAndRot();
        replacement.setHealth(replacementHealth(original.getHealth(), original.getMaxHealth(), replacement.getMaxHealth()));
        if (!level.addFreshEntity(replacement)) return false;

        original.removeVehicle();
        original.getPassengers().forEach(Entity::removeVehicle);
        if (original.isPassenger() || original.isVehicle()) {
            replacement.discard();
            return false;
        }
        original.discard();
        return true;
    }
}
