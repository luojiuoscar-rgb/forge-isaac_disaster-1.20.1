package net.luojiuoscar.isaac_disaster.capability.entity;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleInstance;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public class TriggerModule {
    private TriggerModuleQueue triggerModuleQueue;

    public TriggerModule() {
        triggerModuleQueue = new TriggerModuleQueue();
        init();
    }

    public void init() {
        triggerModuleQueue.clear();
    }

    public void copyFrom(TriggerModule source) {
        this.triggerModuleQueue = new TriggerModuleQueue(source.triggerModuleQueue.getQueue());
    }

    public void saveNBTData(CompoundTag nbt) {
        ListTag triggerList = new ListTag();
        for (TriggerModuleInstance inst : triggerModuleQueue.getQueue()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", inst.id.toString());
            tag.putInt("stacks", inst.stacks);
            tag.putDouble("priority", inst.priority);
            triggerList.add(tag);
        }
        nbt.put("trigger_modules", triggerList);
    }

    public void loadNBTData(CompoundTag nbt) {
        triggerModuleQueue.clear();
        if (nbt.contains("trigger_modules", Tag.TAG_LIST)) {
            ListTag list = nbt.getList("trigger_modules", Tag.TAG_COMPOUND);
            for (Tag t : list) {
                CompoundTag tag = (CompoundTag)t;
                try {
                    TriggerModuleInstance inst = new TriggerModuleInstance();
                    inst.id = ResourceLocation.parse(tag.getString("id"));
                    inst.stacks = tag.getInt("stacks");
                    inst.priority = tag.getDouble("priority");
                    triggerModuleQueue.add(inst);
                } catch (Exception ignored) {}
            }
        }
    }

    public TriggerModuleQueue getTriggerModules(){
        IsaacDisaster.LOGGER.info("Returned queue");
        return triggerModuleQueue;
    }
}
