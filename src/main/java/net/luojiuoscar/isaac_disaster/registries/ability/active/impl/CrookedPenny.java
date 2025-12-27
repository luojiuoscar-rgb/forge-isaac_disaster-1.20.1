package net.luojiuoscar.isaac_disaster.registries.ability.active.impl;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModActiveItems;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ActiveAbility;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ModActiveAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CrookedPenny extends ActiveAbility {
    public CrookedPenny(int id, int level) {
        super(id, level);
    }

    @Override
    public void onFirstUse(ServerPlayer player, @Nullable ItemStack stack, @javax.annotation.Nullable InteractionHand hand) {}

    @Override
    public void onTrigger(ServerPlayer player, ItemStack stack, @javax.annotation.Nullable InteractionHand hand) {
        RandomSource random = player.getRandom();

        if (random.nextDouble() < 0.5){
            ModActiveAbility.ANARCHIST_COOKBOOK.get().onTrigger(player, stack, hand);
        }else{
            // 清空背包并给予1块钱，返还弯币
            Inventory inv = player.getInventory();
            inv.items.clear();
            inv.offhand.clear();
            inv.armor.clear();
            PlayerHelper.giveItem(player, ModActiveItems.CROOKED_PENNY.get(), 1);
            PlayerHelper.giveMoney(player, 1);
        }
    }

    @Override
    public void onTriggerStronger(ServerPlayer player, ItemStack stack, @javax.annotation.Nullable InteractionHand hand){
        onTrigger(player, stack, hand);
        onTrigger(player, stack, hand);
    }

    @Override
    public void triggerSFX(ServerPlayer player) {}

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("item.isaac_disaster.crooked_penny.lore.1"),
                Component.translatable("item.isaac_disaster.crooked_penny.lore.2"),
                Component.translatable("item.isaac_disaster.special.cannot_duplicate")
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack) {
        List<Component> description = new ArrayList<>();

        if (ClientDataManager.getInstance().getCountFromId(ItemId.CAR_BATTERY.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.car_battery").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.double"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }
        return description;
    }
}
