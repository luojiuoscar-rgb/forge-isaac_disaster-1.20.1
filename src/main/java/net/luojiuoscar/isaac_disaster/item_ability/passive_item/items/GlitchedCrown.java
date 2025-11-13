package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.event.custom.ItemDisplayAddEvent;
import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.helper.PoolHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.item.IsaacItem;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GlitchedCrown implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.GLITCHED_CROWN.getId();
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
                Component.translatable("item.isaac_disaster.glitched_crown.lore.1")
        );
    }

    public static void addDisplayItem(ItemDisplayAddEvent event){
        ServerLevel level = event.getLevel();
        ResourceLocation tableId = event.getLootTable();
        Player player = event.getPlayer();
        BlockPos pos = event.getPos();

        for (int i = 0; i < 4; i++){
            List<ItemStack> items = LootHelper.generateLoot(level, tableId, new LootParams.Builder(level)
                            .withParameter(LootContextParams.ORIGIN, pos.getCenter())
                            .withOptionalParameter(LootContextParams.THIS_ENTITY, player),
                    LootContextParamSets.EMPTY);

            ItemStack stack = new ItemStack(ModItems.BREAKFAST.get()); // default
            if (!items.isEmpty()){
                stack = items.get(0);
            }

            if (stack.getItem() instanceof IsaacItem isaacItem){
                event.addDisplayItem(stack);
                PoolHelper.markAsRemoval(player, tableId, isaacItem.getItemId()); // 移出道具池
            }
        }
    }

}
