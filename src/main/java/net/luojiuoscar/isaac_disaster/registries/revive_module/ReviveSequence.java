package net.luojiuoscar.isaac_disaster.registries.revive_module;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.ReviveEntityEventS2CPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReviveSequence {
    private static final String PROVIDER_KEY = "provider_entries";
    private static final String CONSUMER_KEY = "consumer_entries";
    private static final String NEXT_ORDER_KEY = "next_grant_order";

    private static final Comparator<Entry> ENTRY_ORDER = Comparator
            .comparingDouble(Entry::priority).reversed()
            .thenComparingLong(Entry::grantOrder);

    private final List<Entry> providerEntries;
    private final List<Entry> consumerEntries;
    private long nextGrantOrder;

    public ReviveSequence() {
        this.providerEntries = new ArrayList<>();
        this.consumerEntries = new ArrayList<>();
    }

    public void clear() {
        providerEntries.clear();
        consumerEntries.clear();
        nextGrantOrder = 0L;
    }

    public void clear(ResourceLocation id) {
        if (id == null) {
            return;
        }

        providerEntries.removeIf(entry -> entry.id().equals(id));
        consumerEntries.removeIf(entry -> entry.id().equals(id));
    }

    public void copyFrom(ReviveSequence source) {
        clear();
        nextGrantOrder = source.nextGrantOrder;

        for (Entry entry : source.providerEntries) {
            providerEntries.add(entry.copy());
        }
        for (Entry entry : source.consumerEntries) {
            consumerEntries.add(entry.copy());
        }

        providerEntries.sort(ENTRY_ORDER);
        consumerEntries.sort(ENTRY_ORDER);
    }

    public void addProvider(ResourceLocation id, int count) {
        mutateEntries(providerEntries, id, count);
    }

    public void addConsumer(ResourceLocation id, int count) {
        mutateEntries(consumerEntries, id, count);
    }

    public boolean tryConsumeOnDeath(ServerPlayer player, LivingDeathEvent event) {
        if (player == null || event == null) {
            return false;
        }

        while (!consumerEntries.isEmpty()) {
            Entry entry = consumerEntries.remove(0);
            ReviveModule module = resolveRegisteredModule(entry.id());
            if (module == null) {
                continue;
            }

            event.setCanceled(true);
            player.setHealth(1.0F); // default health

            ExecutableEffectContext context = new ExecutableEffectContext(player);
            context.set(ContextKeys.EVENT, event);
            context.set(ContextKeys.TARGET_POSITION, player.position());
            module.getReviveEffect().apply(context);

            ModMessages.sendToTrackingAndSelf(
                    new ReviveEntityEventS2CPacket(player, module.getReviveDisplayItem()), player);
            return true;
        }

        return false;
    }

    public void rebuildConsumerFromProvider() {
        consumerEntries.clear();
        for (Entry entry : providerEntries) {
            consumerEntries.add(entry.copy());
        }
        consumerEntries.sort(ENTRY_ORDER);
    }

    public List<ResourceLocation> getHudPreview(int limit) {
        if (limit <= 0 || consumerEntries.isEmpty()) {
            return List.of();
        }

        List<ResourceLocation> icons = new ArrayList<>(Math.min(limit, consumerEntries.size()));
        for (Entry entry : consumerEntries) {
            ReviveModule module = resolveRegisteredModule(entry.id());
            if (module == null) {
                continue;
            }

            icons.add(module.getHudTexture());
            if (icons.size() >= limit) {
                break;
            }
        }
        return List.copyOf(icons);
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.put(PROVIDER_KEY, writeEntries(providerEntries));
        nbt.put(CONSUMER_KEY, writeEntries(consumerEntries));
        nbt.putLong(NEXT_ORDER_KEY, nextGrantOrder);
    }

    public void loadNBTData(CompoundTag nbt) {
        clear();
        readEntries(nbt.getList(PROVIDER_KEY, Tag.TAG_COMPOUND), providerEntries);
        readEntries(nbt.getList(CONSUMER_KEY, Tag.TAG_COMPOUND), consumerEntries);
        long loadedNextOrder = nbt.getLong(NEXT_ORDER_KEY);
        nextGrantOrder = Math.max(loadedNextOrder, highestGrantOrder(providerEntries, consumerEntries) + 1L);
    }

    private void mutateEntries(List<Entry> entries, ResourceLocation id, int count) {
        if (id == null || count == 0) {
            return;
        }

        ReviveModule module = resolveRegisteredModule(id);
        if (module == null) {
            return;
        }

        if (count > 0) {
            for (int i = 0; i < count; i++) {
                entries.add(new Entry(id, module.getPriority(), nextGrantOrder++));
            }
            entries.sort(ENTRY_ORDER);
            return;
        }

        int toRemove = -count;
        for (int i = entries.size() - 1; i >= 0 && toRemove > 0; i--) {
            if (entries.get(i).id().equals(id)) {
                entries.remove(i);
                toRemove--;
            }
        }
    }

    private static ListTag writeEntries(List<Entry> entries) {
        ListTag list = new ListTag();
        for (Entry entry : entries) {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", entry.id().toString());
            tag.putLong("grant_order", entry.grantOrder());
            list.add(tag);
        }
        return list;
    }

    private void readEntries(ListTag list, List<Entry> target) {
        for (Tag value : list) {
            if (!(value instanceof CompoundTag tag)) {
                continue;
            }

            try {
                ResourceLocation id = ResourceLocation.parse(tag.getString("id"));
                ReviveModule module = resolveRegisteredModule(id);
                if (module == null) {
                    continue;
                }

                long grantOrder = tag.contains("grant_order", Tag.TAG_LONG)
                        ? tag.getLong("grant_order")
                        : nextGrantOrder++;
                target.add(new Entry(id, module.getPriority(), grantOrder));
            } catch (Exception ignored) {
            }
        }
        target.sort(ENTRY_ORDER);
    }

    @Nullable
    private static ReviveModule resolveRegisteredModule(ResourceLocation id) {
        IForgeRegistry<ReviveModule> registry =
                RegistryManager.ACTIVE.getRegistry(ModReviveModule.REVIVE_MODULE_KEY);
        return registry == null ? null : registry.getValue(id);
    }

    private static long highestGrantOrder(List<Entry> providerEntries, List<Entry> consumerEntries) {
        long highest = -1L;
        for (Entry entry : providerEntries) {
            highest = Math.max(highest, entry.grantOrder());
        }
        for (Entry entry : consumerEntries) {
            highest = Math.max(highest, entry.grantOrder());
        }
        return highest;
    }

    private record Entry(ResourceLocation id, double priority, long grantOrder) {
        private Entry copy() {
            return new Entry(id, priority, grantOrder);
        }
    }
}
