package net.luojiuoscar.isaac_disaster.item.item.custom;

import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class ExperimentalTreatmentItem extends PassiveItem {
    private static final String NBT_MODIFIERS = "Modifiers";

    public ExperimentalTreatmentItem(Properties properties, int itemLevel, int itemId, boolean hasSpecialEffect) {
        super(properties, itemLevel, itemId, hasSpecialEffect);
    }


    @Override
    public void addDescription(List<Component> tooltipComponents, ItemStack stack){
        Map<UUID, Double> map = getModifierMap(stack);
        if (map.isEmpty()) {
            super.addDescription(tooltipComponents, stack);
            return;
        }

        for (Map.Entry<UUID, Double> entry : map.entrySet()) {
            UUID key = entry.getKey();
            Double value = entry.getValue();

            if (value < 0){
                tooltipComponents.add(StatManager.fromUUID(key).description(value, Style.EMPTY.withColor(ChatFormatting.RED)));
            }else if (value <= 1){
                tooltipComponents.add(StatManager.fromUUID(key).description(value, Style.EMPTY.withColor(ChatFormatting.GREEN)));
            }else {
                tooltipComponents.add(StatManager.fromUUID(key).description(value, Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE)));
            }
        }
    }

    /** 将指定修正值写入 NBT */
    public static void setModifier(ItemStack stack, UUID uuid, double ratio) {
        Map<UUID, Double> modifiers = getModifierMap(stack);
        modifiers.put(uuid, ratio);
        saveNBT(stack, modifiers);
    }

    public static Map<UUID, Double> getModifierMap(ItemStack stack) {
        Map<UUID, Double> map = new HashMap<>();
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(NBT_MODIFIERS, Tag.TAG_LIST)) return map;

        ListTag list = tag.getList(NBT_MODIFIERS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            try {
                UUID uuid = UUID.fromString(entry.getString("UUID"));
                double value = entry.getDouble("ratio");
                map.put(uuid, value);
            } catch (Exception ignored) {}
        }
        return map;
    }
    private static void saveNBT(ItemStack stack, Map<UUID, Double> map) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, Double> entry : map.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("UUID", entry.getKey().toString());
            tag.putDouble("ratio", entry.getValue());
            list.add(tag);
        }
        stack.getOrCreateTag().put(NBT_MODIFIERS, list);
    }
}
