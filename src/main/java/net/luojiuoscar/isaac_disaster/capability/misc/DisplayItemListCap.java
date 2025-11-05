package net.luojiuoscar.isaac_disaster.capability.misc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DisplayItemListCap {
    private final List<ItemStack> itemList = new ArrayList<>();

    private int displayIndex = 0;

    public List<ItemStack> getItemList() {
        return itemList;
    }

    public void setItemList(List<ItemStack> list) {
        itemList.clear();
        if (list != null) itemList.addAll(list);
        displayIndex = 0;
    }

    public void addItem(ItemStack stack){
        itemList.add(stack);
    }

    public void clear(){
        itemList.clear();
    }

    // 获取当前展示的物品
    public ItemStack getDisplayedItem() {
        if (itemList.isEmpty()) return ItemStack.EMPTY;
        return itemList.get(displayIndex % itemList.size());
    }

    // 轮播物品
    public void rotateDisplayedItem() {
        if (!itemList.isEmpty()) {
            displayIndex = (displayIndex + 1) % itemList.size();
        }
    }

    // 持久化
    public void save(CompoundTag tag) {
        ListTag listTag = new ListTag();
        for (ItemStack stack : itemList) {
            CompoundTag stackTag = new CompoundTag();
            stack.save(stackTag);
            listTag.add(stackTag);
        }
        tag.put("itemList", listTag);
        tag.putInt("displayIndex", displayIndex);
    }

    public void load(CompoundTag tag) {
        itemList.clear();
        displayIndex = 0;

        if (tag.contains("itemList", 9)) { // 9 = ListTag
            ListTag listTag = tag.getList("itemList", 10); // 10 = CompoundTag
            for (int i = 0; i < listTag.size(); i++) {
                itemList.add(ItemStack.of(listTag.getCompound(i)));
            }
        }

        if (tag.contains("displayIndex")) {
            displayIndex = tag.getInt("displayIndex");
        }
    }
}
