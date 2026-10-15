package net.luojiuoscar.isaac_disaster.system.heart;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.entity.ExtraData;
import net.luojiuoscar.isaac_disaster.capability.entity.ExtraDataProvider;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** Stores the pending eternal-heart pickup without using a MobEffectInstance. */
public final class EternalHeartState {
    public static final int DURATION_TICKS = 3600;
    public static final ResourceLocation DATA_KEY =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "eternal_heart_remaining_ticks");

    private EternalHeartState() {
    }

    /** Arms one heart, or returns true when a second heart converts the pair immediately. */
    public static boolean grant(ExtraData data) {
        if (has(data)) {
            data.removeDouble(DATA_KEY);
            return true;
        }
        data.setDouble(DATA_KEY, DURATION_TICKS);
        return false;
    }

    /** Advances the countdown and returns true when the pending heart converts. */
    public static boolean tick(ExtraData data) {
        int remaining = remainingTicks(data);
        if (remaining <= 0) return false;
        if (remaining <= 1) {
            data.removeDouble(DATA_KEY);
            return true;
        }
        data.setDouble(DATA_KEY, remaining - 1);
        return false;
    }

    public static boolean consume(ExtraData data) {
        if (!has(data)) return false;
        data.removeDouble(DATA_KEY);
        return true;
    }

    public static boolean has(ExtraData data) {
        return remainingTicks(data) > 0;
    }

    public static int remainingTicks(ExtraData data) {
        Double saved = data.getDouble(DATA_KEY);
        if (saved == null || !Double.isFinite(saved) || saved <= 0) return 0;
        return (int) Math.min(DURATION_TICKS, Math.ceil(saved));
    }

    /** Imports an old effect instance using only its serialized duration and amplifier. */
    public static boolean importLegacy(ExtraData data, int duration, int amplifier) {
        if (amplifier > 0 || (duration >= 0 && duration <= 10)) {
            data.removeDouble(DATA_KEY);
            return true;
        }
        if (!has(data)) {
            data.setDouble(DATA_KEY, duration < 0 ? DURATION_TICKS : Math.min(DURATION_TICKS, duration));
        }
        return false;
    }

    public static boolean has(LivingEntity entity) {
        return entity.getCapability(ExtraDataProvider.EXTRA_DATA_CAP)
                .map(EternalHeartState::has)
                .orElse(false);
    }

    public static void grant(ServerPlayer player) {
        player.getCapability(ExtraDataProvider.EXTRA_DATA_CAP).ifPresent(data -> {
            if (grant(data)) activate(player);
        });
    }

    public static void tick(ServerPlayer player) {
        player.getCapability(ExtraDataProvider.EXTRA_DATA_CAP).ifPresent(data -> {
            if (tick(data)) activate(player);
        });
    }

    /** Consumes a heart for a qualifying hit and plays the original activation sound. */
    public static boolean consumeForDamage(Player player) {
        if (player.getAbsorptionAmount() != 0) return false;
        boolean consumed = player.getCapability(ExtraDataProvider.EXTRA_DATA_CAP)
                .map(EternalHeartState::consume)
                .orElse(false);
        if (!consumed) return false;

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.BLACK_HEART_ACTIVE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        return true;
    }

    public static void activate(ServerPlayer player) {
        StatManager.MAX_HEALTH.apply(player, 1);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.RED_HEART.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
