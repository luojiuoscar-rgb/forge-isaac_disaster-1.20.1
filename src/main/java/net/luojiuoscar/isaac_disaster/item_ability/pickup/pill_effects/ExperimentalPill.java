package net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects;

import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPillEffect;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.PillEffectId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;


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
    public void onUseEffect(ServerPlayer player) {
        RandomSource random = player.getRandom();

        PillEffectManager.getInstance().getEffectFromEffectId(effects_good[random.nextInt(effects_bad.length)]).onUseEffect(player);
        PillEffectManager.getInstance().getEffectFromEffectId(effects_bad[random.nextInt(effects_bad.length)]).onUseEffect(player);
    }

    @Override
    public void onUseEffectH(ServerPlayer player) {
        RandomSource random = player.getRandom();

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
