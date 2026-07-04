package net.luojiuoscar.isaac_disaster.capability.player;

import net.minecraft.nbt.CompoundTag;
import top.theillusivec4.curios.api.SlotContext;

public record CurioSlotKey(String slotType, int index) {
    private static final String SLOT_TYPE = "SlotType";
    private static final String INDEX = "Index";

    public CurioSlotKey {
        if (slotType == null) {
            slotType = "";
        }
    }

    public static CurioSlotKey from(SlotContext slotContext) {
        return new CurioSlotKey(slotContext.identifier(), slotContext.index());
    }

    public CompoundTag saveNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString(SLOT_TYPE, slotType);
        tag.putInt(INDEX, index);
        return tag;
    }

    public static CurioSlotKey loadNBT(CompoundTag tag) {
        return new CurioSlotKey(tag.getString(SLOT_TYPE), tag.getInt(INDEX));
    }
}
