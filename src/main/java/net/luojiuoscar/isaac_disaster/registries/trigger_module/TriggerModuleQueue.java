package net.luojiuoscar.isaac_disaster.registries.trigger_module;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.util.ArrayList;
import java.util.List;

public class TriggerModuleQueue {
    public static final TriggerModuleQueue EMPTY = new TriggerModuleQueue();

    private final List<TriggerModuleInstance> queue;
    private boolean locked = false;

    public TriggerModuleQueue(){
        this.queue = new ArrayList<>();
    }

    public TriggerModuleQueue(List<TriggerModuleInstance> modules) {
        this.queue = new ArrayList<>(modules);
    }

    public void clear() {
        if (locked) return;

        queue.clear();
    }

    public void rawAdd(TriggerModuleInstance inst) {
        if (locked) return;

        queue.add(inst);
    }

    /** 仅在当前模块不存在的时候才加 */
    public void addIfNotExist(ResourceLocation id, int stacks){
        if (!contains(id) && stacks > 0) {
            add(id, 1);
        }
    }

    public void add(TriggerModuleInstance inst){
        if (locked) return;

        add(inst.id, inst.stacks);
    }

    public void add(ResourceLocation id, int stacks) {
        if (locked) return;
        // 获取注册表
        IForgeRegistry<TriggerModule> registry =
                RegistryManager.ACTIVE.getRegistry(ModTriggerModule.TRIGGER_MODULE_KEY);
        if (registry == null) return;

        TriggerModule module = registry.getValue(id);
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
        if (locked) return;

        queue.removeIf(inst -> inst.id.equals(id));
    }

    public List<TriggerModuleInstance> getQueue(){
        return queue;
    }

    public TriggerModuleQueue copy(){
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

    public boolean isEmpty(){
        return queue.isEmpty();
    }

    public void lock() {
        this.locked = true;
    }

    public void unlock(){
        this.locked = false;
    }
}
