package net.luojiuoscar.isaac_disaster.capability.player;

import net.luojiuoscar.isaac_disaster.helper.CuriosHelper;
import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.INewBulletTypePassiveItem;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IRecursivePassiveItem;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.ITriggerPassiveItem;
import net.luojiuoscar.isaac_disaster.item_ability.set.ISet;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PassiveItemManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.SetManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.ObtainPassiveItemS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.SetCountSyncS2CPacket;
import net.minecraft.nbt.CompoundTag;
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
    private Map<Integer, Integer> itemCountMap;
    private Map<Integer, Integer> triggerItemMap;
    private Map<Integer, Integer> recursiveItemMap; // itemId : remainTick
    private Map<Integer, Integer> newBulletTypeMap;

    private Map<Integer, Integer> setCountMap; // 当前套装计数
    private Map<Integer, Integer> permanentSetCountMap; // 被cap记录的set数量（由永久效果给予的套装数量）


    // constructor
    public PlayerPassiveItem(){
        init();
    }

    public void init(){
        this.playerPassiveItems = new ArrayList<>();

        this.itemCountMap = new HashMap<>();
        this.triggerItemMap = new HashMap<>();
        this.recursiveItemMap = new HashMap<>();
        this.setCountMap = new HashMap<>();
        this.permanentSetCountMap = new HashMap<>();
        this.newBulletTypeMap = new HashMap<>();
    }





    /**
     * 清空全部的哈希表
     */
    public void clearItemMap(){
        itemCountMap.clear();
        triggerItemMap.clear();
        recursiveItemMap.clear();
        setCountMap.clear();
        permanentSetCountMap.clear();
        newBulletTypeMap.clear();
    }


    /**
     * 移除道具后更新哈希表计数
     * 不用删除值为0的映射；在玩家死亡等时机由于表格刷新会自动删除
     */
    private void updateItemMap(int itemId, int amount) {
        itemCountMap.put(itemId, itemCountMap.getOrDefault(itemId, 0) + amount);
        // 如果是触发型道具
        if(PassiveItemManager.getInstance().getItemFromId(itemId) instanceof ITriggerPassiveItem){
            triggerItemMap.put(itemId, itemCountMap.get(itemId));
        }
        // 如果是循环型道具
        if(PassiveItemManager.getInstance().getItemFromId(itemId) instanceof IRecursivePassiveItem item){
            // 对于循环型道具需要移除计数器
            int itemCount = itemCountMap.get(itemId);
            if (itemCount == 0){
                recursiveItemMap.remove(itemId);
                return;
            }
            // 初始计时
            recursiveItemMap.put(itemId, item.getTickInterval());
        }
        // 如果可以射出特殊类型的子弹
        if(PassiveItemManager.getInstance().getItemFromId(itemId) instanceof INewBulletTypePassiveItem){
            newBulletTypeMap.put(itemId, itemCountMap.get(itemId));
        }
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
        return Collections.unmodifiableList(playerPassiveItems);
    }
    public Map<Integer, Integer> getCapTriggerItems(){
        return Collections.unmodifiableMap(triggerItemMap);
    }

    /** 包含cap和curios在内的所有触发型道具 */
    public Map<Integer, Integer> getAllTriggerItems(Player player){
        Map<Integer, Integer> map = new HashMap<>(triggerItemMap);

        List<ItemStack> curiosItems = CuriosHelper.getEquippedItemsInSlot(player, CuriosHelper.PASSIVE_ITEM);
        for (ItemStack stack : curiosItems){
            if (!(stack.getItem() instanceof PassiveItem item)) continue;
            int itemId = item.getItemId();

            if (!(PassiveItemManager.getInstance().getItemFromId(itemId) instanceof ITriggerPassiveItem)) continue;

            map.put(itemId, 1 + map.getOrDefault(itemId, 0));
        }

        return map;
    }

    public Map<Integer, Integer> getCapRecursiveItems(){
        return Collections.unmodifiableMap(recursiveItemMap);
    }
    /** 包含cap和curios在内的所有循环型道具 */
    public Map<Integer, Integer> getAllRecursiveItems(Player player){
        Map<Integer, Integer> map = new HashMap<>(recursiveItemMap);

        List<ItemStack> curiosItems = CuriosHelper.getEquippedItemsInSlot(player, CuriosHelper.PASSIVE_ITEM);
        for (ItemStack stack : curiosItems){
            if (!(stack.getItem() instanceof PassiveItem item)) continue;
            int itemId = item.getItemId();

            if (!(PassiveItemManager.getInstance().getItemFromId(itemId) instanceof IRecursivePassiveItem)) continue;

            map.put(itemId, 1 + map.getOrDefault(itemId, 0));
        }

        return map;
    }

    public Map<Integer, Integer> getSetCountMap(){
        return Collections.unmodifiableMap(setCountMap);
    }

    /** 包含cap和curios在内的所有子弹类型改变道具 */
    public Map<Integer, Integer> getAllNewBulletTypeItems(Player player){
        Map<Integer, Integer> map = new HashMap<>(newBulletTypeMap);

        List<ItemStack> curiosItems = CuriosHelper.getEquippedItemsInSlot(player, CuriosHelper.PASSIVE_ITEM);
        for (ItemStack stack : curiosItems){
            if (!(stack.getItem() instanceof PassiveItem item)) continue;
            int itemId = item.getItemId();

            if (!(PassiveItemManager.getInstance().getItemFromId(itemId) instanceof INewBulletTypePassiveItem)) continue;

            map.put(itemId, 1 + map.getOrDefault(itemId, 0));
        }


        return map;
    }

    /**
     * 循环型道具的任务计时器
     */
    public void recursiveItemTick(ServerPlayer player, int tick){
        for (Map.Entry<Integer, Integer> entry : recursiveItemMap.entrySet()) {
            entry.setValue(entry.getValue() - tick);
            // 计数器归零时
            if (entry.getValue() <= 0){
                int itemId = entry.getKey();
                IRecursivePassiveItem item = (IRecursivePassiveItem) PassiveItemManager.getInstance().getItemFromId(itemId);
                // 触发循环效果
                item.recursiveEffect(player);
                entry.setValue(item.getTickInterval());
            }
        }
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

        // 增加被动道具到列表
        playerPassiveItems.add(stack.copy());
        // 更新哈希表：数量+1
        updateItemMap(item.getItemId(), 1);

        // 触发效果 Obtain效果仅触发一次
        if (!PassiveItem.isConsumed(stack)) {
            PassiveItemManager.getInstance().getItemFromId(itemId).onFirstObtain(player, true);
        }
        PassiveItemManager.getInstance().getItemFromId(itemId).onObtain(player, true);
        ModMessages.sentToPlayer(new ObtainPassiveItemS2CPacket(itemId), player);
        PassiveItem.setConsumed(stack, true);

        // 删除手持的道具原型(不论是否为创造模式)
        player.getItemInHand(hand).shrink(1);
    }

    /** 将一个新的道具添加到列表。同时无视首次添加效果 */
    public void addItemWithoutFirst(ServerPlayer player, ItemStack stack){
        // 如果道具数量已达上限
        if(this.getTotalItemsCountInCap() >= PASSIVE_ITEM_LIMIT.get()){
            player.sendSystemMessage(Component.translatable("message.isaac_disaster.warning.too_many_items").withStyle(
                    style -> style.withColor(0xFFBF00).withUnderlined(true)
            ));
            return;
        }

        if (!(stack.getItem() instanceof PassiveItem item)) return;
        int itemId = item.getItemId();

        playerPassiveItems.add(stack);
        // 更新哈希表：数量+1
        updateItemMap(itemId, 1);

        PassiveItemManager.getInstance().getItemFromId(itemId).onObtain(player, true);
        ModMessages.sentToPlayer(new ObtainPassiveItemS2CPacket(itemId), player);
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
        PassiveItemManager.getInstance().getItemFromId(removeId).onRemove(player, true);
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

                PassiveItemManager.getInstance().getItemFromId(itemId).onRemove(player, true);
                break;
            }
        }
    }

    public int getSetCountFromId(int setId){
        return setCountMap.getOrDefault(setId, 0);
    }

    public int getPermanentSetCountFromId(int setId){
        return permanentSetCountMap.getOrDefault(setId, 0);
    }

    public void modifySetCount(ServerPlayer player, int setId, int amount, boolean isPermanent){
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
        if (isPermanent){
            permanentSetCountMap.put(setId, getPermanentSetCountFromId(setId) + amount);
        }
        // 同步到客户端
        ModMessages.sentToPlayer(new SetCountSyncS2CPacket(setId, newCount), player);
    }



    // 从目标处复制
    public void copyFrom(PlayerPassiveItem source) {
        this.playerPassiveItems = new ArrayList<>(source.playerPassiveItems);
        this.permanentSetCountMap = new HashMap<>(source.permanentSetCountMap);
        this.setCountMap = new HashMap<>(source.permanentSetCountMap);
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
        for (Map.Entry<Integer, Integer> entry : permanentSetCountMap.entrySet()) {
            CompoundTag setTag = new CompoundTag();
            setTag.putInt("SetId", entry.getKey());
            setTag.putInt("Count", entry.getValue());
            setList.add(setTag);
        }
        nbt.put("SetCounts", setList);
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
        permanentSetCountMap.clear();
        if (nbt.contains("SetCounts", Tag.TAG_LIST)) {
            ListTag setList = nbt.getList("SetCounts", Tag.TAG_COMPOUND);
            for (Tag baseTag : setList) {
                if (baseTag instanceof CompoundTag setTag) {
                    int setId = setTag.getInt("SetId");
                    int count = setTag.getInt("Count");
                    setCountMap.put(setId, count);
                    permanentSetCountMap.put(setId, count);
                }
            }
        }
    }

}