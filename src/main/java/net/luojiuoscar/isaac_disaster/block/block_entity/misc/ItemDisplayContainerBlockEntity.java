package net.luojiuoscar.isaac_disaster.block.block_entity.misc;

import net.luojiuoscar.isaac_disaster.capability.misc.DisplayItemListCap;
import net.luojiuoscar.isaac_disaster.event.custom.ItemDisplayAddEvent;
import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.helper.PoolHelper;
import net.luojiuoscar.isaac_disaster.item.item.IsaacItem;
import net.luojiuoscar.isaac_disaster.manager.data.IsaacItemBlockData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;

public interface ItemDisplayContainerBlockEntity {
    void itemRollFromPlayer(Player player);

    DisplayItemListCap getItemDisplayCap();



    boolean tryLootItem(ServerLevel serverLevel, Player player, BlockPos pos);

    /**
     * 从道具池中将道具填充至itemDisplayList
     */
    default boolean lootItem(ServerLevel serverLevel, Player player, BlockPos pos, ResourceLocation tableId){
        List<ItemStack> items = LootHelper.generateLoot(serverLevel, tableId, new LootParams.Builder(serverLevel)
                        .withParameter(LootContextParams.ORIGIN, pos.getCenter())
                        .withOptionalParameter(LootContextParams.THIS_ENTITY, player),
                LootContextParamSets.CHEST);

        if (items.isEmpty()) return false;
        ItemStack stack = items.get(0);

        if (!stack.isEmpty() && stack.getItem() instanceof IsaacItem isaacItem){
            stack.setCount(1);
            addItemDisplay(stack); // 加入到展示型道具列表
            setItemDisplay(stack); // 首次加入

            // 从事件中添加
            ItemDisplayAddEvent event = new ItemDisplayAddEvent(serverLevel, player, pos, tableId);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
            for (ItemStack extra : event.getDisplayItems()) {
                if (!extra.isEmpty()) addItemDisplay(extra);
            }

            PoolHelper.markAsRemoval(player, tableId, isaacItem.getItemId()); // 移出道具池
            IsaacItemBlockData.get(serverLevel).addItemBlock(pos); // 记录当前坐标
            return true;
        }
        return false;
    }

    default List<ItemStack> getItemDisplayList() {
        return getItemDisplayCap().getItemList();
    }

    default void setItemDisplayList(List<ItemStack> list) {
        getItemDisplayCap().setItemList(list);
    }

    default ItemStack getCurrentItemDisplay() {
        return getItemDisplayCap().getDisplayedItem();
    }

    default int getDisplayTickInterval() {
        int size = getItemDisplayList().size();
        if (size == 0) return 30;

        double C = 0.5;    // 最小值趋近
        double k = 0.345;  // 衰减速度
        double A = 30 - C; // 初始振幅

        double value = A * Math.exp(-k * (size - 1)) + C;
        return (int) Math.round(value);
    }

    default void rotateItemDisplay() {
        getItemDisplayCap().rotateDisplayedItem();
    }

    // 新增方法：添加单个物品到列表
    default void addItemDisplay(ItemStack stack) {
        getItemDisplayCap().addItem(stack);
        setItemDisplay(stack);
    }

    void setItemDisplay(ItemStack stack);


    // 新增方法：清空列表
    default void clearItemDisplayList() {
        getItemDisplayCap().clear();
    }

    /**
     * 接口提供的轮播tick方法
     * @param level 当前世界
     * @param tickCount 世界tick计数
     * @param updateDisplayedItem 回调函数，用于将 index=0 的物品同步到 BlockEntity 的 inventory
     */
    default void tickRotate(Level level, long tickCount, Runnable updateDisplayedItem) {
        if (!level.isClientSide && !getItemDisplayList().isEmpty() && tickCount % getDisplayTickInterval() == 0) {
            rotateItemDisplay();
            if (updateDisplayedItem != null) updateDisplayedItem.run();
        }
    }

    default void saveItemDisplayCap(CompoundTag tag) {
        if (getItemDisplayCap() != null) {
            CompoundTag capTag = new CompoundTag();
            getItemDisplayCap().save(capTag);
            tag.put("DisplayItemListCap", capTag);
        }
    }

    default void loadItemDisplayCap(CompoundTag tag) {
        if (getItemDisplayCap() != null && tag.contains("DisplayItemListCap", 10)) { // 10=CompoundTag
            getItemDisplayCap().load(tag.getCompound("DisplayItemListCap"));
        }
    }

}
