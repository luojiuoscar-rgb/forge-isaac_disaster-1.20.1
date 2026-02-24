package net.luojiuoscar.isaac_disaster.registries.recursive_module;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.util.ArrayList;
import java.util.List;

public class RecursiveModuleQueue {
    private final List<RecursiveModuleInstance> queue;

    public RecursiveModuleQueue(){
        this.queue = new ArrayList<>();
    }

    public RecursiveModuleQueue(List<RecursiveModuleInstance> modules) {
        this.queue = new ArrayList<>(modules);
    }

    public void clear() {
        queue.clear();
    }

    public void rawAdd(RecursiveModuleInstance inst) {
        queue.add(inst);
    }

    public void add(LivingEntity entity, ResourceLocation id, int stacks) {

        boolean found = false;
        RecursiveModuleInstance instance = null;

        for (RecursiveModuleInstance inst : queue) {
            if (inst.id.equals(id)) {
                inst.stacks += stacks;

                found = true;
                instance = inst;
                break;
            }
        }

        if (!found){
            queue.add(new RecursiveModuleInstance(id, stacks, 0));
        }else{
            if (instance.stacks <= 0){
                // 从queue中移除
                queue.remove(instance);

                IForgeRegistry<IRecursiveModule> registry =
                        RegistryManager.ACTIVE.getRegistry(ModRecursiveModule.RECURSIVE_MODULE_KEY);
                if (registry == null) return;

                IRecursiveModule module = registry.getValue(id);
                if (module == null) return;

                module.handleRemove(entity);
            }
        }
    }

    public List<RecursiveModuleInstance> getQueue(){
        return queue;
    }

    public RecursiveModuleQueue getCopy(){
        return new RecursiveModuleQueue(queue);
    }

    public void tickAll(LivingEntity entity) {
        List<RecursiveModuleInstance> copy = getCopy().queue;

        for (RecursiveModuleInstance inst : copy) {
            inst.coolDown--;
            if (inst.coolDown <= 0) {
                inst.trigger(entity, this);
            }
        }
    }

    public boolean contains(ResourceLocation id) {
        for (RecursiveModuleInstance inst : queue) {
            if (inst.id.equals(id)) {
                return true;
            }
        }
        return false;
    }

    public RecursiveModuleInstance get(ResourceLocation id) {
        for (RecursiveModuleInstance inst : queue) {
            if (inst.id.equals(id)) {
                return inst;
            }
        }
        return null;
    }


}
