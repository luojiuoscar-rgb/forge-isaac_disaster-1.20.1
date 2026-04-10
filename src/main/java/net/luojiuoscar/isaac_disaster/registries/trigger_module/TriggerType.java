package net.luojiuoscar.isaac_disaster.registries.trigger_module;

import net.minecraft.resources.ResourceLocation;

public class TriggerType {
    private final ResourceLocation id;

    public TriggerType(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation getId() {
        return id;
    }

    public boolean is(TriggerType type){
        return id.equals(type.getId()) || isPlaceholder();
    }

    public boolean isPlaceholder(){
        return id.equals(ModTriggerTypes.EMTPY.id);
    }

    @Override
    public String toString() {
        return id.toString();
    }
}