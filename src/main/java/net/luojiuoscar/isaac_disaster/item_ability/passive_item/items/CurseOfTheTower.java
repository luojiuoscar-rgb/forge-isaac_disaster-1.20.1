package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IHurtTriggerPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CurseOfTheTower implements IHurtTriggerPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.CURSE_OF_THE_TOWER.getId();
    }

    @Override
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.curse_of_the_tower.lore.1")
        );
    }

    @Override
    public void handleHurtEffect(Player player, Entity target) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        // 如果为true（禁用状态）则取消释放；同时启用受伤效果（防止退出游戏丢失schedule后永久禁用）
        if (PlayerHelper.getItemFlag(serverPlayer, ItemId.CURSE_OF_THE_TOWER.getId())) {
            PlayerHelper.setItemFlag(serverPlayer, ItemId.CURSE_OF_THE_TOWER.getId(), false);
            return;
        }
        // 生成随机炸弹
        PlayerHelper.spawnRandomBombsNearby(serverPlayer, StatManager.getNearbyRange() * 0.5, 6);

        PlayerHelper.setItemFlag(serverPlayer, ItemId.CURSE_OF_THE_TOWER.getId(), true);
        // 计划重启
        ScheduledFuncHelper.schedule("curse_of_the_tower_cool_down", 120, 0, true,
                () -> PlayerHelper.setItemFlag(serverPlayer, ItemId.CURSE_OF_THE_TOWER.getId(), false));
    }

    @Override
    public boolean isPunishType() {
        return true;
    }

    @Override
    public double getTriggerChance(Player player) {
        return 1;
    }
}
