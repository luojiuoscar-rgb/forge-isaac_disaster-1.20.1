package net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects;

import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPillEffect;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id.PillEffectId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.ActiveItemManager;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.world.entity.player.Player;


public class PowerPill implements IPillEffect {
    @Override
    public int getPillEffectId() {
        return PillEffectId.POWER_PILL.getId();
    }


    @Override
    public void onUseEffect(Player player) {
        ActiveItemManager.getInstance().getItemFromId(ItemId.THE_GAMEKID.getId()).onTriggeredEffect(player);
    }

    @Override
    public void onUseEffectH(Player player) {
        ActiveItemManager.getInstance().getItemFromId(ItemId.THE_GAMEKID.getId()).onTriggeredEffectStronger(player);
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.POWER_PILL.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_GOOD.get(), 1.0f, 1.0f);
    }

    @Override
    public void onUseSoundH(Player player) {
        player.playSound(ModSounds.POWER_PILL_H.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_GOOD_H.get(), 1.0f, 1.0f);

    }

    @Override
    public String getDescriptionId(){
        return "pill.isaac_disaster.effect.power_pill";
    }

}
