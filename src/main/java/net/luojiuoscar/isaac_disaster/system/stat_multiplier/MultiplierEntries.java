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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/** Persistent entry facts and reconciliation of only the modifiers owned by those entries. */
public final class MultiplierEntries {
    private record AppliedModifier(ResourceLocation attributeId, ResourceLocation entryId) {
    }

    private record LegacyEntry(ResourceLocation attributeId, int stacks) {
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

    private final Map<ResourceLocation, EntryStack> entries = new LinkedHashMap<>();
    private final Map<UUID, AppliedModifier> applied = new HashMap<>();
    private final Map<ResourceLocation, LegacyEntry> legacyEntries = new HashMap<>();

    public Map<ResourceLocation, EntryStack> snapshot() {
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
        EntryStack current = entries.get(entry.id());
        if (current != null && !current.entry().equals(entry)) {
            IsaacDisaster.LOGGER.warn("Ignoring conflicting multiplier entry {}", entry.id());
            return;
        }
        if (current == null && entries.values().stream().anyMatch(candidate ->
                candidate.entry().uuid().equals(entry.uuid()))) {
            IsaacDisaster.LOGGER.warn("Ignoring multiplier entry {} with duplicate UUID {}", entry.id(), entry.uuid());
            return;
        }
        long stacks = (long) (current == null ? 0 : current.stacks()) + count;
        if (stacks > Integer.MAX_VALUE) {
            IsaacDisaster.LOGGER.warn("Ignoring multiplier entry {} because stack count would overflow", entry.id());
            return;
        }
        entries.put(entry.id(), new EntryStack(entry, (int) stacks));
    }

    public void remove(ResourceLocation id, int count) {
        if (id == null || count <= 0) {
            IsaacDisaster.LOGGER.warn("Ignoring multiplier removal with id {} and count {}", id, count);
            return;
        }
        EntryStack current = entries.get(id);
        if (current == null) {
            IsaacDisaster.LOGGER.warn("Ignoring removal of missing multiplier entry {}", id);
            return;
        }
        if (current.stacks() < count) {
            IsaacDisaster.LOGGER.warn("Multiplier entry {} removal underflow: held {}, requested {}; using 0",
                    id, current.stacks(), count);
        }
        int remaining = Math.max(0, current.stacks() - count);
        if (remaining == 0) entries.remove(id);
        else entries.put(id, new EntryStack(current.entry(), remaining));
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
        for (Map.Entry<ResourceLocation, LegacyEntry> legacy : legacyEntries.entrySet()) {
            AttributeInstance instance = attributes.apply(legacy.getValue().attributeId());
            if (instance == null) continue;
            // The old format applied contiguous copy indices, at most one per current modifier.
            int candidates = Math.min(legacy.getValue().stacks(), instance.getModifiers().size());
            for (int i = 0; i < candidates; i++) {
                removeOwned(instance, MultiplierEntry.legacyModifierId(legacy.getKey(), i), legacy.getKey());
            }
        }
        legacyEntries.clear();
        for (EntryStack stack : entries.values()) {
            MultiplierEntry entry = stack.entry();
            AttributeInstance instance = attributes.apply(entry.attributeId());
            if (instance == null) {
                IsaacDisaster.LOGGER.warn("Player has no attribute for multiplier entry {}: {}", entry.id(), entry.attributeId());
                continue;
            }
            if (!removeOwned(instance, entry.uuid(), entry.id())) continue;
            if (rules.suppressed(entry.id())) continue;
            double amount = effectiveAmount(stack, allowStacking);
            if (!Double.isFinite(amount)) {
                IsaacDisaster.LOGGER.warn("Multiplier entry {} has a non-finite effective amount", entry.id());
                continue;
            }
            instance.addPermanentModifier(new AttributeModifier(entry.uuid(), entry.id().toString(),
                    amount, entry.operation()));
            applied.put(entry.uuid(), new AppliedModifier(entry.attributeId(), entry.id()));
        }
    }

    static double effectiveAmount(EntryStack stack, boolean allowStacking) {
        if (!allowStacking) return stack.entry().amount();
        double amount = stack.entry().amount();
        return stack.entry().operation() == AttributeModifier.Operation.MULTIPLY_BASE
                ? amount * stack.stacks() : Math.pow(1.0D + amount, stack.stacks()) - 1.0D;
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
        legacyEntries.clear();
        legacyEntries.putAll(source.legacyEntries);
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
        legacyEntries.clear();
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
                boolean legacy = !tag.contains("uuid");
                if (!legacy && !tag.hasUUID("uuid")) {
                    IsaacDisaster.LOGGER.warn("Ignored multiplier entry {} with invalid UUID", id);
                    continue;
                }
                UUID uuid = legacy ? MultiplierEntry.legacyModifierId(id, 0) : tag.getUUID("uuid");
                MultiplierEntry entry = new MultiplierEntry(id, uuid,
                        ResourceLocation.parse(tag.getString("source")),
                        ResourceLocation.parse(tag.getString("attribute")), tag.getDouble("amount"),
                        AttributeModifier.Operation.valueOf(tag.getString("operation")));
                add(entry, tag.getInt("stacks"));
                if (legacy && entries.containsKey(id) && entries.get(id).entry().equals(entry)) {
                    legacyEntries.put(id, new LegacyEntry(entry.attributeId(), entries.get(id).stacks()));
                }
            } catch (RuntimeException error) {
                IsaacDisaster.LOGGER.warn("Ignored invalid multiplier entry in player data: {}", error.getMessage());
            }
        }
    }
}
