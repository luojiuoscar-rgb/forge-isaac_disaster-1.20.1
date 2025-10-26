package net.luojiuoscar.isaac_disaster.item.item.custom;

import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;


public class ExperimentalTreatment extends PassiveItem {
    private static final String NBT_MODIFIERS = "Modifiers";

    public ExperimentalTreatment(Properties properties, int itemLevel, int itemId) {
        super(properties, itemLevel, itemId);
    }

    /** 当玩家首次获得该物品时触发，生成若干随机属性。 */
    public static void shuffle(Player player, ItemStack stack) {
        RandomSource random = player.getRandom();
        // 随机 3~8 条属性
        int total = random.nextInt(3, 9);
        List<UUID> uuidList = new ArrayList<>(StatManager.getAllNormalTypeUUID().stream().toList());
        Collections.shuffle(uuidList);
        Set<UUID> picked = new HashSet<>(uuidList.subList(0, total));

        for (UUID uuid : picked){
            double radio = random.nextDouble() * 2.2 - 1;
            setModifier(stack, uuid, radio); // 记录筛选出来的内容
        }
    }

    /** 将指定修正值写入 NBT */
    public static void setModifier(ItemStack stack, UUID uuid, double ratio) {
        Map<UUID, Double> modifiers = loadNBT(stack);
        modifiers.put(uuid, ratio);
        saveNBT(stack, modifiers);
    }


    @Override
    public void addDescription(List<Component> tooltipComponents){

    }










    public static Map<UUID, Double> loadNBT(ItemStack stack) {
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
