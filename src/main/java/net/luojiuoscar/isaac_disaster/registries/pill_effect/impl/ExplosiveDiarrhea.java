package net.luojiuoscar.isaac_disaster.registries.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.IPillEffect;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.ModPillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;


public class ExplosiveDiarrhea implements IPillEffect {

    private static final ResourceLocation SCHEDULE_TYPE =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "explosive_diarrhea");

    @Override
    public IPillEffect redirect(ServerPlayer player) {
        return PlayerHelper.getPillQuality(player) < 0 ? ModPillEffect.RANGE_DOWN.get() : this;
    }

    @Override
    public void onUseEffect(ServerPlayer player) {
        ScheduledFuncHelper.scheduleForPlayer(player.getUUID(), SCHEDULE_TYPE, 15,15, 5, false,
                () -> {
            EntityHelper.spawnBomb(player.position(), player, player.level(), Vec3.ZERO, 1);
            player.playNotifySound(ModSounds.FART_NORMAL.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        });
    }

    @Override
    public void onUseEffectH(ServerPlayer player) {
        ScheduledFuncHelper.scheduleForPlayer(player.getUUID(), SCHEDULE_TYPE, 15, 15, 8, false,
                () -> {
            EntityHelper.spawnBomb(player.position(), player, player.level(), Vec3.ZERO, 2);
            player.playNotifySound(ModSounds.FART_HUGE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        });
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.EXPLOSIVE_DIARRHEA.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_BAD.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void makeSoundH(ServerPlayer player) {
        player.playNotifySound(ModSounds.EXPLOSIVE_DIARRHEA_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_BAD_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

    }

    @Override
    public String getDescriptionId(int pillQuality){
        return pillQuality < 0
                ? "pill.isaac_disaster.effect.range_down"
                : "pill.isaac_disaster.effect.explosive_diarrhea";
    }
}
