package net.luojiuoscar.isaac_disaster.item_ability.trinket.items;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.item_ability.trinket.IHurtTriggerTrinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.luojiuoscar.isaac_disaster.manager.id.TrinketId;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public class SwallowedPenny implements IHurtTriggerTrinket {
    @Override
    public int getId() {
        return TrinketId.SWALLOWED_PENNY.getId();
    }

    @Override
    public List<Component> getDescription() {
        return List.of(Component.translatable("item.isaac_disaster.swallowed_penny.lore.1"));
    }

    @Override
    public List<Component> getEnchantedDescription() {
        return List.of(Component.translatable("item.isaac_disaster.swallowed_penny.enchanted.lore.1")
                .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
    }

    @Override
    public void handleHurtEffect(Player player, Entity attacker, List<ItemStack> stackList, LivingHurtEvent event) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        LootHelper.spawnLootAtPos(player, player.blockPosition().getCenter(), LootTableManager.RANDOM_COINS);
    }

    @Override
    public boolean isPunishType() {
        return false;
    }

    @Override
    public double getTriggerChance(Player player, List<ItemStack> stackList) {
        return stackList.stream().anyMatch(Trinket::isEnchanted) ? 0.7 : 0.35;
    }
}
