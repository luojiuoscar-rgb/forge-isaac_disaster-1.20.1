package net.luojiuoscar.isaac_disaster.capability.entity;

import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModuleInstance;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModuleQueue;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleInstance;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public class EffectModules {
    private final TriggerModuleQueue triggerModuleQueue;
    private final RecursiveModuleQueue recursiveModuleQueue;

    public EffectModules() {
        triggerModuleQueue = new TriggerModuleQueue();
        recursiveModuleQueue = new RecursiveModuleQueue();
        init();
    }

    public void init() {
        triggerModuleQueue.clear();
        recursiveModuleQueue.clear();
    }

    public void copyFrom(EffectModules source) {
        triggerModuleQueue.clear();
        this.triggerModuleQueue.getQueue().addAll(source.triggerModuleQueue.getQueue());

        recursiveModuleQueue.clear();
        this.recursiveModuleQueue.getQueue().addAll(source.recursiveModuleQueue.getQueue());
    }

    public void saveNBTData(CompoundTag nbt) {
        /* ---------- Trigger Modules ---------- */
        ListTag triggerList = new ListTag();
        for (TriggerModuleInstance inst : triggerModuleQueue.getQueue()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", inst.id.toString());
            tag.putInt("stacks", inst.stacks);
            tag.putDouble("priority", inst.priority);
            triggerList.add(tag);
        }
        nbt.put("trigger_modules", triggerList);

        /* ---------- Recursive Modules ---------- */
        ListTag recursiveList = new ListTag();
        for (RecursiveModuleInstance inst : recursiveModuleQueue.getQueue()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", inst.id.toString());
            tag.putInt("stacks", inst.stacks);
            tag.putLong("nextPos", inst.nextPos);
            recursiveList.add(tag);
        }
        nbt.put("recursive_modules", recursiveList);
    }


    public void loadNBTData(CompoundTag nbt) {
        /* ---------- Trigger Modules ---------- */
        triggerModuleQueue.clear();
        if (nbt.contains("trigger_modules", Tag.TAG_LIST)) {
            ListTag list = nbt.getList("trigger_modules", Tag.TAG_COMPOUND);
            for (Tag t : list) {
                CompoundTag tag = (CompoundTag) t;
                try {
                    TriggerModuleInstance inst = new TriggerModuleInstance();
                    inst.id = ResourceLocation.parse(tag.getString("id"));
                    inst.stacks = tag.getInt("stacks");
                    inst.priority = tag.getDouble("priority");
                    triggerModuleQueue.rawAdd(inst);
                } catch (Exception ignored) {}
            }
        }

        /* ---------- Recursive Modules ---------- */
        recursiveModuleQueue.clear();
        if (nbt.contains("recursive_modules", Tag.TAG_LIST)) {
            ListTag list = nbt.getList("recursive_modules", Tag.TAG_COMPOUND);
            for (Tag t : list) {
                CompoundTag tag = (CompoundTag) t;
                try {
                    ResourceLocation id = ResourceLocation.parse(tag.getString("id"));
                    int stacks = tag.getInt("stacks");
                    long nextPos = tag.getLong("nextPos");

                    RecursiveModuleInstance inst = new RecursiveModuleInstance(id, stacks, nextPos);

                    recursiveModuleQueue.rawAdd(inst);
                } catch (Exception ignored) {}
            }
        }
    }


    public TriggerModuleQueue getTriggerModules(){
        return triggerModuleQueue;
    }

    public RecursiveModuleQueue getRecursiveModuleQueue(){
        return recursiveModuleQueue;
    }
}
