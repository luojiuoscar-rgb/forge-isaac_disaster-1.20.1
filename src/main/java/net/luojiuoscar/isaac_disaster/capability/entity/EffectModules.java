package net.luojiuoscar.isaac_disaster.capability.entity;

import net.luojiuoscar.isaac_disaster.registries.periodic_module.PeriodicModuleQueue;
import net.luojiuoscar.isaac_disaster.registries.revive_module.ReviveSequence;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * 目前EffectModule只挂在给玩家以确保性能不会被影响
 * */
public class EffectModules {
    private final TriggerModuleQueue triggerModuleQueue;
    private final PeriodicModuleQueue periodicModuleQueue;
    private final ReviveSequence reviveSequence;

    public EffectModules() {
        triggerModuleQueue = new TriggerModuleQueue();
        periodicModuleQueue = new PeriodicModuleQueue();
        reviveSequence = new ReviveSequence();
        init();
    }

    public void init() {
        triggerModuleQueue.clear();
        periodicModuleQueue.clear();
        reviveSequence.clear();
    }

    public void copyFrom(EffectModules source) {
        triggerModuleQueue.copyFrom(source.triggerModuleQueue);
        periodicModuleQueue.copyFrom(source.periodicModuleQueue);

        reviveSequence.copyFrom(source.reviveSequence);
    }

    public void saveNBTData(CompoundTag nbt) {
        /* ---------- Trigger Modules ---------- */
        nbt.put("trigger_modules", triggerModuleQueue.saveNBTData());

        /* ---------- Periodic Modules ---------- */
        nbt.put("periodic_modules", periodicModuleQueue.saveNBTData());

        /* ---------- Revive Sequence ---------- */
        CompoundTag reviveTag = new CompoundTag();
        reviveSequence.saveNBTData(reviveTag);
        nbt.put("revive_sequence", reviveTag);
    }


    public void loadNBTData(CompoundTag nbt) {
        triggerModuleQueue.clear();
        periodicModuleQueue.clear();
        reviveSequence.clear();

        /* ---------- Trigger Modules ---------- */
        if (nbt.contains("trigger_modules", Tag.TAG_LIST)) {
            triggerModuleQueue.loadNBTData(nbt.getList("trigger_modules", Tag.TAG_COMPOUND));
        }

        /* ---------- Periodic Modules ---------- */
        if (nbt.contains("periodic_modules", Tag.TAG_LIST)) {
            periodicModuleQueue.loadNBTData(nbt.getList("periodic_modules", Tag.TAG_COMPOUND));
        }

        /* ---------- Revive Sequence ---------- */
        if (nbt.contains("revive_sequence", Tag.TAG_COMPOUND)) {
            reviveSequence.loadNBTData(nbt.getCompound("revive_sequence"));
        }
    }


    public TriggerModuleQueue getTriggerModules(){
        return triggerModuleQueue;
    }

    public PeriodicModuleQueue getPeriodicModuleQueue(){
        return periodicModuleQueue;
    }

    public ReviveSequence getReviveSequence() {
        return reviveSequence;
    }
}
