package net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPillEffect;
import net.luojiuoscar.isaac_disaster.manager.id_managers.PillEffectId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ExperimentalPill implements IPillEffect {
    private final int[] effects_good = new int[]{
            PillEffectId.HEALTH_UP.getId(),
            PillEffectId.LUCK_UP.getId(),
            PillEffectId.RANGE_UP.getId(),
            PillEffectId.SHOT_SPEED_UP.getId(),
            PillEffectId.TEARS_UP.getId(),
            PillEffectId.SPEED_UP.getId(),
    };
    private final int[] effects_bad = new int[]{
            PillEffectId.HEALTH_DOWN.getId(),
            PillEffectId.LUCK_DOWN.getId(),
            PillEffectId.RANGE_DOWN.getId(),
            PillEffectId.SHOT_SPEED_DOWN.getId(),
            PillEffectId.TEARS_DOWN.getId(),
            PillEffectId.SPEED_DOWN.getId(),
    };


    @Override
    public int getPillEffectId() {
        return PillEffectId.EXPERIMENTAL_PILL.getId();
    }


    @Override
    public void onUseEffect(Player player) {
        Random random = new Random();
        PillEffectManager.getInstance().getEffectFromEffectId(effects_good[random.nextInt(effects_bad.length)]).onUseEffect(player);
        PillEffectManager.getInstance().getEffectFromEffectId(effects_bad[random.nextInt(effects_bad.length)]).onUseEffect(player);
    }

    @Override
    public void onUseEffectH(Player player) {
        Random random = new Random();
        PillEffectManager.getInstance().getEffectFromEffectId(effects_good[random.nextInt(effects_bad.length)]).onUseEffectH(player);
        PillEffectManager.getInstance().getEffectFromEffectId(effects_bad[random.nextInt(effects_bad.length)]).onUseEffectH(player);
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.EXPERIMENTAL_PILL.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_GOOD.get(), 1.0f, 1.0f);
    }

    @Override
    public void onUseSoundH(Player player) {
        player.playSound(ModSounds.EXPERIMENTAL_PILL_H.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_GOOD_H.get(), 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(){
        return "pill.isaac_disaster.effect.experimental_pill";
    }

}
