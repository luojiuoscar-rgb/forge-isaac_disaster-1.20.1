package net.luojiuoscar.isaac_disaster.system.stat_multiplier;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/** Persistent entry facts and reconciliation of only the modifiers owned by those entries. */
public final class MultiplierEntries {
    private record AppliedModifier(ResourceLocation attributeId, ResourceLocation entryId) {
    }

    public record EntryStack(MultiplierEntry entry, int stacks) {
        public EntryStack {
            if (entry == null) {
                IsaacDisaster.LOGGER.warn("Multiplier entry stack has no entry");
                stacks = 0;
            }
            if (stacks < 0) {
                IsaacDisaster.LOGGER.warn("Negative multiplier stack count {}; using 0", stacks);
                stacks = 0;
            }
        }
    }

    private final Map<MultiplierEntry.Key, EntryStack> entries = new LinkedHashMap<>();
    private final Map<UUID, AppliedModifier> applied = new HashMap<>();

    public Map<MultiplierEntry.Key, EntryStack> snapshot() {
        return Map.copyOf(entries);
    }

    public void add(MultiplierEntry entry, int count) {
        if (entry == null) {
            IsaacDisaster.LOGGER.warn("Ignoring null multiplier entry");
            return;
        }
        if (count <= 0) {
            IsaacDisaster.LOGGER.warn("Ignoring multiplier entry {} with nonpositive count {}", entry.id(), count);
            return;
        }
        if (entry.id() == null || entry.uuid() == null || entry.sourceId() == null || entry.attributeId() == null
                || (entry.operation() != AttributeModifier.Operation.MULTIPLY_BASE
                && entry.operation() != AttributeModifier.Operation.MULTIPLY_TOTAL)
                || !Double.isFinite(entry.amount()) || entry.amount() <= -1.0D) {
            IsaacDisaster.LOGGER.warn("Ignoring invalid multiplier entry {}", entry);
            return;
        }
        if (ForgeRegistries.ATTRIBUTES.getValue(entry.attributeId()) == null) {
            IsaacDisaster.LOGGER.warn("Ignoring multiplier entry {} with unknown attribute {}",
                    entry.id(), entry.attributeId());
            return;
        }
        EntryStack current = entries.get(entry.key());
        if (current != null && !current.entry().equals(entry)) {
            IsaacDisaster.LOGGER.warn("Ignoring conflicting multiplier entry {}", entry.id());
            return;
        }
        if (entries.values().stream().anyMatch(candidate -> {
            MultiplierEntry existing = candidate.entry();
            return existing.id().equals(entry.id())
                    ? !existing.uuid().equals(entry.uuid()) || !existing.attributeId().equals(entry.attributeId())
                            || existing.operation() != entry.operation()
                    : existing.uuid().equals(entry.uuid());
        })) {
            IsaacDisaster.LOGGER.warn("Ignoring incompatible multiplier group definition {}", entry.id());
            return;
        }
        long stacks = (long) (current == null ? 0 : current.stacks()) + count;
        if (stacks > Integer.MAX_VALUE) {
            IsaacDisaster.LOGGER.warn("Ignoring multiplier entry {} because stack count would overflow", entry.id());
            return;
        }
        entries.put(entry.key(), new EntryStack(entry, (int) stacks));
    }

    public void remove(MultiplierEntry.Key key, int count) {
        if (key == null || count <= 0) {
            IsaacDisaster.LOGGER.warn("Ignoring multiplier removal with key {} and count {}", key, count);
            return;
        }
        EntryStack current = entries.get(key);
        if (current == null) {
            IsaacDisaster.LOGGER.warn("Ignoring removal of missing multiplier source {}", key);
            return;
        }
        if (current.stacks() < count) {
            IsaacDisaster.LOGGER.warn("Multiplier entry {} removal underflow: held {}, requested {}; using 0",
                    key, current.stacks(), count);
        }
        int remaining = Math.max(0, current.stacks() - count);
        if (remaining == 0) entries.remove(key);
        else entries.put(key, new EntryStack(current.entry(), remaining));
    }

    public void reconcile(ServerPlayer player, boolean allowStacking,
                          MultiplierRules rules) {
        if (player == null) {
            IsaacDisaster.LOGGER.warn("Cannot reconcile multiplier entries without a player");
            return;
        }
        reconcile(attributeId -> {
            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(attributeId);
            return attribute == null ? null : player.getAttribute(attribute);
        }, allowStacking, rules);
    }

