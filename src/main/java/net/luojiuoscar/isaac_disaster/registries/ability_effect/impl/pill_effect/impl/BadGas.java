package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.effect.custom.DeadlyPoisonEffect;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.PillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;


public class BadGas extends PillEffect {

    @Override
    public PillEffect redirect(ServerPlayer player) {
        return PlayerHelper.getPillQuality(player) < 0 ? (PillEffect) ModExecutableEffects.HEALTH_DOWN.get() : this;
    }

    @Override
    protected boolean pillActive(ServerPlayer player, boolean isHorse, ExecutableEffectContext context) {
        Level level = player.level();
        Vec3 pos = player.blockPosition().getCenter();

        int amplifier = DeadlyPoisonEffect.amplifierForDamage(
                player.getAttributeValue(Attributes.ATTACK_DAMAGE) * (isHorse ? 1 : 0.3));
        var potion = new PotionProfile(ModEffects.DEADLY_POISON.get(), isHorse ? 600 : 300, amplifier);

        List<LivingEntity> entities = LevelHelper.selectBySphere(level, pos, StatManager.getNearbyRange());
        for (LivingEntity entity : entities){
            if (entity == player) continue; // 排除自己

            if (isHorse){
                LevelHelper.pushEntity(level, entity, pos, 5.5, 0.4);
            }else {
                LevelHelper.pushEntity(level, entity, pos, 2, 0.2);
            }

            var targetContext = context.copy(entity);
            targetContext.set(ContextKeys.AMPLIFIER, 1.0);
            targetContext.set(ContextKeys.POTIONS, List.of(potion));
            ModExecutableEffects.POTIONS.get().apply(targetContext);
        }

        return true;
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.BAD_GAS.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.FART_NORMAL.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void makeSoundH(ServerPlayer player) {
        player.playNotifySound(ModSounds.BAD_GAS_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.FART_HUGE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(int pillQuality){
        return pillQuality < 0
                ? "pill.isaac_disaster.effect.health_down"
                : "pill.isaac_disaster.effect.bad_gas";
    }

}
