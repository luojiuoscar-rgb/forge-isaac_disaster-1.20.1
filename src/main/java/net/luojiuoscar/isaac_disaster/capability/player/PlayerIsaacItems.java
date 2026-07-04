package net.luojiuoscar.isaac_disaster.capability.player;

import net.luojiuoscar.isaac_disaster.helper.CuriosHelper;
import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.ModPassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.SetAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.TrinketAbilityContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.util.*;

import static net.luojiuoscar.isaac_disaster.Config.PASSIVE_ITEM_LIMIT;

@AutoRegisterCapability
public class PlayerIsaacItems {
    // 用编号代表道具。需要查询道具的时候再使用道具管理器
    private final List<ItemStack> playerPassiveItems;
    private final List<ItemStack> swallowedTrinkets;

    private final Map<ResourceLocation, Integer> setCountMap; // 当前套装计数
    private final Set<Integer> obtainedSets; // 已经获得过的套装
    private final Map<CurioSlotKey, ItemStack> activeCurioSlots; // 已经应用过效果的 Curios 槽位


    // constructor
    public PlayerIsaacItems(){
        this.playerPassiveItems = new ArrayList<>();
        this.swallowedTrinkets = new ArrayList<>();

        this.setCountMap = new HashMap<>();
        this.obtainedSets = new HashSet<>();
        this.activeCurioSlots = new HashMap<>();
        init();
    }

    public void init(){
        this.playerPassiveItems.clear();
        this.swallowedTrinkets.clear();

        this.setCountMap.clear();
        this.obtainedSets.clear();
        this.activeCurioSlots.clear();
    }

    public void clearSetMap(){
        setCountMap.clear();
        obtainedSets.clear();
    }

    public List<ItemStack> getPassiveItems(){
        return new ArrayList<>(playerPassiveItems);
    }

    public Map<ResourceLocation, Integer> getSetCountMap(){
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
        clearSetMap();
    }

    /**
     * 获取某个道具的总数
     */
    public int getItemCountFromAll(Player player, int id) {
        int count = (int) playerPassiveItems.stream()
                .filter(s -> id == (((PassiveItem) s.getItem()).getId()))
                .count();
        // curios
        List<ItemStack> stackList = CuriosHelper.getEquippedItemsInSlot(player, CuriosHelper.PASSIVE_ITEM);

        count += (int) stackList.stream()
                .filter(s -> ((s.getItem() instanceof PassiveItem item) && item.getId() == id))
                .count();

        return count;
    }

    public Map<Integer, Integer> getItemCountMap() {
        Map<Integer, Integer> map = new HashMap<>();

        for (ItemStack stack : playerPassiveItems){
            int id = ((PassiveItem) stack.getItem()).getId();

            map.put(id, map.getOrDefault(id, 0) + 1);
        }

        return map;
    }

    public Map<Integer, Integer> getItemCountMapFromAll(Player player) {
        Map<Integer, Integer> map = new HashMap<>();

        for (ItemStack stack : playerPassiveItems){
            int id = ((PassiveItem) stack.getItem()).getId();

            map.put(id, map.getOrDefault(id, 0) + 1);
        }

        // curios
        List<ItemStack> stackList = CuriosHelper.getEquippedItemsInSlot(player, CuriosHelper.PASSIVE_ITEM);

        for (ItemStack stack : stackList){
            int id = ((PassiveItem) stack.getItem()).getId();

            map.put(id, map.getOrDefault(id, 0) + 1);
        }
        return map;
    }

    public List<ItemStack> getItemWithList(int id){
        return playerPassiveItems.stream()
                .filter(s -> ((PassiveItem) s.getItem()).getId() == id)
                .toList();
    }

    /** 将一个新的道具添加到列表 */
    public void addItem(ServerPlayer player, ItemStack stack){
        // 如果道具数量已达上限
        if(this.getTotalItemsCountInCap() >= PASSIVE_ITEM_LIMIT.get()){
            player.sendSystemMessage(Component.translatable("message.isaac_disaster.warning.too_many_items").withStyle(
                    style -> style.withColor(0xFFBF00).withUnderlined(true)
            ));
            return;
        }

        // 增加被动道具到列表
        playerPassiveItems.add(stack.copy());
    }

