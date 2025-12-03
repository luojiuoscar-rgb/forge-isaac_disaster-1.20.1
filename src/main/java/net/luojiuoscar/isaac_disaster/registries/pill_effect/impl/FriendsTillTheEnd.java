package net.luojiuoscar.isaac_disaster.registries.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.IPillEffect;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.ModPillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.Level;

import java.util.Objects;


public class FriendsTillTheEnd implements IPillEffect {
    @Override
    public IPillEffect redirect(ServerPlayer player) {
        return PlayerHelper.getPillQuality(player) < 0 ? ModPillEffect.HEALTH_DOWN.get() : this;
    }

    @Override
    public void onUseEffect(ServerPlayer player) {
        Level level = player.level();
        Wolf wolf = EntityType.WOLF.create(level);
        if (wolf != null) {
            wolf.tame(player); // 主人
            wolf.setOrderedToSit(false);
            wolf.setPos(player.blockPosition().getCenter());
            Objects.requireNonNull(wolf.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(StatManager.DAMAGE.getBonus() * 5);
            Objects.requireNonNull(wolf.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(StatManager.MAX_HEALTH.getBonus() * 5);
            wolf.setHealth(wolf.getMaxHealth());

            level.addFreshEntity(wolf);
        }
    }

    @Override
    public void onUseEffectH(ServerPlayer player) {
        Level level = player.level();
        Wolf wolf = EntityType.WOLF.create(level);
        if (wolf != null) {
            wolf.tame(player); // 主人
            wolf.setOrderedToSit(false);
            wolf.setPos(player.blockPosition().getCenter());
            wolf.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(StatManager.DAMAGE.getBonus() * 12);
            wolf.getAttribute(Attributes.MAX_HEALTH).setBaseValue(StatManager.MAX_HEALTH.getBonus() * 12);
            wolf.setHealth(wolf.getMaxHealth());

            level.addFreshEntity(wolf);
        }
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.FRIENDS_TILL_THE_END.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_GOOD.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void makeSoundH(ServerPlayer player) {
        player.playNotifySound(ModSounds.FRIENDS_TILL_THE_END_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_GOOD_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(int pillQuality){
        return pillQuality < 0
                ? "pill.isaac_disaster.effect.health_down"
                : "pill.isaac_disaster.effect.friends_till_the_end";
    }

}
