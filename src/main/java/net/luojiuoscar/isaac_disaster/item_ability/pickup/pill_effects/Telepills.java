package net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPillEffect;
import net.luojiuoscar.isaac_disaster.manager.id_managers.PillEffectId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;


public class Telepills implements IPillEffect {
    @Override
    public int getPillEffectId() {
        return PillEffectId.TELEPILLS.getId();
    }


    @Override
    public void onUseEffect(Player player) {
        PlayerHelper.teleportToRandomLocation(player, 64);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void onUseEffectH(Player player) {
        PlayerHelper.teleportToRandomLocation(player, 256);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.TELEPILLS.get(), 1.0f, 1.0f);
    }

    @Override
    public void onUseSoundH(Player player) {
        player.playSound(ModSounds.TELEPILLS_H.get(), 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(){
        return "pill.isaac_disaster.effect.telepills";
    }

}
