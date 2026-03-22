package net.luojiuoscar.isaac_disaster.capability.player;

import net.luojiuoscar.isaac_disaster.helper.CuriosHelper;
import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.ModPassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.SetAbility;
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
public class PlayerPassiveItem {
    // 用编号代表道具。需要查询道具的时候再使用道具管理器
    private final List<ItemStack> playerPassiveItems;

    private final Map<ResourceLocation, Integer> setCountMap; // 当前套装计数
    private final Set<Integer> obtainedSets; // 已经获得过的套装


    // constructor
    public PlayerPassiveItem(){
        this.playerPassiveItems = new ArrayList<>();

        this.setCountMap = new HashMap<>();
        this.obtainedSets = new HashSet<>();
        init();
    }

    public void init(){
        this.playerPassiveItems.clear();

        this.setCountMap.clear();
        this.obtainedSets.clear();
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



    // 从目标处复制
    public void copyFrom(PlayerPassiveItem source) {
        this.playerPassiveItems.clear();
        this.playerPassiveItems.addAll(source.playerPassiveItems);

        this.setCountMap.clear();
        this.setCountMap.putAll(source.setCountMap);

        this.obtainedSets.clear();
        this.obtainedSets.addAll(source.obtainedSets);
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
    }

}