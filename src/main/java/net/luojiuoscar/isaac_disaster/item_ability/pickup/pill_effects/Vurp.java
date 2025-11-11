package net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerItemUseRecordProvider;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.registries.PillRegistry;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPillEffect;
import net.luojiuoscar.isaac_disaster.manager.id_managers.PillEffectId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public class Vurp implements IPillEffect {
    @Override
    public int getPillEffectId() {
        return PillEffectId.VURP.getId();
    }


    @Override
    public void onUseEffect(Player player) {
        player.getCapability(PlayerItemUseRecordProvider.PLAYER_ITEM_USE_RECORD).ifPresent(
                playerItemUseRecord -> {
                    var pills = playerItemUseRecord.getPillRecords();
                    if (pills.isEmpty()){
                        PlayerHelper.giveItem(player, new ItemStack(PillRegistry.getGoldenPill(false).get()));
                    }else{
                        int id = pills.get(0).id();
                        PlayerHelper.giveItem(player, new ItemStack(PillRegistry.getPillById(id, false).get()));
                    }
                }
        );
    }

    @Override
    public void onUseEffectH(Player player) {
        player.getCapability(PlayerItemUseRecordProvider.PLAYER_ITEM_USE_RECORD).ifPresent(
                playerItemUseRecord -> {
                    var pills = playerItemUseRecord.getPillRecords();
                    if (pills.isEmpty()){
                        PlayerHelper.giveItem(player, new ItemStack(PillRegistry.getGoldenPill(true).get()));
                    }else{
                        int id = pills.get(0).id();
                        PlayerHelper.giveItem(player, new ItemStack(PillRegistry.getPillById(id, true).get()));
                    }
                }
        );
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.VURP.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_GOOD.get(), 1.0f, 1.0f);
    }

    @Override
    public void onUseSoundH(Player player) {
        player.playSound(ModSounds.VURP_H.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_GOOD_H.get(), 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(){
        return "pill.isaac_disaster.effect.vurp";
    }

}
