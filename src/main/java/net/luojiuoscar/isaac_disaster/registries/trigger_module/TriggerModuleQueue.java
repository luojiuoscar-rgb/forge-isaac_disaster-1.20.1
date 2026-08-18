package net.luojiuoscar.isaac_disaster.registries.trigger_module;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.rule.TriggerModuleSnapshot;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TriggerModuleQueue {
    private final List<TriggerModuleInstance> queue = new ArrayList<>();

    public void clear() {
        queue.clear();
    }

    public void copyFrom(TriggerModuleQueue source) {
        replaceAll(source.snapshot().modules());
    }

    public ListTag saveNBTData() {
        ListTag list = new ListTag();
        for (TriggerModuleInstance inst : queue) {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", inst.id().toString());
            tag.putInt("stacks", inst.stacks());
            tag.putDouble("priority", inst.priority());
            list.add(tag);
        }
        return list;
    }

    public void loadNBTData(ListTag list) {
        List<TriggerModuleInstance> modules = new ArrayList<>();
        for (Tag value : list) {
            if (!(value instanceof CompoundTag tag)) {
                continue;
            }

            try {
                modules.add(new TriggerModuleInstance(
                        ResourceLocation.parse(tag.getString("id")),
                        tag.getInt("stacks"),
                        tag.getDouble("priority")));
            } catch (Exception ignored) {
            }
        }
        replaceAll(modules);
    }

    public void replaceAll(List<TriggerModuleInstance> modules) {
        queue.clear();
        modules.stream()
                .filter(module -> module.stacks() > 0)
                .sorted(Comparator.comparingDouble(TriggerModuleInstance::priority).reversed())
                .forEach(queue::add);
    }

    /** 仅在当前模块不存在的时候才加 */
    public void addIfNotExist(ResourceLocation id, int stacks) {
        if (!contains(id) && stacks > 0) {
            add(id, 1);
        }
    }

    public void add(TriggerModuleInstance instance) {
        add(instance.id(), instance.stacks());
    }

    public void add(ResourceLocation id, int stacks) {
        // 获取注册表
        IForgeRegistry<TriggerModule> registry =
                RegistryManager.ACTIVE.getRegistry(ModTriggerModule.TRIGGER_MODULE_KEY);
        if (registry == null) return;

        TriggerModule module = registry.getValue(id);
        if (module == null) return; // 无效 id

        TriggerModuleInstance existing = get(id);
        if (existing == null) {
            if (stacks > 0) {
                insertSorted(new TriggerModuleInstance(id, stacks, module.getPriority()));
            }
            return;
        }

        queue.remove(existing);
        int updatedStacks = existing.stacks() + stacks;
        if (updatedStacks > 0) {
            insertSorted(new TriggerModuleInstance(id, updatedStacks, module.getPriority()));
        }
    }

    public void remove(ResourceLocation id) {
        queue.removeIf(instance -> instance.id().equals(id));
    }

    public TriggerModuleSnapshot snapshot() {
        return queue.isEmpty() ? TriggerModuleSnapshot.empty() : new TriggerModuleSnapshot(queue);
    }

    public boolean contains(ResourceLocation id) {
        return get(id) != null;
    }

    public TriggerModuleInstance get(ResourceLocation id) {
        for (TriggerModuleInstance instance : queue) {
            if (instance.id().equals(id)) {
                return instance;
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    private void insertSorted(TriggerModuleInstance instance) {
        int left = 0;
        int right = queue.size() - 1;

        while (left <= right) {
            int middle = (left + right) >>> 1;
            if (instance.priority() > queue.get(middle).priority()) {
                right = middle - 1;
            } else {
                left = middle + 1;
            }
        }
        // left
        queue.add(left, instance);
    }
}
