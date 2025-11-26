package net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPillEffect;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.PillEffectId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PillOnUseS2CPacket;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;


public class BombsAreKey implements IPillEffect {
    @Override
    public int getPillEffectId() {
        return PillEffectId.BOMBS_ARE_KEY.getId();
    }

    @Override
    public void onUse(ServerPlayer player){
        if (PlayerHelper.getPillQuality(player) < 0){
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.TEARS_DOWN.getId()).onUse(player);
            return;
        }

        onUseEffect(player);
        if (!player.level().isClientSide){
            ModMessages.sentToPlayer(new PillOnUseS2CPacket(getPillEffectId(), false), (ServerPlayer) player);
        }
    }

    @Override
    public void onUseH(ServerPlayer player){
        if (PlayerHelper.getPillQuality(player) < 0){
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.TEARS_DOWN.getId()).onUseH(player);
            return;
        }

        onUseEffectH(player);
        if (!player.level().isClientSide){
            ModMessages.sentToPlayer(new PillOnUseS2CPacket(getPillEffectId(), true), (ServerPlayer) player);
        }
    }

    @Override
    public void onUseEffect(ServerPlayer player) {
        Inventory inv = player.getInventory();
        List<ItemStack> items = new ArrayList<>();
        items.addAll(inv.items);
        items.addAll(inv.offhand);

        int bomb = 0;
        int key = 0;

        for (ItemStack stack : items){
            Item item = stack.getItem();

            if (item == ModItems.BOMB.get()){
                bomb += stack.getCount();
                stack.setCount(0);

            }else if (item == ModItems.KEY.get()){
                key += stack.getCount();
                stack.setCount(0);

            }else if (item == ModItems.GOLDEN_BOMB.get()){
                stack.setCount(0);
                PlayerHelper.giveItem(player, ModItems.GOLDEN_KEY.get(), 1);

            }else if (item == ModItems.GOLDEN_KEY.get()){
                stack.setCount(0);
                PlayerHelper.giveItem(player, ModItems.GOLDEN_BOMB.get(), 1);
            }
        }

        PlayerHelper.giveItem(player, ModItems.BOMB.get(), key);
        PlayerHelper.giveItem(player, ModItems.KEY.get(), bomb);
    }

    @Override
    public void onUseEffectH(ServerPlayer player) {
        onUseEffect(player);
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.BOMBS_ARE_KEY.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_GOOD.get(), 1.0f, 1.0f);
    }

    @Override
    public void onUseSoundH(Player player) {
        player.playSound(ModSounds.BOMBS_ARE_KEY_H.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_GOOD_H.get(), 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(){
        if (ClientDataManager.getInstance().getPillQuality() < 0){
            return "pill.isaac_disaster.effect.tears_down";
        }
        return "pill.isaac_disaster.effect.bombs_are_key";
    }

}
