package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackPlanEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.BeforePerformAttackEvent;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.MonstrosLungAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_type.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.DelegatingAttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.IChargeableAttack;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModTrajectoryModules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.Map;

public final class MonstrosLungAttack extends AttackType implements IChargeableAttack, DelegatingAttackType {
    private static final MonstrosLungAttackPattern PATTERN = new MonstrosLungAttackPattern();

    public MonstrosLungAttack(int priorityTier, double priority) {
        super(priorityTier, priority);
    }

    @Override
    public ResourceLocation getId() {
        return ModAttackTypes.MONSTROS_LUNG.getId();
    }

    @Override
    public List<AttackContext> getAttackContexts(ServerPlayer player, int bulletCount) {
        if (bulletCount <= 0) return List.of();
        AttackContext reference = player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                .map(ability -> ability.getAttackSelection().baseAttack().createAttackContext(player, player))
                .orElse(null);
        if (reference == null) return List.of();

        // Logical multishot stays at one origin and axis until planning finishes.
        List<AttackContext> contexts = new ArrayList<>(bulletCount);
        for (int i = 0; i < bulletCount; i++) contexts.add(reference.toBuilder().build());
        return contexts;
    }

    @Override
    public void performAttack(List<AttackContext> contexts, Consumer<AttackContext> onFired) {}

    @Override
    public void makeSound(LivingEntity entity) {}

    @Override
    public void shoot(AttackContext context) {}

    @Override
    public void onTick(ServerPlayer player) {
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(ability -> {
            if (ability.isHoldingRightClick()) addCharge(player, 1);
        });
    }

    @Override
    public int getTotalCharge(Player player) {
        return (int) Math.ceil(4.3 * Math.max(1.0, getShotDelay(player)));
    }

    @Override
    public void onReleased(ServerPlayer player) {
        try {
            player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(ability -> {
                if (!isChargeEligible(player)
                        || ability.getChargeAmount(getChargeBarId()) < getTotalCharge(player)) return;
                if (MinecraftForge.EVENT_BUS.post(new BeforePerformAttackEvent(player, this))) return;

                AttackType baseAttack = ability.getAttackSelection().baseAttack();
                AttackPlan plan = createAttackPlan(player);
                List<AttackContext> logicalContexts = plan.finalizeContexts();
                if (logicalContexts.isEmpty()) return;

                int copies = ability.getAttackTypes().getOrDefault(getId(), 1);
                int barrageCount = barrageCount(plan.getBaseContexts().size(), copies);
                List<AttackContext> projectiles = expandScatter(player, baseAttack,
                        groupLaunchDirections(logicalContexts), barrageCount);
                AttackExecutor.perform(AttackRequest.withContexts(player, baseAttack,
                        AttackOrigin.PLAYER_PRIMARY, AttackPipelineMode.PREPARE_AND_EXECUTE, projectiles, true));
            });
        } finally {
            IChargeableAttack.super.onReleased(player);
        }
    }

    private AttackPlan createAttackPlan(ServerPlayer player) {
        AttackRequest request = AttackRequest.generated(player, this,
                AttackOrigin.PLAYER_PRIMARY, AttackPipelineMode.PLAN_PREPARE_AND_EXECUTE, false);
        AttackPlan plan = new AttackPlan(request, getAttackContexts(player, getBulletCount(player)));
        MinecraftForge.EVENT_BUS.post(new AttackPlanEvent(request, plan));
        return plan;
    }

    private List<AttackContext> groupLaunchDirections(List<AttackContext> contexts) {
        Map<LaunchDirection, AttackContext> directions = new LinkedHashMap<>();
        for (AttackContext context : contexts) {
            directions.putIfAbsent(new LaunchDirection(context.getPos(), context.getMainAxis()), context);
        }
        return new ArrayList<>(directions.values());
    }

    private List<AttackContext> expandScatter(ServerPlayer player, AttackType baseAttack,
                                              List<AttackContext> directions, int barrageCount) {
        List<AttackContext> projectiles = new ArrayList<>(directions.size() * barrageCount);
        for (AttackContext reference : directions) {
            reference = reference.bindAttackTypeOrCopy(baseAttack);
            for (AttackContext projectile : PATTERN.generate(new AttackPatternContext(reference, barrageCount))) {
                configureProjectile(player, projectile);
                projectiles.add(projectile);
            }
        }
        return projectiles;
    }

    private void configureProjectile(ServerPlayer player, AttackContext projectile) {
        if (ModAttackTypes.BULLET.getId().equals(projectile.getRootTypeId())) {
            projectile.setBulletScale(projectile.getBulletScale()
                    * (0.90 + player.getRandom().nextDouble() * 0.43), true);
            if (projectile.getTrajectorySpecs().stream()
                    .noneMatch(spec -> spec.id().equals(ModTrajectoryModules.GRAVITY.getId()))) {
                projectile.addTrajectoryModule(ModTrajectoryModules.GRAVITY.getId(), 1);
            }
        } else if (projectile.isLaserAttack()) {
            projectile.useFixedLaunchTransform();
        }
    }

    private static int barrageCount(int logicalCount, int copies) {
        long count = 14L + Math.round(2.4 * (Math.max(1, logicalCount) - 1))
                + 12L * (Math.max(1, copies) - 1);
        return (int) Math.min(50L, count);
    }

    private record LaunchDirection(Vec3 position, Vec3 direction) {}
}
