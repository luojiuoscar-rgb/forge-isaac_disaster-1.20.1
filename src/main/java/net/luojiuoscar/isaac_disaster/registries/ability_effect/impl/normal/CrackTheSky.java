package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.helper.TrackingTargetRules;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.HolyLight;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.server.HolyLightPlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID)
public final class CrackTheSky implements IAbilityEffect {
    private static final ResourceLocation SCHEDULE_TYPE =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "crack_the_sky");
    private static final int RANDOM_RETRIES = 5;
    private static final double TARGET_RADIUS = 3;

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player) || !player.isAlive() || player.isRemoved()) return false;
        double amplifier = context.getOrDefault(ContextKeys.AMPLIFIER, 1.0);
        if (!Double.isFinite(amplifier)) amplifier = 1;
        int count = (int) Math.min(Integer.MAX_VALUE, 7 * Math.max(0, Math.floor(amplifier)));
        if (count == 0) return true;
        UUID owner = player.getUUID();
        MinecraftServer server = player.getServer();
        ScheduledFuncHelper.scheduleOrAppendForPlayer(owner, SCHEDULE_TYPE, 3, count, () -> emit(server, owner));
        return true;
    }

    private static void emit(MinecraftServer server, UUID owner) {
        ServerPlayer player = server.getPlayerList().getPlayer(owner);
        if (player == null || !player.isAlive() || player.isRemoved()) {
            clear(owner);
            return;
        }
        spawn(player);
    }

    private static void spawn(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 center = player.position();
        double range = StatManager.getNearbyRange() * 0.75;
        if (!Double.isFinite(range) || range < 0) range = 0;
        double value = 4 * player.getAttributeValue(Attributes.ATTACK_DAMAGE) + 20 * StatManager.DAMAGE.getBonus();
        float damage = Double.isFinite(value) ? (float) Math.min(Float.MAX_VALUE, Math.max(0, value)) : 0;
        for (int attempt = 0; attempt <= RANDOM_RETRIES; attempt++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = Math.sqrt(level.random.nextDouble()) * range;
            Vec3 sample = center.add(Math.cos(angle) * distance, 0, Math.sin(angle) * distance);
            Optional<Vec3> ground = groundBelow(level, sample);
            if (ground.isEmpty()) continue;
            Vec3 landing = retarget(level, player, ground.get(), center, range);
            if (strike(level, player, landing, damage)) return;
        }
        Vec3 fallback = groundBelow(level, center).orElse(center);
        strike(level, player, fallback, damage);
    }

    private static boolean strike(ServerLevel level, ServerPlayer player, Vec3 position, float damage) {
        return HolyLight.strike(level, player, position, damage, HolyLight.DEFAULT_RADIUS, false, 7, 2);
    }

    private static Vec3 retarget(ServerLevel level, ServerPlayer player, Vec3 ground, Vec3 center,
                                 double range) {
        Optional<LivingEntity> target = level.getEntitiesOfClass(LivingEntity.class,
                        new AABB(ground, ground).inflate(TARGET_RADIUS), candidate ->
                                eligibleTarget(level, player, candidate)
                                        && candidate.position().distanceToSqr(ground) <= TARGET_RADIUS * TARGET_RADIUS
                                        && horizontalDistanceSquared(candidate.position(), center) <= range * range)
                .stream().min(Comparator.comparingDouble((LivingEntity entity) -> entity.position().distanceToSqr(ground))
                        .thenComparingInt(LivingEntity::getId));
        return target.flatMap(entity -> groundBelow(level, entity.position())).orElse(ground);
    }

    private static boolean eligibleTarget(ServerLevel level, ServerPlayer player, LivingEntity candidate) {
        if (!TrackingTargetRules.eligible(level, player, candidate)) return false;
        if (candidate instanceof Player other) return !other.isCreative() && !other.isSpectator();
        return candidate instanceof Enemy
                || candidate instanceof Mob mob && mob.getTarget() == player
                || candidate instanceof NeutralMob neutral && neutral.isAngryAt(player);
    }

    private static double horizontalDistanceSquared(Vec3 a, Vec3 b) {
        double x = a.x - b.x;
        double z = a.z - b.z;
        return x * x + z * z;
    }

    /** Reads one loaded column below the supplied height, keeping indoor floors and exact collision tops. */
    static Optional<Vec3> groundBelow(ServerLevel level, Vec3 position) {
        if (!Double.isFinite(position.x) || !Double.isFinite(position.y) || !Double.isFinite(position.z)) return Optional.empty();
        double topY = Math.min(position.y + 0.25, level.getMaxBuildHeight() - 0.001);
        if (topY <= level.getMinBuildHeight()) return Optional.empty();
        Vec3 from = new Vec3(position.x, topY, position.z);
        BlockPos column = BlockPos.containing(from);
        if (!level.hasChunkAt(column) || !level.getWorldBorder().isWithinBounds(column)) return Optional.empty();
        var hit = level.clip(new ClipContext(from, new Vec3(position.x, level.getMinBuildHeight(), position.z),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
        return hit.getType() == HitResult.Type.BLOCK && !hit.isInside() && hit.getDirection() == Direction.UP
                ? Optional.of(hit.getLocation().add(0, HolyLightPlacement.SURFACE_OFFSET, 0)) : Optional.empty();
    }

    private static void clear(UUID owner) {
        ScheduledFuncHelper.clearByType(SCHEDULE_TYPE, owner);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) clear(player.getUUID());
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) clear(player.getUUID());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) clear(player.getUUID());
    }

}
