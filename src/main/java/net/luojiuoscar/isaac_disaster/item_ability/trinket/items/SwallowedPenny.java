package net.luojiuoscar.isaac_disaster.item_ability.trinket.items;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.trinket.IHurtTriggerTrinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.LootTableNameManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.TrinketId;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SwallowedPenny implements IHurtTriggerTrinket {
    @Override
    public int getId() {
        return TrinketId.SWALLOWED_PENNY.getId();
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.SWALLOWED_PENNY.get());
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
    public void handleHurtEffect(Player player, Entity target, boolean isEnchanted) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        LevelHelper.spawnLootAtPos(serverLevel, player.blockPosition().getCenter(), ResourceLocation.fromNamespaceAndPath(
                IsaacDisaster.MOD_ID,LootTableNameManager.RANDOM_COINS));
    }

    @Override
    public boolean isPunishType() {
        return false;
    }

    @Override
    public double getTriggerChance(Player player, boolean isEnchanted) {
        return isEnchanted ? 1 : 0.5;
    }
}
