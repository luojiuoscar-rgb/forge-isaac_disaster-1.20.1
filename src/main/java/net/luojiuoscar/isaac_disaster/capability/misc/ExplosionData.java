package net.luojiuoscar.isaac_disaster.capability.misc;

import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.server.level.ServerPlayer;

public class ExplosionData {

    private ServerPlayer owner;
    private TriggerModuleQueue trigger;

    public ServerPlayer getOwner() {
        return owner;
    }

    public void setOwner(ServerPlayer player) {
        this.owner = player;
    }

    public TriggerModuleQueue getTriggerModuleQueue() {
        return trigger;
    }

    public void setTriggerModuleQueue(TriggerModuleQueue trigger) {
        this.trigger = trigger;
    }
}
