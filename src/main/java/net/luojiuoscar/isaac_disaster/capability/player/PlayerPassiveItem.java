package net.luojiuoscar.isaac_disaster.capability.player;

import net.luojiuoscar.isaac_disaster.manager.item_managers.PassiveItemManager;
import net.luojiuoscar.isaac_disaster.isaac.passive_item.IInteractiveIPassiveItem;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.DirectObtainPassiveItemS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.ObtainPassiveItemS2CPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static net.luojiuoscar.isaac_disaster.Config.PASSIVE_ITEM_LIMIT;

@AutoRegisterCapability
public class PlayerPassiveItem {
    // 用编号代表道具。需要查询道具的时候再使用道具管理器
    private ArrayList<Integer> playerPassiveItems;
    // 键为道具ID，值为道具数量
    private Map<Integer, Integer> itemCountMap;
    private Map<Integer, Integer> interactiveItemCountMap;

    // constructor
    public PlayerPassiveItem(){
        this.playerPassiveItems = new ArrayList<>();

        this.itemCountMap = new HashMap<>();
        this.interactiveItemCountMap = new HashMap<>();
    }

    /**
     * 清空全部的哈希表
     */
    public void clearItemMap(){
        itemCountMap.clear();
        interactiveItemCountMap.clear();
    }

    /**
     * 移除道具后更新哈希表计数
     * 不用删除值为0的映射；在玩家死亡等时机由于表格刷新会自动删除
     */
    private void updateItemMap(int itemId, int amount) {
        itemCountMap.put(itemId, itemCountMap.getOrDefault(itemId, 0) + amount);
        // 如果是触发型道具
        if(PassiveItemManager.getInstance().getItemFromId(itemId) instanceof IInteractiveIPassiveItem){
            interactiveItemCountMap.put(itemId, itemCountMap.getOrDefault(itemId, 0) + amount);
        }
    }

    /**
     * 根据当前道具列表重新计算哈希表（用于数据同步）
     */
    private void refreshItemCountMap() {
        clearItemMap();
        for (int id : playerPassiveItems) {
            updateItemMap(id, 1); // 增加一个
        }
    }


    /**
     * @return 全部道具的列表
     */
    public ArrayList<Integer> getPlayerPassiveItems(){
        return playerPassiveItems;
    }

    /**
     * @return 全部触发型道具的列表
     */
    public Map<Integer, Integer> getPlayerInteractiveItemMap(){
        return interactiveItemCountMap;
    }

    /**
     * @return 获取道具的数量
     */
    public int getTotalItems(){
        return playerPassiveItems.size();
    }


    /**
     * 通过循环删除最先获取的道具来清空道具列表
     */
    public void clearPlayerPassiveItems(ServerPlayer player){
        // 循环清除最先获取的道具
        while(removeFromIndex(player, 0));
        // 清空哈希表
        clearItemMap();
    }

    /**
     * 获取某个道具的总数
     */
    public int getItemCount(int itemId) {
        return itemCountMap.getOrDefault(itemId, 0);
    }


    /**
     * 将一个新的道具添加到列表。同时触发添加道具的效果和直接添加效果
     */
    public void addItem(ServerPlayer player, int itemId, InteractionHand hand){
        // 如果道具数量已达上限
        if(this.getTotalItems() >= PASSIVE_ITEM_LIMIT.get()){
            player.sendSystemMessage(Component.translatable("message.isaac_disaster.warning.too_many_items").withStyle(
                    style -> style.withColor(0xFFBF00).withUnderlined(true)
            ));
            return;
        }

        // 增加被动道具到列表
        playerPassiveItems.add(itemId);
        // 更新哈希表：数量+1
        updateItemMap(itemId, 1);

        // 触发效果
        PassiveItemManager.getInstance().getItemFromId(itemId).onObtain(player);
        ModMessages.sentToPlayer(new ObtainPassiveItemS2CPacket(itemId), player);

        PassiveItemManager.getInstance().getItemFromId(itemId).onDirectObtain(player);
        ModMessages.sentToPlayer(new DirectObtainPassiveItemS2CPacket(itemId), player);


        // 删除手持的道具原型
        player.getItemInHand(hand).shrink(1);
    }

    /**
     * 将一个新的道具添加到列表。同时触发添加道具的直接添加效果
     */
    public void directAddItem(ServerPlayer player, int itemId){
        // 如果道具数量已达上限
        if(this.getTotalItems() >= PASSIVE_ITEM_LIMIT.get()){
            player.sendSystemMessage(Component.translatable("message.isaac_disaster.warning.too_many_items").withStyle(
                    style -> style.withColor(0xFFBF00).withUnderlined(true)
            ));
            return;
        }

        playerPassiveItems.add(itemId);
        // 更新哈希表：数量+1
        updateItemMap(itemId, 1);

        PassiveItemManager.getInstance().getItemFromId(itemId).onDirectObtain(player);
        ModMessages.sentToPlayer(new DirectObtainPassiveItemS2CPacket(itemId), player);
    }

    /**
     * 删除最先获取的一个道具
     */
    public boolean removeFromIndex(ServerPlayer player, int index) {
        // 确保索引有效
        if (index < 0 || index >= playerPassiveItems.size()) {
            return false; // 索引无效，直接返回失败
        }

        int removeId = playerPassiveItems.remove(index);
        // 更新哈希表：数量-1（若数量为0则移除键）
        updateItemMap(removeId, -1);

        //移除效果
        PassiveItemManager.getInstance().getItemFromId(removeId).onRemove(player);
        return true;
    }


    /**
     * 依据ID删除最先获取的一个道具
     */
    public void removeFromId(ServerPlayer player, int itemId) {
        // 使用迭代器遍历并移除第一个匹配的元素
        for (Iterator<Integer> iterator = playerPassiveItems.iterator(); iterator.hasNext(); ) {
            int id = iterator.next();
            if (id == itemId) {
                iterator.remove();
                // 更新哈希表：数量-1（若数量为0则移除键）
                updateItemMap(itemId, -1);

                PassiveItemManager.getInstance().getItemFromId(itemId).onRemove(player);
                break;
            }
        }
    }



    // 从目标处复制
    public void copyFrom(PlayerPassiveItem source) {
        this.playerPassiveItems = new ArrayList<>(source.playerPassiveItems);
        // 重新计算哈希表（确保与列表数据一致）
        refreshItemCountMap();
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putIntArray("passive_items", playerPassiveItems);
    }

    public void loadNBTData(CompoundTag nbt) {
        int[] ids = nbt.getIntArray("passive_items");
        // 将int[]转换为List<Integer>
        playerPassiveItems = Arrays.stream(ids)
                .boxed()
                .collect(Collectors.toCollection(ArrayList::new));
        // 加载后刷新哈希表
        refreshItemCountMap();
    }


    public static boolean hasItem(int ItemId, Player player){
        AtomicInteger count = new AtomicInteger();
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> count.set(playerPassiveItem.getItemCount(ItemId))
        );
        return count.get() > 0;
    }
}