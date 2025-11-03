package net.luojiuoscar.isaac_disaster.item_ability.active_item.items;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.active_item.IActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.LootTableNameManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WoodenNickel implements IActiveItem {

    @Override
    public int getItemId() {
        return ItemId.WOODEN_NICKEL.getId();
    }

    @Override
    public SoundEvent getSound() {
        return null;
    }

    @Override
    public void onTriggeredEffect(Player player) {
        // 60%概率触发
        if (Math.random() < 0.6 && !player.level().isClientSide){
            EntityHelper.spawnLootAtPos(player, player.blockPosition().getCenter(),
                    ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, LootTableNameManager.RANDOM_COINS));
        }
    }

    @Override
    public void onTriggeredEffectStronger(Player player){
        onTriggeredEffect(player);
        onTriggeredEffect(player);
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.WOODEN_NICKEL.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.wooden_nickel.lore.1")
        );
    }

    @Override
    public List<Component> getSynergyDescription() {
        List<Component> description = new ArrayList<>();

        if (ClientDataManager.getInstance().getCountFromId(ItemId.CAR_BATTERY.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.car_battery").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.double"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }
        return description;
    }
}
