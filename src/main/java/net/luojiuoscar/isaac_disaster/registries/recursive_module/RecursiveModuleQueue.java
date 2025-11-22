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

    public void add(ResourceLocation id, int stacks) {
        for (RecursiveModuleInstance inst : queue) {
            if (inst.id.equals(id)) {
                inst.stacks += stacks;
                // 这里不处理remove
                return;
            }
        }
        if (stacks > 0) {
            queue.add(new RecursiveModuleInstance(id, stacks, 0));
        }
    }

    public void remove(ResourceLocation id, LivingEntity entity) {
        boolean removed = queue.removeIf(inst -> inst.id.equals(id));
        if (removed){
            IForgeRegistry<IRecursiveModule> registry =
                    RegistryManager.ACTIVE.getRegistry(ModRecursiveModule.RECURSIVE_MODULE_KEY);
            if (registry == null) return;

            IRecursiveModule instance = registry.getValue(id);
            if (instance == null) return;

            instance.handleRemove(entity);
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
            if (inst.nextPos > entity.tickCount)
                continue;

            if (inst.stacks <= 0) {
                this.remove(inst.id, entity);
                continue;
            }

            inst.trigger(entity, this);
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
