package net.luojiuoscar.isaac_disaster.capability.player;

import net.luojiuoscar.isaac_disaster.helper.CuriosHelper;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.manager.item_managers.TrinketManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.ArrayList;
import java.util.List;

@AutoRegisterCapability
public class PlayerSwallowedTrinkets {

    private List<ItemStack> swallowedTrinkets;

    public PlayerSwallowedTrinkets(){
        init();
    }

    public void init(){
        swallowedTrinkets = new ArrayList<>();
    }

    /** 添加一个饰品 */
    public void swallow(Player player, ItemStack stack) {
        if (!(stack.getItem() instanceof Trinket)) return;
        addToList(stack.copy());
        Trinket.setSwallowing(stack, true);
        stack.setCount(0);
    }

    public void addToList(ItemStack stack){
        if (!(stack.getItem() instanceof Trinket)) return;

        swallowedTrinkets.add(stack);
    }


    public List<ItemStack> getSwallowedTrinkets() {
        return new ArrayList<>(swallowedTrinkets);
    }

    public List<ItemStack> getAllTrinkets(Player player) {
        List<ItemStack> stackList = getSwallowedTrinkets();
        stackList.addAll(CuriosHelper.getEquippedItemsInSlot(player, CuriosHelper.TRINKET));
        return new ArrayList<>(stackList);
    }

    public void removeAt(Player player, int index){
        ItemStack stack = swallowedTrinkets.remove(index);
        if (stack.getItem() instanceof Trinket item){
            TrinketManager.getInstance().getTrinketFromId(item.getTrinketId()).onUnequipped(player, Trinket.isEnchanted(stack));
        }
    }

    public void removeFromId(Player player, int id){
        for (ItemStack stack : swallowedTrinkets){
            if (stack.getItem() instanceof Trinket item &&
                    item.getTrinketId() == id) {
                TrinketManager.getInstance().getTrinketFromId(item.getTrinketId()).onUnequipped(player, Trinket.isEnchanted(stack));
                swallowedTrinkets.remove(stack);
                break;
            }
        }
    }

    public void clear(Player player) {
        while (!swallowedTrinkets.isEmpty()){
            removeAt(player, 0);
        }
    }

    public List<ItemStack> getCapTrinketListFromId(int id){
        List<ItemStack> trinkets = new ArrayList<>();
        for (ItemStack stack : swallowedTrinkets){
            if (!(stack.getItem() instanceof Trinket trinket)) continue;
            if (trinket.getTrinketId() != id) continue;
            trinkets.add(stack.copy());
        }
        return trinkets;
    }

    public List<ItemStack> getAllTrinketListFromId(Player player, int id){
        List<ItemStack> trinkets = getCapTrinketListFromId(id);
        for (ItemStack stack : CuriosHelper.getEquippedItemsInSlot(player, CuriosHelper.TRINKET)){
            if (stack.getItem() instanceof Trinket item && item.getTrinketId() == id){
                trinkets.add(stack.copy());
            }
        }
        return trinkets;
    }

    public void saveNBTData(CompoundTag nbt) {
        ListTag list = new ListTag();

        for (ItemStack stack : swallowedTrinkets) {
            if (stack != null && !stack.isEmpty()) {
                CompoundTag stackTag = new CompoundTag();
                stack.save(stackTag);
                list.add(stackTag);
            }
        }

        nbt.put("SwallowedTrinkets", list);
    }

    public void loadNBTData(CompoundTag nbt) {
        swallowedTrinkets.clear();

        if (!nbt.contains("SwallowedTrinkets", Tag.TAG_LIST)) {
            return;
        }

        ListTag list = nbt.getList("SwallowedTrinkets", Tag.TAG_COMPOUND);
        for (Tag tag : list) {
            if (tag instanceof CompoundTag stackTag) {
                ItemStack stack = ItemStack.of(stackTag);
                if (!stack.isEmpty()) {
                    addToList(stack); // 确保list和map同步更新
                }
            }
        }
    }

    public void copyFrom(PlayerSwallowedTrinkets source) {
        this.swallowedTrinkets.clear();
        this.swallowedTrinkets = new ArrayList<>(source.swallowedTrinkets);
    }
}
