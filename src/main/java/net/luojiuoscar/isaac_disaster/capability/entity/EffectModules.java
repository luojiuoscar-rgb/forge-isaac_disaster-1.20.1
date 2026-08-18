package net.luojiuoscar.isaac_disaster.capability.entity;

import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModuleQueue;
import net.luojiuoscar.isaac_disaster.registries.revive_module.ReviveSequence;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * 目前EffectModule只挂在给玩家以确保性能不会被影响
 * */
public class EffectModules {
    private final TriggerModuleQueue triggerModuleQueue;
    private final RecursiveModuleQueue recursiveModuleQueue;
    private final ReviveSequence reviveSequence;

    public EffectModules() {
        triggerModuleQueue = new TriggerModuleQueue();
        recursiveModuleQueue = new RecursiveModuleQueue();
        reviveSequence = new ReviveSequence();
        init();
    }

    public void init() {
        triggerModuleQueue.clear();
        recursiveModuleQueue.clear();
        reviveSequence.clear();
    }

    public void copyFrom(EffectModules source) {
        triggerModuleQueue.copyFrom(source.triggerModuleQueue);
        recursiveModuleQueue.copyFrom(source.recursiveModuleQueue);

        reviveSequence.copyFrom(source.reviveSequence);
    }

    public void saveNBTData(CompoundTag nbt) {
        /* ---------- Trigger Modules ---------- */
        nbt.put("trigger_modules", triggerModuleQueue.saveNBTData());

        /* ---------- Recursive Modules ---------- */
        nbt.put("recursive_modules", recursiveModuleQueue.saveNBTData());

        /* ---------- Revive Sequence ---------- */
        CompoundTag reviveTag = new CompoundTag();
        reviveSequence.saveNBTData(reviveTag);
        nbt.put("revive_sequence", reviveTag);
    }


    public void loadNBTData(CompoundTag nbt) {
        triggerModuleQueue.clear();
        recursiveModuleQueue.clear();
        reviveSequence.clear();

        /* ---------- Trigger Modules ---------- */
        if (nbt.contains("trigger_modules", Tag.TAG_LIST)) {
            triggerModuleQueue.loadNBTData(nbt.getList("trigger_modules", Tag.TAG_COMPOUND));
        }

        /* ---------- Recursive Modules ---------- */
        if (nbt.contains("recursive_modules", Tag.TAG_LIST)) {
            recursiveModuleQueue.loadNBTData(nbt.getList("recursive_modules", Tag.TAG_COMPOUND));
        }

        /* ---------- Revive Sequence ---------- */
        if (nbt.contains("revive_sequence", Tag.TAG_COMPOUND)) {
            reviveSequence.loadNBTData(nbt.getCompound("revive_sequence"));
        }
    }


    public TriggerModuleQueue getTriggerModules(){
        return triggerModuleQueue;
    }

    public RecursiveModuleQueue getRecursiveModuleQueue(){
        return recursiveModuleQueue;
    }

    public ReviveSequence getReviveSequence() {
        return reviveSequence;
    }
}
