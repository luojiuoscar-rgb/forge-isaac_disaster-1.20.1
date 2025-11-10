package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IRecursivePassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class MoneyIsPower implements IRecursivePassiveItem {
    private static final UUID MONEY_IS_POWER_ADDER =
            UUID.nameUUIDFromBytes(("isaac_disaster:money_is_power_adder").getBytes(StandardCharsets.UTF_8));

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
        StatManager.setModifierAdd(player, instance, damage, MONEY_IS_POWER_ADDER, "");
    }

    @Override
    public int getItemId() {
        return ItemId.MONEY_IS_POWER.getId();
    }

    @Override
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {
        recursiveEffect(player);
    }

    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {
        AttributeInstance instance = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (instance == null) return;
        instance.removeModifier(MONEY_IS_POWER_ADDER);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.money_is_power.lore.1")
        );
    }
}
