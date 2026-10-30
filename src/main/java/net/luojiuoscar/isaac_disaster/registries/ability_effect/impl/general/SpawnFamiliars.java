package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

import java.util.Objects;
import java.util.function.Supplier;

/** Generates admitted familiars immediately; active uses can repeat independent random batches. */
public final class SpawnFamiliars implements IAbilityEffect {
    private final Supplier<ResourceLocation> type;
    private final int minimumCount;
    private final int maximumCount;
    private final boolean repeatWithAmplifier;

    public SpawnFamiliars(Supplier<ResourceLocation> type, int minimumCount, int maximumCount,
                          boolean repeatWithAmplifier) {
        this.type = Objects.requireNonNull(type);
        this.minimumCount = Math.max(0, Math.min(64, minimumCount));
        this.maximumCount = Math.max(this.minimumCount, Math.min(64, maximumCount));
        this.repeatWithAmplifier = repeatWithAmplifier;
    }

    public int minimumCount() { return minimumCount; }
    public int maximumCount() { return maximumCount; }

    public int sampleCount(RandomSource random) {
        return minimumCount + random.nextInt(maximumCount - minimumCount + 1);
    }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        ResourceLocation source = context.get(ContextKeys.SOURCE);
        if (source == null) return false;
        if (!player.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).isPresent()) return false;
        ResourceLocation typeId = type.get();
        if (!FamiliarHelper.isRegisteredFamiliarType(typeId)) return false;
        int repetitions = 1;
        if (repeatWithAmplifier) {
            double amplifier = context.getOrDefault(ContextKeys.AMPLIFIER, 1.0);
            repetitions = Double.isFinite(amplifier) ? (int) Math.max(0, Math.min(64, amplifier)) : 0;
        }
        var position = context.getOrDefault(ContextKeys.TARGET_POSITION, player.position());
        for (int i = 0; i < repetitions; i++) {
            FamiliarHelper.spawnFamiliars(player, typeId, source, position, sampleCount(player.getRandom()));
        }
        return true;
    }
}
