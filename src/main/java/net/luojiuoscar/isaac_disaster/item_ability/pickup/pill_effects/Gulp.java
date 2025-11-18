package net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPillEffect;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.PillEffectId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.world.entity.player.Player;


public class Gulp implements IPillEffect {
    @Override
    public int getPillEffectId() {
        return PillEffectId.GULP.getId();
    }

    @Override
    public void onUseEffect(Player player) {
        PlayerHelper.swallowAllTrinkets(player);
    }

    @Override
    public void onUseEffectH(Player player) {
        int count = PlayerHelper.swallowAllTrinkets(player);
        StatManager.healHealth(player, count * 0.5f);
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.GULP.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_GOOD.get(), 1.0f, 1.0f);
    }

    @Override
    public void onUseSoundH(Player player) {
        player.playSound(ModSounds.GULP_H.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_GOOD_H.get(), 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(){
        return "pill.isaac_disaster.effect.gulp";
    }

}
