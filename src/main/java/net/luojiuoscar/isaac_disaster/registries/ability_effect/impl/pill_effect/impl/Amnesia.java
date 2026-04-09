package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.PillEffectManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.PillEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.interfaces.GiveBlackHeartIfLowQuality;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;


public class Amnesia extends PillEffect implements GiveBlackHeartIfLowQuality {

    @Override
    public PillEffect redirect(ServerPlayer player) {
        return PlayerHelper.getPillQuality(player) > 0 ? (PillEffect) ModExecutableEffects.I_CAN_SEE_FOREVER.get() : this;
    }

    @Override
    protected boolean pillActive(ServerPlayer player, boolean isHorse, ExecutableEffectContext context) {
        Level level = player.level();
        if (level instanceof ServerLevel serverLevel){
            PillEffectManager.getInstance().shufflePills(serverLevel);
        }

        return true;
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.AMNESIA.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_BAD.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void makeSoundH(ServerPlayer player) {
        player.playNotifySound(ModSounds.AMNESIA_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_BAD_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(int pillQuality){
        return pillQuality > 0
                ? "pill.isaac_disaster.effect.i_can_see_forever"
                : "pill.isaac_disaster.effect.amnesia";
    }

}
