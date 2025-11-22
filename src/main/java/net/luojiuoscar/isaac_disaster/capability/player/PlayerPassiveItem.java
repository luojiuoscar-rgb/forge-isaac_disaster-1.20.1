package net.luojiuoscar.isaac_disaster.capability.player;

import net.luojiuoscar.isaac_disaster.helper.CuriosHelper;
import net.luojiuoscar.isaac_disaster.item.item.IsaacItem;
import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.item_ability.set.ISet;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PassiveItemManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.SetManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.SetCountSyncS2CPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.*;

import static net.luojiuoscar.isaac_disaster.Config.PASSIVE_ITEM_LIMIT;

@AutoRegisterCapability
public class PlayerPassiveItem {
    // 用编号代表道具。需要查询道具的时候再使用道具管理器
    private List<ItemStack> playerPassiveItems;
    // 键为道具ID，值为道具数量
    private final Map<Integer, Integer> itemCountMap;

    private Map<Integer, Integer> setCountMap; // 当前套装计数
    private Set<Integer> obtainedSets; // 已经获得过的套装


    // constructor
    public PlayerPassiveItem(){
        this.playerPassiveItems = new ArrayList<>();

        this.itemCountMap = new HashMap<>();
        this.setCountMap = new HashMap<>();
        this.obtainedSets = new HashSet<>();
        init();
    }

    public void init(){
        this.playerPassiveItems.clear();

        this.itemCountMap.clear();
        this.setCountMap.clear();
        this.obtainedSets.clear();
    }

    /**
     * 清空全部的哈希表
     */
    public void clearItemMap(){
        itemCountMap.clear();
        setCountMap.clear();
        obtainedSets.clear();
    }


    /**
     * 移除道具后更新哈希表计数
     * 不用删除值为0的映射；在玩家死亡等时机由于表格刷新会自动删除
     */
    private void updateItemMap(int itemId, int amount) {
        itemCountMap.put(itemId, itemCountMap.getOrDefault(itemId, 0) + amount);
    }

    /**
     * 根据当前道具列表重新计算哈希表（用于数据同步）
     */
    private void refreshItemCountMap() {
        clearItemMap();
        for (ItemStack stack : playerPassiveItems) {
            if (!(stack.getItem() instanceof PassiveItem item)) continue;
            updateItemMap(item.getItemId(), 1); // 增加一个
        }
    }



    public List<ItemStack> getPassiveItems(){
        return new ArrayList<>(playerPassiveItems);
    }

    public Map<Integer, Integer> getSetCountMap(){
        return new HashMap<>(setCountMap);
    }

    public boolean isObtainedSet(int setId){
        return this.obtainedSets.contains(setId);
    }
    public void setObtainedSet(int setId){
        this.obtainedSets.add(setId);
    }


    public int getTotalItemsCountInCap(){
        return playerPassiveItems.size();
    }

    /** 通过循环删除最先获取的道具来清空道具列表 */
    public void clearPlayerPassiveItems(ServerPlayer player){
        // 循环清除最先获取的道具 （用于触发效果）
        while(removeFromIndex(player, 0));
        // 清空哈希表
        clearItemMap();
    }

    /**
     * 获取某个道具的总数
     */
    public int getItemCountFromAll(Player player, int itemId) {
        int count = itemCountMap.getOrDefault(itemId, 0);
        // curios
        List<ItemStack> stackList = CuriosHelper.getEquippedItemsInSlot(player, CuriosHelper.PASSIVE_ITEM);
        for (ItemStack stack : stackList){
            if (!(stack.getItem() instanceof PassiveItem item)) continue;
            if (item.getItemId() == itemId){
                count++;
            }
        }

        return count;
    }

    public Map<Integer, Integer> getItemCountMapFromAll(Player player){
        Map<Integer, Integer> map = new HashMap<>(itemCountMap);

        List<ItemStack> stackList = CuriosHelper.getEquippedItemsInSlot(player, CuriosHelper.PASSIVE_ITEM);
        for (ItemStack stack : stackList) {
            if (stack.isEmpty() || !(stack.getItem() instanceof IsaacItem item)) continue;

            int id = item.getItemId();
            map.put(id, map.getOrDefault(id, 0) + 1);
        }

        return map;
    }


    public List<ItemStack> getItemWithList(int itemId){
        List<ItemStack> list = new ArrayList<>();

        for (ItemStack stack : playerPassiveItems){
            if (((PassiveItem) stack.getItem()).getItemId() == itemId){
                list.add(stack.copy());
            }
        }

        return list;
    }

    /** 将一个新的道具添加到列表 */
    public void addItem(ServerPlayer player, ItemStack stack, InteractionHand hand){
        // 如果道具数量已达上限
        if(this.getTotalItemsCountInCap() >= PASSIVE_ITEM_LIMIT.get()){
            player.sendSystemMessage(Component.translatable("message.isaac_disaster.warning.too_many_items").withStyle(
                    style -> style.withColor(0xFFBF00).withUnderlined(true)
            ));
            return;
        }

        if (!(stack.getItem() instanceof PassiveItem item)) return;
        int itemId = item.getItemId();

        // 触发效果 (先触发效果以适配会对stack本身产生变化的道具；确保后续正确存入)
        PassiveItemManager.getInstance().getItemFromId(itemId).onObtain(player, stack, true);
        PassiveItem.setConsumed(stack, true);

        // 增加被动道具到列表
        playerPassiveItems.add(stack.copy());
        // 更新哈希表：数量+1
        updateItemMap(item.getItemId(), 1);

        // 删除道具(不论是否为创造模式)
        stack.shrink(1);
    }

