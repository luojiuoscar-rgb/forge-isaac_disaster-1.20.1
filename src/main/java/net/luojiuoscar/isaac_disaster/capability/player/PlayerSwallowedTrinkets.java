package net.luojiuoscar.isaac_disaster.capability.player;

import net.luojiuoscar.isaac_disaster.manager.item_managers.TrinketManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@AutoRegisterCapability
public class PlayerSwallowedTrinkets {

    // 记录每个饰品状态的内部类
    public static class SwallowedTrinket {
        public int itemId;
        public boolean enchanted;

        public SwallowedTrinket(int itemId, boolean enchanted) {
            this.itemId = itemId;
            this.enchanted = enchanted;
        }

        public CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("item_id", itemId);
            tag.putBoolean("enchanted", enchanted);
            return tag;
        }

        public static SwallowedTrinket fromTag(CompoundTag tag) {
            return new SwallowedTrinket(
                    tag.getInt("item_id"),
                    tag.getBoolean("enchanted")
            );
        }
    }

    // 按顺序存储
    private final List<SwallowedTrinket> swallowedTrinkets = new ArrayList<>();

    // 添加饰品
    public void swallow(int itemId, boolean enchanted) {
        swallowedTrinkets.add(new SwallowedTrinket(itemId, enchanted));
    }


    public boolean removeAt(Player player, int index) {
        if (index < 0 || index >= swallowedTrinkets.size()) return false;
        SwallowedTrinket t = swallowedTrinkets.remove(index);
        TrinketManager.getInstance().getTrinketFromId(t.itemId).onUnequipped(player);
        return true;
    }
    public boolean removeById(Player player, int itemId) {
        for (Iterator<SwallowedTrinket> it = swallowedTrinkets.iterator(); it.hasNext(); ) {
            SwallowedTrinket t = it.next();
            if (t.itemId == itemId) {
                TrinketManager.getInstance().getTrinketFromId(t.itemId).onUnequipped(player);
                it.remove();
                return true;
            }
        }
        return false;
    }

    public List<SwallowedTrinket> getSwallowedTrinkets() {
        return new ArrayList<>(swallowedTrinkets);
    }

    public int getTotal() {
        return swallowedTrinkets.size();
    }

    public void clearAll(Player player){
        for (int i = 0; i < getTotal(); i++){
            removeAt(player, 0);
        }
    }


    public void saveNBTData(CompoundTag nbt) {
        ListTag list = new ListTag();
        for (SwallowedTrinket trinket : swallowedTrinkets) {
            list.add(trinket.toTag());
        }
        nbt.put("SwallowedTrinkets", list);
    }


    public void loadNBTData(CompoundTag nbt) {
        swallowedTrinkets.clear();
        if (nbt.contains("SwallowedTrinkets", Tag.TAG_LIST)) {
            ListTag list = nbt.getList("SwallowedTrinkets", Tag.TAG_COMPOUND);
            for (Tag t : list) {
                if (t instanceof CompoundTag tag) {
                    swallowedTrinkets.add(SwallowedTrinket.fromTag(tag));
                }
            }
        }
    }


    public void copyFrom(PlayerSwallowedTrinkets source) {
        this.swallowedTrinkets.clear();
        this.swallowedTrinkets.addAll(source.swallowedTrinkets.stream()
                .map(t -> new SwallowedTrinket(t.itemId, t.enchanted))
                .toList());
    }
}