    public boolean removeFromIndex(ServerPlayer player, int index) {
        // 确保索引有效
        if (index < 0 || index >= playerPassiveItems.size()) {
            return false; // 索引无效，直接返回失败
        }

        ItemStack stack = playerPassiveItems.remove(index);
        ResourceLocation removeId = ForgeRegistries.ITEMS.getKey(stack.getItem());

        IForgeRegistry<PassiveAbility> passiveAbilityIForgeRegistry =
                RegistryManager.ACTIVE.getRegistry(ModPassiveAbility.PASSIVE_ABILITY_KEY);
        if (passiveAbilityIForgeRegistry == null) return false;

        PassiveAbility ability = passiveAbilityIForgeRegistry.getValue(removeId);
        if (ability == null) return false;

        //移除效果
        ability.onRemove(player, stack);
        return true;
    }

    /** 依据ID删除最先获取的一个道具 */
    public void removeFromId(ServerPlayer player, ResourceLocation itemId) {
        // 使用迭代器遍历并移除第一个匹配的元素
        for (Iterator<ItemStack> iterator = playerPassiveItems.iterator(); iterator.hasNext(); ) {
            ItemStack stack = iterator.next();
            ResourceLocation id = (ForgeRegistries.ITEMS.getKey(stack.getItem()));
            if (id != null && id.equals(itemId)) {
                iterator.remove();

                IForgeRegistry<PassiveAbility> passiveAbilityIForgeRegistry =
                        RegistryManager.ACTIVE.getRegistry(ModPassiveAbility.PASSIVE_ABILITY_KEY);
                if (passiveAbilityIForgeRegistry == null) return;

                PassiveAbility ability = passiveAbilityIForgeRegistry.getValue(itemId);
                if (ability == null) return;

                ability.onRemove(player, stack);
                break;
            }
        }
    }

    public int getSetCountFromId(ResourceLocation id){
        return setCountMap.getOrDefault(id, 0);
    }


    public void modifySetCount(ServerPlayer player, ResourceLocation id, int amount){
        IForgeRegistry<SetAbility> registry =
                RegistryManager.ACTIVE.getRegistry(ModSetAbility.SET_ABILITY_KEY);
        if (registry == null) return;

        SetAbility ability = registry.getValue(id);
        if (ability == null) return;

        int itemRequirement = ability.getRequirementCount();
        int preCount = getSetCountFromId(id);
        int newCount = preCount + amount;

        // 当道具数量突破套装需求时、触发对应效果
        if (preCount < itemRequirement && newCount >= itemRequirement){
            ability.onObtain(player);
        }else if(preCount >= itemRequirement && newCount < itemRequirement){
            ability.onRemove(player);
        }

        setCountMap.put(id, newCount);
    }

    /**
     * 将一个 Curios 饰品转移到吞下饰品列表，并消耗原槽位中的 ItemStack。
     *
     * <p>保存进 capability 的是原饰品的副本；原 ItemStack 会被标记为 swallowing，
     * 用于让 Curios 卸下流程识别这是吞下导致的移除，而不是普通卸装。</p>
     */
    public void swallow(ItemStack stack) {
        if (!(stack.getItem() instanceof Trinket)) return;

        addToList(stack.copy());
        Trinket.setSwallowing(stack, true);
        stack.setCount(0);
    }

    /**
     * 直接把饰品副本加入吞下列表，不改动来源 ItemStack。
     *
     * <p>主要用于 NBT 读取和特殊数据迁移；普通吞下流程应调用 {@link #swallow(ItemStack)}。</p>
     */
    public void addToList(ItemStack stack){
        if (!(stack.getItem() instanceof Trinket)) return;

        swallowedTrinkets.add(stack.copy());
    }

    /**
     * 获取玩家已经吞下的饰品列表。
     *
     * @return 返回 ItemStack 副本列表，调用方可以安全调整列表顺序或读取 NBT。
     */
    public List<ItemStack> getSwallowedTrinkets() {
        List<ItemStack> trinkets = new ArrayList<>();
        for (ItemStack stack : swallowedTrinkets) {
            trinkets.add(stack.copy());
        }
        return trinkets;
    }

