package net.luojiuoscar.isaac_disaster.registries.ability.active.impl;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ActiveAbility;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.TagManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Diplopia extends ActiveAbility {

    public Diplopia(int id, int level) {
        super(id, level);
    }

    @Override
    public void onFirstUse(ServerPlayer player, @Nullable ItemStack stack, @javax.annotation.Nullable InteractionHand hand) {
    }

    @Override
    public void onTrigger(ServerPlayer player, ItemStack stack, @javax.annotation.Nullable InteractionHand hand) {
        // 遍历背包并生成掉落
        Inventory inv = player.getInventory();
        List<ItemStack> invItems = new ArrayList<>();
        invItems.addAll(inv.items);
        invItems.addAll(inv.offhand);


        for (ItemStack s : invItems) {
            if (!s.isEmpty()) {
                // 跳过不可被复制的物品
                if (s.is(TagManager.ITEM_CANNOT_BE_DUPLICATED)) continue;

                ItemStack copy = s.copy();
                PlayerHelper.giveItem(player, copy);
            }
        }
    }

    @Override
    public void onTriggerStronger(ServerPlayer player, ItemStack stack, @javax.annotation.Nullable InteractionHand hand){
        onTrigger(player, stack, hand);
    }

    @Override
    public void triggerSFX(ServerPlayer player) {

    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("item.isaac_disaster.diplopia.lore.1")
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack) {
        List<Component> description = new ArrayList<>();

        if (ClientDataManager.getInstance().getCountFromId(ItemId.CAR_BATTERY.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.car_battery").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.no_effect"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }
        return description;
    }
}
