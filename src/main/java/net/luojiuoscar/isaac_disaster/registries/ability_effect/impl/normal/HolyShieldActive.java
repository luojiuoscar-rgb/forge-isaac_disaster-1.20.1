package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.effect.IStackableEffect;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class HolyShieldActive implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.EVENT) instanceof LivingHurtEvent event)) return false;

        double damage = event.getAmount();
        if (!(event.getEntity() instanceof Player player)) return true;
        var instance = player.getEffect(ModEffects.HOLY_SHIELD.get());
        if (instance == null) return true;

        int amplifier = instance.getAmplifier();
        amplifier = Math.min(10, amplifier);

        if (damage > (amplifier + 1) * StatManager.getHolyShieldStrength()){
            // 只有伤害足够高的时候才移除护盾
            ((IStackableEffect) ModEffects.HOLY_SHIELD.get()).stack(player, -1);

            LevelHelper.spawnParticle((ServerLevel) player.level(), new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GLASS.defaultBlockState()),
                    player.position(), 0.5, 0.5, 0.5, 0.05, 20, false, null);

            // sounds
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.HOLY_SHIELD_BROKE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        }

        event.setAmount(0.0f);
        event.setCanceled(true);

        return true;
    }
}
