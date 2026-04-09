package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.PillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;


public class BombsAreKey extends PillEffect {

    @Override
    public PillEffect redirect(ServerPlayer player) {
        return PlayerHelper.getPillQuality(player) < 0 ? (PillEffect) ModExecutableEffects.TEARS_DOWN.get() : this;
    }

    @Override
    protected boolean pillActive(ServerPlayer player, boolean isHorse, ExecutableEffectContext context) {
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

        return true;
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.BOMBS_ARE_KEY.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_GOOD.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void makeSoundH(ServerPlayer player) {
        player.playNotifySound(ModSounds.BOMBS_ARE_KEY_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_GOOD_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(int pillQuality){
        return pillQuality < 0
                ? "pill.isaac_disaster.effect.tears_down"
                : "pill.isaac_disaster.effect.bombs_are_key";
    }

}
