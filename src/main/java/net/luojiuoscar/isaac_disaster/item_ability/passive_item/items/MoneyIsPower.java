package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IRecursivePassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.UUIDManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MoneyIsPower implements IRecursivePassiveItem {
    @Override
    public int getTickInterval() {
        return 20; // 每秒触发
    }

    @Override
    public void recursiveEffect(Player player) {
        // 计算背包金币并更新
        int money = PlayerHelper.countMoney(player);
        double damage = money * Config.MONEY_IS_POWER_STRENGTH.get();
        AttributeInstance instance = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (instance == null) return;
        StatManager.setAdder(player, instance, damage, UUIDManager.MONEY_IS_POWER_ADDER, "base_damage_adder");
    }

    @Override
    public int getItemId() {
        return ItemId.MONEY_IS_POWER.getId();
    }

    @Override
    public void onObtain(Player player) {
    }

    @Override
    public void onDirectObtain(Player player) {
        recursiveEffect(player);
    }

    @Override
    public void onRemove(Player player) {
        AttributeInstance instance = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (instance == null) return;
        instance.removeModifier(UUIDManager.MONEY_IS_POWER_ADDER);
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.MONEY_IS_POWER.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.money_is_power.lore.1")
        );
    }
}
