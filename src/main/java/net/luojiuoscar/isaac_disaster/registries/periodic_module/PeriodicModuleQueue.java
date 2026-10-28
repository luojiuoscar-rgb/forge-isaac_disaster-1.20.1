package net.luojiuoscar.isaac_disaster.registries.periodic_module;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores periodic modules that periodically fire while their stack count is positive.
 */
public class PeriodicModuleQueue {
    private final List<PeriodicModuleInstance> queue;

    public PeriodicModuleQueue(){
        this.queue = new ArrayList<>();
    }

    public PeriodicModuleQueue(List<PeriodicModuleInstance> modules) {
        this.queue = new ArrayList<>(modules);
    }

    public void clear() {
        queue.clear();
    }

    public void copyFrom(PeriodicModuleQueue source) {
        queue.clear();
        for (PeriodicModuleInstance inst : source.queue) {
            queue.add(new PeriodicModuleInstance(inst.id, inst.stacks, inst.coolDown));
        }
    }

    /**
     * Adds an instance restored from persisted data.
     */
    public void rawAdd(PeriodicModuleInstance inst) {
        if (inst == null || inst.stacks <= 0) return;

        queue.add(inst);
    }

    public ListTag saveNBTData() {
        ListTag list = new ListTag();
        for (PeriodicModuleInstance inst : queue) {
            if (inst.stacks <= 0) continue;

            CompoundTag tag = new CompoundTag();
            tag.putString("id", inst.id.toString());
            tag.putInt("stacks", inst.stacks);
            tag.putInt("coolDown", inst.coolDown);
            list.add(tag);
        }
        return list;
    }

    public void loadNBTData(ListTag list) {
        queue.clear();
        for (Tag value : list) {
            if (!(value instanceof CompoundTag tag)) {
                continue;
            }

            try {
                ResourceLocation id = ResourceLocation.parse(tag.getString("id"));
                int stacks = tag.getInt("stacks");
                int coolDown = tag.getInt("coolDown");
                if (stacks <= 0) {
                    continue;
                }

                rawAdd(new PeriodicModuleInstance(id, stacks, coolDown));
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Changes the stack count of one periodic module.
     *
     * <p>Periodic modules are active only while their stack count is positive. Negative changes
     * can remove an existing module, but must not create a negative-stack module that would still
     * be ticked by the queue.</p>
     */
    public void add(LivingEntity entity, ResourceLocation id, int stacks) {
        if (stacks == 0) return;

        IForgeRegistry<PeriodicModule> registry =
                RegistryManager.ACTIVE.getRegistry(ModPeriodicModules.PERIODIC_MODULE_KEY);
        if (registry == null) return;

        PeriodicModule module = registry.getValue(id);
        if (module == null) return;

        PeriodicModuleInstance instance = get(id);
        if (instance == null) {
            if (stacks <= 0) return;

            queue.add(new PeriodicModuleInstance(id, stacks, module.getInitialTick()));
            return;
        }

        instance.stacks += stacks;
        if (instance.stacks <= 0) {
            queue.remove(instance);
            module.handleRemove(entity);
        }
    }

    public List<PeriodicModuleInstance> getQueue(){
        return queue;
    }

    public PeriodicModuleQueue getCopy(){
        return new PeriodicModuleQueue(queue);
    }

    /**
     * Ticks every active module and removes invalid stale instances left by older saves or bugs.
     */
    public void tickAll(LivingEntity entity) {
        List<PeriodicModuleInstance> copy = getCopy().queue;

        for (PeriodicModuleInstance inst : copy) {
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

    public PeriodicModuleInstance get(ResourceLocation id) {
        for (PeriodicModuleInstance inst : queue) {
            if (inst.id.equals(id)) {
                return inst;
            }
        }
        return null;
    }
}