    void reconcile(Function<ResourceLocation, AttributeInstance> attributes, boolean allowStacking,
                   MultiplierRules rules) {
        if (attributes == null || rules == null) {
            IsaacDisaster.LOGGER.warn("Cannot reconcile multiplier entries without attributes or rules");
            return;
        }
        for (Map.Entry<UUID, AppliedModifier> candidate : applied.entrySet()) {
            removeOwned(attributes.apply(candidate.getValue().attributeId()), candidate.getKey(),
                    candidate.getValue().entryId());
        }
        applied.clear();
        Map<ResourceLocation, List<EntryStack>> groups = new LinkedHashMap<>();
        for (EntryStack stack : entries.values()) {
            groups.computeIfAbsent(stack.entry().id(), ignored -> new ArrayList<>()).add(stack);
        }
        for (List<EntryStack> group : groups.values()) {
            MultiplierEntry entry = group.get(0).entry();
            AttributeInstance instance = attributes.apply(entry.attributeId());
            if (instance == null) {
                IsaacDisaster.LOGGER.warn("Player has no attribute for multiplier entry {}: {}", entry.id(), entry.attributeId());
                continue;
            }
            if (!removeOwned(instance, entry.uuid(), entry.id())) continue;
            if (rules.suppressed(entry.id())) continue;
            double amount = effectiveAmount(group, allowStacking);
            if (!Double.isFinite(amount)) {
                IsaacDisaster.LOGGER.warn("Multiplier entry {} has a non-finite effective amount", entry.id());
                continue;
            }
            instance.addPermanentModifier(new AttributeModifier(entry.uuid(), entry.id().toString(),
                    amount, entry.operation()));
            applied.put(entry.uuid(), new AppliedModifier(entry.attributeId(), entry.id()));
        }
    }

    /** Without stacking, the strongest held source wins; stacking combines every source and copy. */
    static double effectiveAmount(List<EntryStack> group, boolean allowStacking) {
        if (!allowStacking) {
            return group.stream().max(Comparator
                    .comparingDouble((EntryStack stack) -> Math.abs(stack.entry().amount()))
                    .thenComparing(stack -> stack.entry().sourceId().toString()))
                    .orElseThrow().entry().amount();
        }
        if (group.get(0).entry().operation() == AttributeModifier.Operation.MULTIPLY_BASE) {
            return group.stream().mapToDouble(stack -> stack.entry().amount() * stack.stacks()).sum();
        }
        double factor = 1.0D;
        for (EntryStack stack : group) {
            factor *= Math.pow(1.0D + stack.entry().amount(), stack.stacks());
        }
        return factor - 1.0D;
    }

    private static boolean removeOwned(AttributeInstance instance, UUID uuid, ResourceLocation entryId) {
        if (instance == null) return true;
        AttributeModifier existing = instance.getModifier(uuid);
        if (existing == null) return true;
        if (!entryId.toString().equals(existing.getName())) {
            IsaacDisaster.LOGGER.warn("Multiplier entry {} cannot use UUID {} owned by another modifier", entryId, uuid);
            return false;
        }
        instance.removeModifier(uuid);
        return true;
    }

    public void copyFrom(MultiplierEntries source) {
        if (source == null) {
            IsaacDisaster.LOGGER.warn("Cannot copy multiplier entries from a missing source");
            return;
        }
        entries.clear();
        entries.putAll(source.entries);
        applied.clear();
    }

    public ListTag save() {
        ListTag result = new ListTag();
        for (EntryStack stack : entries.values()) {
            MultiplierEntry entry = stack.entry();
            CompoundTag tag = new CompoundTag();
            tag.putString("id", entry.id().toString());
            tag.putUUID("uuid", entry.uuid());
            tag.putString("source", entry.sourceId().toString());
            tag.putString("attribute", entry.attributeId().toString());
            tag.putDouble("amount", entry.amount());
            tag.putString("operation", entry.operation().name());
            tag.putInt("stacks", stack.stacks());
            result.add(tag);
        }
        return result;
    }

    public void load(ListTag list) {
        entries.clear();
        applied.clear();
        if (list == null) {
            IsaacDisaster.LOGGER.warn("Missing multiplier entry data; using an empty collection");
            return;
        }
        for (Tag value : list) {
            if (!(value instanceof CompoundTag tag)) {
                IsaacDisaster.LOGGER.warn("Ignored non-compound multiplier entry in player data");
                continue;
            }
            try {
                if (!tag.contains("id", Tag.TAG_STRING) || !tag.contains("source", Tag.TAG_STRING)
                        || !tag.contains("attribute", Tag.TAG_STRING) || !tag.contains("amount", Tag.TAG_DOUBLE)
                        || !tag.contains("operation", Tag.TAG_STRING) || !tag.contains("stacks", Tag.TAG_INT)) {
                    IsaacDisaster.LOGGER.warn("Ignored multiplier entry with missing fields in player data");
                    continue;
                }
                ResourceLocation id = ResourceLocation.parse(tag.getString("id"));
                if (!tag.hasUUID("uuid")) {
                    IsaacDisaster.LOGGER.warn("Ignored multiplier entry {} with invalid UUID", id);
                    continue;
                }
                UUID uuid = tag.getUUID("uuid");
                MultiplierEntry entry = new MultiplierEntry(id, uuid,
                        ResourceLocation.parse(tag.getString("source")),
                        ResourceLocation.parse(tag.getString("attribute")), tag.getDouble("amount"),
                        AttributeModifier.Operation.valueOf(tag.getString("operation")));
                add(entry, tag.getInt("stacks"));
            } catch (RuntimeException error) {
                IsaacDisaster.LOGGER.warn("Ignored invalid multiplier entry in player data: {}", error.getMessage());
            }
        }
    }
}
