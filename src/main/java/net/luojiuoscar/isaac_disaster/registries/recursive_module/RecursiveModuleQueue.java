package net.luojiuoscar.isaac_disaster.registries.recursive_module;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores recursive modules that periodically fire while their stack count is positive.
 */
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

    /**
     * Adds an instance restored from persisted data.
     */
    public void rawAdd(RecursiveModuleInstance inst) {
        if (inst == null || inst.stacks <= 0) return;

        queue.add(inst);
    }

    /**
     * Changes the stack count of one recursive module.
     *
     * <p>Recursive modules are active only while their stack count is positive. Negative changes
     * can remove an existing module, but must not create a negative-stack module that would still
     * be ticked by the queue.</p>
     */
    public void add(LivingEntity entity, ResourceLocation id, int stacks) {
        if (stacks == 0) return;

        IForgeRegistry<RecursiveModule> registry =
                RegistryManager.ACTIVE.getRegistry(ModRecursiveModule.RECURSIVE_MODULE_KEY);
        if (registry == null) return;

        RecursiveModule module = registry.getValue(id);
        if (module == null) return;

        RecursiveModuleInstance instance = get(id);
        if (instance == null) {
            if (stacks <= 0) return;

            queue.add(new RecursiveModuleInstance(id, stacks, module.getInitialTick()));
            return;
        }

        instance.stacks += stacks;
        if (instance.stacks <= 0) {
            queue.remove(instance);
            module.handleRemove(entity);
        }
    }

    public List<RecursiveModuleInstance> getQueue(){
        return queue;
    }

    public RecursiveModuleQueue getCopy(){
        return new RecursiveModuleQueue(queue);
    }

    /**
     * Ticks every active module and removes invalid stale instances left by older saves or bugs.
     */
    public void tickAll(LivingEntity entity) {
        List<RecursiveModuleInstance> copy = getCopy().queue;

        for (RecursiveModuleInstance inst : copy) {
            if (!queue.contains(inst) || inst.stacks <= 0) {
                queue.remove(inst);
                continue;
            }

            inst.coolDown--;
            if (inst.coolDown <= 0) {
                inst.trigger(entity, this);
            }
        }
    }

    public boolean contains(ResourceLocation id) {
        return get(id) != null;
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
