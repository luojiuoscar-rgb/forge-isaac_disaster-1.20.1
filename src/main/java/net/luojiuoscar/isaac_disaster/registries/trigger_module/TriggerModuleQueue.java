package net.luojiuoscar.isaac_disaster.registries.trigger_module;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.util.ArrayList;
import java.util.List;

public class TriggerModuleQueue {
    private final List<TriggerModuleInstance> queue;

    public TriggerModuleQueue(){
        this.queue = new ArrayList<>();
    }

    public TriggerModuleQueue(List<TriggerModuleInstance> modules) {
        this.queue = new ArrayList<>(modules);
    }

    public void clear() {
        queue.clear();
    }

    public void rawAdd(TriggerModuleInstance inst) {
        queue.add(inst);
    }

    public void add(TriggerModuleInstance inst){
        add(inst.id, inst.stacks);
    }

    public void add(ResourceLocation id, int stacks) {
        // 获取注册表
        IForgeRegistry<ITriggerModule> registry =
                RegistryManager.ACTIVE.getRegistry(ModTriggerModule.TRIGGER_MODULE_KEY);
        if (registry == null) return;

        ITriggerModule module = registry.getValue(id);
        if (module == null) return; // 无效 id

        double priority = module.getPriority();
        TriggerModuleInstance found = null;

        for (TriggerModuleInstance inst : queue) {
            if (inst.id.equals(id)) {
                found = inst;
                break;
            }
        }

        if (found != null) {
            // 修改已有实例
            found.stacks += stacks;

            if (found.stacks <= 0) {
                queue.remove(found);
                return;
            }

            found.priority = priority;
            queue.remove(found); // 先移除，重新插入
            insertSortedBinary(found);
        } else {
            if (stacks <= 0) return;

            TriggerModuleInstance newInst = new TriggerModuleInstance(id, stacks, priority);
            insertSortedBinary(newInst);
        }
    }

    private void insertSortedBinary(TriggerModuleInstance inst) {
        int left = 0;
        int right = queue.size() - 1;

        while (left <= right) {
            int mid = (left + right) >>> 1;
            double midPriority = queue.get(mid).priority;

            if (inst.priority > midPriority) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        // left
        queue.add(left, inst);
    }


    public void remove(ResourceLocation id) {
        queue.removeIf(inst -> inst.id.equals(id));
    }

    public List<TriggerModuleInstance> getQueue(){
        return queue;
    }

    public TriggerModuleQueue getCopy(){
        return new TriggerModuleQueue(queue);
    }

    public boolean contains(ResourceLocation id) {
        for (TriggerModuleInstance inst : queue) {
            if (inst.id.equals(id)) {
                return true;
            }
        }
        return false;
    }

    public TriggerModuleInstance get(ResourceLocation id) {
        for (TriggerModuleInstance inst : queue) {
            if (inst.id.equals(id)) {
                return inst;
            }
        }
        return null;
    }

}