    /**
     * 获取吞下饰品与当前 Curios 饰品槽中的全部饰品。
     *
     * @param player 查询的玩家
     * @return 吞下饰品副本加上 Curios 当前装备饰品的列表
     */
    public List<ItemStack> getAllTrinkets(Player player) {
        List<ItemStack> stackList = getSwallowedTrinkets();
        stackList.addAll(CuriosHelper.getEquippedItemsInSlot(player, CuriosHelper.TRINKET));
        return new ArrayList<>(stackList);
    }

    /**
     * 删除指定索引的吞下饰品，并触发对应饰品的卸下效果。
     *
     * @param player 触发卸下效果的玩家
     * @param index  吞下饰品列表中的索引
     */
    public void removeAt(Player player, int index){
        if (index < 0 || index >= swallowedTrinkets.size()) return;

        ItemStack stack = swallowedTrinkets.remove(index);
        if (stack.getItem() instanceof Trinket item){
            item.getAbility().onUnequipped(player, new TrinketAbilityContext(stack));
        }
    }

    /**
     * 删除第一个匹配 id 的吞下饰品，并触发对应饰品的卸下效果。
     *
     * @param player 触发卸下效果的玩家
     * @param id     饰品数字 id
     */
    public void removeFromId(Player player, int id){
        for (Iterator<ItemStack> iterator = swallowedTrinkets.iterator(); iterator.hasNext(); ) {
            ItemStack stack = iterator.next();
            if (stack.getItem() instanceof Trinket item &&
                    item.getTrinketId() == id) {
                iterator.remove();
                item.getAbility().onUnequipped(player, new TrinketAbilityContext(stack));
                break;
            }
        }
    }

    /**
     * 清空全部吞下饰品，并逐个触发饰品卸下效果。
     *
     * @param player 触发卸下效果的玩家
     */
    public void clear(Player player) {
        while (!swallowedTrinkets.isEmpty()){
            removeAt(player, 0);
        }
    }

    /**
     * 只从 capability 的吞下饰品列表中按饰品 id 查询。
     *
     * @param id 饰品数字 id
     * @return 匹配饰品的 ItemStack 副本列表
     */
    public List<ItemStack> getCapTrinketListFromId(int id){
        List<ItemStack> trinkets = new ArrayList<>();
        for (ItemStack stack : swallowedTrinkets){
            if (!(stack.getItem() instanceof Trinket trinket)) continue;
            if (trinket.getTrinketId() != id) continue;
            trinkets.add(stack.copy());
        }
        return trinkets;
    }

    /**
     * 从吞下饰品和 Curios 当前装备饰品中按 id 查询全部匹配项。
     *
     * @param player 查询的玩家
     * @param id     饰品数字 id
     * @return 匹配饰品的 ItemStack 副本列表
     */
    public List<ItemStack> getAllTrinketListFromId(Player player, int id){
        List<ItemStack> trinkets = getCapTrinketListFromId(id);
        for (ItemStack stack : CuriosHelper.getEquippedItemsInSlot(player, CuriosHelper.TRINKET)){
            if (stack.getItem() instanceof Trinket item && item.getTrinketId() == id){
                trinkets.add(stack.copy());
            }
        }
        return trinkets;
    }

    public Optional<ItemStack> getActiveCurioStack(CurioSlotKey key) {
        ItemStack stack = activeCurioSlots.get(key);
        if (stack == null || stack.isEmpty()) return Optional.empty();
        return Optional.of(stack.copy());
    }

    public boolean hasActiveCurioSlot(CurioSlotKey key) {
        return activeCurioSlots.containsKey(key);
    }

    public void setActiveCurioStack(CurioSlotKey key, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            activeCurioSlots.remove(key);
            return;
        }

