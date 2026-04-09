package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.PillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.Level;


public class FriendsTillTheEnd extends PillEffect {
    @Override
    public PillEffect redirect(ServerPlayer player) {
        return PlayerHelper.getPillQuality(player) < 0 ? (PillEffect) ModExecutableEffects.HEALTH_DOWN.get() : this;
    }

    @Override
    protected boolean pillActive(ServerPlayer player, boolean isHorse, ExecutableEffectContext context) {
        Level level = player.level();
        int multiplier = isHorse ? 12 : 5;

        Wolf wolf = EntityType.WOLF.create(level);
        if (wolf != null) {
            wolf.tame(player); // 主人
            wolf.setOrderedToSit(false);
            wolf.setPos(player.blockPosition().getCenter());
            wolf.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(StatManager.DAMAGE.getBonus() * multiplier);
            wolf.getAttribute(Attributes.MAX_HEALTH).setBaseValue(StatManager.MAX_HEALTH.getBonus() * multiplier);
            wolf.setHealth(wolf.getMaxHealth());

            level.addFreshEntity(wolf);
        }

        return true;
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
