package net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects;

import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPillEffect;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.PillEffectId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class IFoundPills implements IPillEffect {
    @Override
    public int getPillEffectId() {
        return PillEffectId.I_FOUND_PILLS.getId();
    }

    @Override
    public void onUseEffect(ServerPlayer player) {
        // no effect
    }

    @Override
    public void onUseEffectH(ServerPlayer player) {
        // no effect
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.I_FOUND_PILLS.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.I_FOUND_PILLS_USE.get(), 1.0f, 1.0f);
    }

    @Override
    public void onUseSoundH(Player player) {
        player.playSound(ModSounds.I_FOUND_PILLS_H.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.I_FOUND_PILLS_USE.get(), 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(){
        return "pill.isaac_disaster.effect.i_found_pills";
    }
}
