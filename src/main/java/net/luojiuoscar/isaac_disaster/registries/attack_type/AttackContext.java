package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColor;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleInstance;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttackContext {
    public ResourceLocation colorRl;
    private final TriggerModuleQueue triggerModuleQueue;
    public final Map<ResourceLocation, Integer> trajectories;

    public AttackContext(){
        this.colorRl = ModBulletColor.BASE.getId();
        this.trajectories = new HashMap<>();
        this.triggerModuleQueue = new TriggerModuleQueue();
    }

    public AttackContext(ResourceLocation colorRl,
                         TriggerModuleQueue triggerModuleQueue,
                         Map<ResourceLocation, Integer> trajectories) {
        this.colorRl = colorRl;
        // 避免外部修改影响 AttackContext 内部
        this.triggerModuleQueue = new TriggerModuleQueue(triggerModuleQueue.getQueue());
        this.trajectories = new HashMap<>(trajectories);
    }

    public TriggerModuleQueue getTriggerModuleQueue() {
        return triggerModuleQueue;
    }

    public void copyTriggerModule(List<TriggerModuleInstance> source) {
        this.triggerModuleQueue.clear();
        this.triggerModuleQueue.getQueue().addAll(source);
    }

    public void addTriggerModule(ResourceLocation id, int count) {
        triggerModuleQueue.add(id, count);
    }

}