        activeCurioSlots.put(key, stack.copy());
    }

    public Optional<ItemStack> removeActiveCurioStack(CurioSlotKey key) {
        ItemStack stack = activeCurioSlots.remove(key);
        if (stack == null || stack.isEmpty()) return Optional.empty();
        return Optional.of(stack.copy());
    }

    public Map<CurioSlotKey, ItemStack> getActiveCurioSlots() {
        Map<CurioSlotKey, ItemStack> result = new HashMap<>();
        for (Map.Entry<CurioSlotKey, ItemStack> entry : activeCurioSlots.entrySet()) {
            result.put(entry.getKey(), entry.getValue().copy());
        }
        return result;
    }



    // 从目标处复制
    public void copyFrom(PlayerIsaacItems source) {
        this.playerPassiveItems.clear();
        for (ItemStack stack : source.playerPassiveItems) {
            this.playerPassiveItems.add(stack.copy());
        }

        this.swallowedTrinkets.clear();
        for (ItemStack stack : source.swallowedTrinkets) {
            this.swallowedTrinkets.add(stack.copy());
        }

        this.setCountMap.clear();
        this.setCountMap.putAll(source.setCountMap);

        this.obtainedSets.clear();
        this.obtainedSets.addAll(source.obtainedSets);

        this.activeCurioSlots.clear();
        for (Map.Entry<CurioSlotKey, ItemStack> entry : source.activeCurioSlots.entrySet()) {
            this.activeCurioSlots.put(entry.getKey(), entry.getValue().copy());
        }
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
        for (Map.Entry<ResourceLocation, Integer> entry : setCountMap.entrySet()) {
            CompoundTag setTag = new CompoundTag();
            setTag.putString("SetId", entry.getKey().toString());
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

        // 保存已应用效果的 Curios 槽位
        ListTag curioSlotList = new ListTag();
        for (Map.Entry<CurioSlotKey, ItemStack> entry : activeCurioSlots.entrySet()) {
            ItemStack stack = entry.getValue();
            if (stack.isEmpty()) continue;

            CompoundTag curioSlotTag = new CompoundTag();
            curioSlotTag.put("Key", entry.getKey().saveNBT());

            CompoundTag stackTag = new CompoundTag();
            stack.save(stackTag);
            curioSlotTag.put("Stack", stackTag);

            curioSlotList.add(curioSlotTag);
        }
        nbt.put("ActiveCurioSlots", curioSlotList);

        // 保存已吞下的饰品
        ListTag swallowedList = new ListTag();
        for (ItemStack stack : swallowedTrinkets) {
            if (stack.isEmpty()) continue;

            CompoundTag stackTag = new CompoundTag();
            stack.save(stackTag);
            swallowedList.add(stackTag);
        }
        nbt.put("SwallowedTrinkets", swallowedList);
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

        // 读取 setCountMap
        setCountMap.clear();
        if (nbt.contains("SetCounts", Tag.TAG_LIST)) {
            ListTag setList = nbt.getList("SetCounts", Tag.TAG_COMPOUND);
            for (Tag baseTag : setList) {
                if (baseTag instanceof CompoundTag setTag) {
                    ResourceLocation setId = ResourceLocation.parse(setTag.getString("SetId"));
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

        // 读取已应用效果的 Curios 槽位
        activeCurioSlots.clear();
        if (nbt.contains("ActiveCurioSlots", Tag.TAG_LIST)) {
            ListTag curioSlotList = nbt.getList("ActiveCurioSlots", Tag.TAG_COMPOUND);
            for (Tag baseTag : curioSlotList) {
                if (baseTag instanceof CompoundTag curioSlotTag) {
                    CurioSlotKey key = CurioSlotKey.loadNBT(curioSlotTag.getCompound("Key"));
                    ItemStack stack = ItemStack.of(curioSlotTag.getCompound("Stack"));
                    if (!stack.isEmpty()) {
                        activeCurioSlots.put(key, stack);
                    }
                }
            }
        }

        // 读取已吞下的饰品
        swallowedTrinkets.clear();
        if (nbt.contains("SwallowedTrinkets", Tag.TAG_LIST)) {
            ListTag swallowedList = nbt.getList("SwallowedTrinkets", Tag.TAG_COMPOUND);
            for (Tag tag : swallowedList) {
                if (tag instanceof CompoundTag stackTag) {
                    ItemStack stack = ItemStack.of(stackTag);
                    if (!stack.isEmpty()) {
                        addToList(stack);
                    }
                }
            }
        }
    }

}
