package net.luojiuoscar.isaac_disaster.item.item.custom;

import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class ExperimentalTreatmentItem extends PassiveItem {
    private static final String NBT_MODIFIERS = "Modifiers";

    public ExperimentalTreatmentItem(Properties properties, RegistryObject<PassiveAbility> ability) {
        super(properties, ability);
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
