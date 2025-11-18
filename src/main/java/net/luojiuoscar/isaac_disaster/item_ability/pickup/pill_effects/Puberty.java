package net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects;

import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPillEffect;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.PillEffectId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.SetId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.world.entity.player.Player;


public class Puberty implements IPillEffect {
    @Override
    public int getPillEffectId() {
        return PillEffectId.PUBERTY.getId();
    }

    @Override
    public void onUseEffect(Player player) {
        StatManager.modifySetWithId(player, SetId.ADULT.getId(), 1);
    }

    @Override
    public void onUseEffectH(Player player) {
        StatManager.modifySetWithId(player, SetId.ADULT.getId(), 2);
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.PUBERTY.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_BAD.get(), 1.0f, 1.0f);
    }

    @Override
    public void onUseSoundH(Player player) {
        player.playSound(ModSounds.PUBERTY_H.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_BAD_H.get(), 1.0f, 1.0f);

    }

    @Override
    public String getDescriptionId(){
        return "pill.isaac_disaster.effect.puberty";
    }

}