    public boolean removeFromIndex(ServerPlayer player, int index) {
        // 确保索引有效
        if (index < 0 || index >= playerPassiveItems.size()) {
            return false; // 索引无效，直接返回失败
        }

        ItemStack stack = playerPassiveItems.remove(index);
        int removeId = ((PassiveItem) stack.getItem()).getItemId();

        // 更新哈希表：数量-1（若数量为0则移除键）
        updateItemMap(removeId, -1);

        //移除效果
        PassiveItemManager.getInstance().getItemFromId(removeId).onRemove(player, stack);
        return true;
    }

    /** 依据ID删除最先获取的一个道具 */
    public void removeFromId(ServerPlayer player, int itemId) {
        // 使用迭代器遍历并移除第一个匹配的元素
        for (Iterator<ItemStack> iterator = playerPassiveItems.iterator(); iterator.hasNext(); ) {
            ItemStack stack = iterator.next();
            int id = ((PassiveItem) stack.getItem()).getItemId();
            if (id == itemId) {
                iterator.remove();
                // 更新哈希表：数量-1（若数量为0则移除键）
                updateItemMap(itemId, -1);

                PassiveItemManager.getInstance().getItemFromId(itemId).onRemove(player, stack);
                break;
            }
        }
    }

    public int getSetCountFromId(int setId){
        return setCountMap.getOrDefault(setId, 0);
    }


    public void modifySetCount(ServerPlayer player, int setId, int amount){
        int preCount = getSetCountFromId(setId);
        int newCount = preCount + amount;
        ISet set = SetManager.getInstance().getSetFromId(setId);

        int itemRequirement = set.getRequireCount();

        // 当道具数量突破套装需求时、触发对应效果
        if (preCount < itemRequirement && newCount >= itemRequirement){
            set.onObtain(player);
        }else if(preCount >= itemRequirement && newCount < itemRequirement){
            set.onRemove(player);
        }

        setCountMap.put(setId, newCount);

        // 同步到客户端
        ModMessages.sentToPlayer(new SetCountSyncS2CPacket(setId, newCount), player);
    }



    // 从目标处复制
    public void copyFrom(PlayerPassiveItem source) {
        this.playerPassiveItems = new ArrayList<>(source.playerPassiveItems);
        this.setCountMap = new HashMap<>(source.setCountMap);
        this.obtainedSets = new HashSet<>(source.obtainedSets);
        // 重新计算各类item map
        refreshItemCountMap();
        // set的客户端计数会在event重置
    }

    public void saveNBTData(CompoundTag nbt) {
        // 保存被动道具列表
        ListTag itemList = new ListTag();
        for (ItemStack stack : playerPassiveItems) {
            CompoundTag stackTag = new CompoundTag();
            stack.save(stackTag);
            itemList.add(stackTag);
        }
        nbt.put("PlayerPassiveItems", itemList);

        // 保存 setCountMap
        ListTag setList = new ListTag();
        for (Map.Entry<Integer, Integer> entry : setCountMap.entrySet()) {
            CompoundTag setTag = new CompoundTag();
            setTag.putInt("SetId", entry.getKey());
            setTag.putInt("Count", entry.getValue());
            setList.add(setTag);
        }
        nbt.put("SetCounts", setList);

        // 保存 obtainedSets
        ListTag obtainedList = new ListTag();
        for (int setId : obtainedSets) {
            obtainedList.add(IntTag.valueOf(setId));
        }
        nbt.put("ObtainedSets", obtainedList);
    }


    public void loadNBTData(CompoundTag nbt) {
        playerPassiveItems.clear();
        if (nbt.contains("PlayerPassiveItems", Tag.TAG_LIST)) {
            ListTag itemList = nbt.getList("PlayerPassiveItems", Tag.TAG_COMPOUND);
            for (Tag baseTag : itemList) {
                if (baseTag instanceof CompoundTag stackTag) {
                    ItemStack stack = ItemStack.of(stackTag);
                    playerPassiveItems.add(stack);
                }
            }
        }

        // 重新计算道具表
        refreshItemCountMap();

        // 读取 setCountMap
        setCountMap.clear();
        if (nbt.contains("SetCounts", Tag.TAG_LIST)) {
            ListTag setList = nbt.getList("SetCounts", Tag.TAG_COMPOUND);
            for (Tag baseTag : setList) {
                if (baseTag instanceof CompoundTag setTag) {
                    int setId = setTag.getInt("SetId");
                    int count = setTag.getInt("Count");
                    setCountMap.put(setId, count);
                }
            }
        }

        // 读取 obtainedSets
        obtainedSets.clear();
        if (nbt.contains("ObtainedSets", Tag.TAG_LIST)) {
            ListTag obtainedList = nbt.getList("ObtainedSets", Tag.TAG_INT);
            for (Tag tag : obtainedList) {
                if (tag instanceof IntTag intTag) {
                    obtainedSets.add(intTag.getAsInt());
                }
            }
        }
    }

}