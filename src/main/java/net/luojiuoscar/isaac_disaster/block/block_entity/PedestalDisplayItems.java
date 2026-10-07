package net.luojiuoscar.isaac_disaster.block.block_entity;

import net.luojiuoscar.isaac_disaster.capability.misc.DisplayItemListCap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/** Keeps the pedestal's optional cycling display inside its item-mutation boundary. */
final class PedestalDisplayItems extends DisplayItemListCap {
    private final Runnable onChange;

    PedestalDisplayItems(Runnable onChange) {
        this.onChange = onChange;
    }

    private static ItemStack normalize(ItemStack stack) {
        ItemStack copy = stack.copy();
        if (!copy.isEmpty()) copy.setCount(1);
        return copy;
    }

    @Override
    public List<ItemStack> getItemList() {
        return super.getItemList().stream().map(ItemStack::copy)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ItemStack getDisplayedItem() {
        return super.getDisplayedItem().copy();
    }

    @Override
    public void setItemList(List<ItemStack> list) {
        super.setItemList(list == null ? List.of() : list.stream()
                .filter(stack -> stack != null && !stack.isEmpty()).map(PedestalDisplayItems::normalize).toList());
        onChange.run();
    }

    @Override
    public void addItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        super.addItem(normalize(stack));
        onChange.run();
    }

    @Override
    public void rotateDisplayedItem() {
        super.rotateDisplayedItem();
        onChange.run();
    }

    @Override
    public void clear() {
        super.setItemList(List.of());
        onChange.run();
    }

    @Override
    public void load(CompoundTag tag) {
        CompoundTag normalized = tag.copy();
        normalized.putInt("displayIndex", Math.max(0, tag.getInt("displayIndex")));
        super.load(normalized);
        super.getItemList().removeIf(ItemStack::isEmpty);
        super.getItemList().replaceAll(PedestalDisplayItems::normalize);
        onChange.run();
    }
}
