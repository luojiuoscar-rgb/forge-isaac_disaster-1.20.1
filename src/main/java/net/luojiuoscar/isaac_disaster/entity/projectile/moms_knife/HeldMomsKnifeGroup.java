package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.MomsKnifeAttack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Coordinates held slots without owning projectile state, a global queue, or saved player data. */
public final class HeldMomsKnifeGroup {
    public static final int MAX_KNIVES = 16;

    private HeldMomsKnifeGroup() {}

    public record Layout(Vec3 offset, Vec3 direction) {}

    public static Layout layout(int count, int slot, double radius) {
        count = Mth.clamp(count, 1, MAX_KNIVES);
        slot = Mth.clamp(slot, 0, count - 1);
        if (count == 2) return new Layout(new Vec3(slot == 0 ? 0.25 : -0.25, 0, 0), PlayerMomsKnifeMotion.FORWARD);
        double angle = Math.toRadians(((count - 1) / 2.0 - slot) * 2 * Math.max(11 - count, 5));
        Vec3 direction = new Vec3(Math.sin(angle), 0, Math.cos(angle));
        return new Layout(direction.subtract(PlayerMomsKnifeMotion.FORWARD).scale(radius), direction);
    }

    public static int heldCount(ServerPlayer player) {
        return Mth.clamp((int) player.getAttributeValue(ModAttributes.BULLET_COUNT.get()), 1, MAX_KNIVES);
    }

    public static List<PlayerMomsKnifeEntity> knives(ServerPlayer player) {
        return player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).map(ability -> {
            List<PlayerMomsKnifeEntity> knives = new ArrayList<>();
            for (int slot = 0; slot < MAX_KNIVES; slot++) {
                PlayerMomsKnifeEntity knife = ability.getRuntimeEntities().getEntity(
                        ModAttackTypes.MOMS_KNIFE.getId(), slot, player.serverLevel(), PlayerMomsKnifeEntity.class);
                if (knife != null && knife.getOwner() == player && knife.getMode() == PlayerMomsKnifeEntity.Mode.RETURNING) {
                    knives.add(knife);
                }
            }
            return knives;
        }).orElseGet(List::of);
    }

    public static boolean isInFlight(ServerPlayer player) {
        return knives(player).stream().anyMatch(PlayerMomsKnifeEntity::isInFlight);
    }

    public static List<PlayerMomsKnifeEntity> refresh(ServerPlayer player) {
        if (!MomsKnifeAttack.isSelectedAndReady(player)) return List.of();
        List<PlayerMomsKnifeEntity> current = knives(player);
        if (current.stream().anyMatch(PlayerMomsKnifeEntity::isInFlight)) return current;
        int count = heldCount(player);
        for (var knife : current) if (knife.getHeldSlot() >= count) knife.discard();
        List<PlayerMomsKnifeEntity> result = new ArrayList<>();
        for (int slot = 0; slot < count; slot++) {
            Layout layout = layout(count, slot, Math.max(0.6, player.getBbWidth()));
            PlayerMomsKnifeEntity knife = getOrCreate(player, slot, layout);
            if (knife != null) {
                knife.configureHeld(slot, layout.offset(), layout.direction());
                result.add(knife);
            }
        }
        return result;
    }

    @Nullable
    private static PlayerMomsKnifeEntity getOrCreate(ServerPlayer player, int slot, Layout layout) {
        return player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).map(ability -> {
            var entities = ability.getRuntimeEntities();
            var id = ModAttackTypes.MOMS_KNIFE.getId();
            PlayerMomsKnifeEntity knife = entities.getEntity(id, slot, player.serverLevel(), PlayerMomsKnifeEntity.class);
            if (knife != null && (knife.getOwner() != player || knife.getMode() != PlayerMomsKnifeEntity.Mode.RETURNING
                    || knife.getHeldSlot() != slot)) {
                entities.unregister(id, slot, knife.getUUID());
                knife = null;
            }
            if (knife == null) {
                knife = new PlayerMomsKnifeEntity(ModEntities.PLAYER_MOMS_KNIFE.get(), player.level());
                knife.initialize(player, slot, layout.offset(), layout.direction());
                if (!player.serverLevel().addFreshEntity(knife)) return null;
                entities.register(id, slot, knife.getUUID());
            }
            return knife;
        }).orElse(null);
    }

    /** Launches eligible slots after the caller's group check, returning the first success or null. */
    @Nullable
    public static AttackContext launch(ServerPlayer player, List<AttackContext> contexts, double charge, int barrageCount) {
        AttackContext firstLaunched = null;
        Vec3 idle = PlayerMomsKnifeMotion.idlePosition(player, 1);
        for (int slot = 0; slot < Math.min(contexts.size(), MAX_KNIVES); slot++) {
            AttackContext context = contexts.get(slot);
            if (context.getOwner() != player || !Double.isFinite(context.getPos().lengthSqr())) continue;
            Layout layout = new Layout(PlayerMomsKnifeMotion.offsetToLocal(context.getPos().subtract(idle),
                    player.getYRot(), player.getXRot()),
                    PlayerMomsKnifeMotion.toLocal(context.getMainAxis(), player.getYRot(), player.getXRot()));
            PlayerMomsKnifeEntity knife = getOrCreate(player, slot, layout);
            if (knife == null) continue;
            knife.configureHeld(slot, layout.offset(), layout.direction());
            if (knife.launch(PlayerMomsKnifeMotion.range(context.getBulletRange(), charge), context.getBulletSpeed(),
                    context, firstLaunched == null ? barrageCount : 0)) {
                if (firstLaunched == null) firstLaunched = context;
            }
        }
        return firstLaunched;
    }
}
