package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IRecursivePassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TheWiz implements IPassiveItem, IRecursivePassiveItem {
    @Override
    public int getItemId() {
        return ItemId.THE_WIZ.getId();
    }

    @Override
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {
        StatManager.addSpectral(player, 1);
        recursiveEffect(player);
    }

    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {
        StatManager.addSpectral(player, -1);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.the_wiz.lore.1")
        );
    }

    @Override
    public int getTickInterval() {
        return 100;
    }

    @Override
    public void recursiveEffect(Player player) {
        player.addEffect(new MobEffectInstance(
                ModEffects.THE_WIZ.get(),
                150,
                1,
                true,
                false,
                false
        ));
    }
}
