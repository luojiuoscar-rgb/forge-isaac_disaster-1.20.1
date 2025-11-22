package net.luojiuoscar.isaac_disaster.effect.custom;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class HolyShieldEffect extends MobEffect {
    public HolyShieldEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public java.util.List<ItemStack> getCurativeItems() {
        return java.util.Collections.emptyList();
    }

    public static void onTriggered(LivingHurtEvent event){
        double damage = event.getAmount();
        if (!(event.getEntity() instanceof Player player)) return;
        if (!player.hasEffect(ModEffects.HOLY_SHIELD.get())) return;

        int amplifier = player.getEffect(ModEffects.HOLY_SHIELD.get()).getAmplifier();
        amplifier = Math.min(10, amplifier);

        if (damage > (amplifier + 1) * StatManager.getHolyShieldStrength()){
            // 只有伤害足够高的时候才移除护盾
            HolyShieldEffect.stack(player, -1);

            LevelHelper.spawnParticle((ServerLevel) player.level(), new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GLASS.defaultBlockState()),
                    player.position(), 0.5, 0.5, 0.5, 0.05, 20, false, null);

            // sounds
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.HOLY_SHIELD_BROKE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        }

        event.setAmount(0.0f);
        event.setCanceled(true);
    }

    public static void stack(LivingEntity entity, int count){
        MobEffect effect = ModEffects.HOLY_SHIELD.get();

        int amplifier = entity.getEffect(effect) == null ? -1 : entity.getEffect(effect).getAmplifier();
        amplifier += count;
        amplifier = Math.min(10, amplifier); // max 10

        entity.removeEffect(effect);
        if (amplifier < 0) return;

        MobEffectInstance newEffect = new MobEffectInstance(
                effect,
                -1,
                amplifier,
                false,
                false,
                true
        );
        entity.addEffect(newEffect);
    }
}
