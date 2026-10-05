package net.luojiuoscar.isaac_disaster.system.stat_multiplier;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

/** Player-owned suppression rules. A held rule suppresses its target entry. */
public final class MultiplierRules {
    public record RuleEntry(ResourceLocation id, ResourceLocation sourceId, ResourceLocation targetEntryId) {
    }

    public record RuleStack(RuleEntry rule, int stacks) {
        public RuleStack {
            if (rule == null) {
                IsaacDisaster.LOGGER.warn("Multiplier rule stack has no rule");
                stacks = 0;
            }
            if (stacks < 0) {
                IsaacDisaster.LOGGER.warn("Negative multiplier rule stack count {}; using 0", stacks);
                stacks = 0;
            }
        }
    }

    private final Map<ResourceLocation, RuleStack> rules = new LinkedHashMap<>();

    public Map<ResourceLocation, RuleStack> snapshot() {
        return Map.copyOf(rules);
    }

    public void add(RuleEntry rule, int count) {
        if (rule == null || rule.id() == null || rule.sourceId() == null || rule.targetEntryId() == null
                || count <= 0) {
            IsaacDisaster.LOGGER.warn("Ignoring invalid multiplier rule {} with count {}", rule, count);
            return;
        }
        RuleStack current = rules.get(rule.id());
        if (current != null && !current.rule().equals(rule)) {
            IsaacDisaster.LOGGER.warn("Ignoring conflicting multiplier rule {}", rule.id());
            return;
        }
        long stacks = (long) (current == null ? 0 : current.stacks()) + count;
        if (stacks > Integer.MAX_VALUE) {
            IsaacDisaster.LOGGER.warn("Ignoring multiplier rule {} because stack count would overflow", rule.id());
            return;
        }
        rules.put(rule.id(), new RuleStack(rule, (int) stacks));
    }

    public void remove(ResourceLocation id, int count) {
        if (id == null || count <= 0) {
            IsaacDisaster.LOGGER.warn("Ignoring multiplier rule removal with id {} and count {}", id, count);
            return;
        }
        RuleStack current = rules.get(id);
        if (current == null) {
            IsaacDisaster.LOGGER.warn("Ignoring removal of missing multiplier rule {}", id);
            return;
        }
        if (current.stacks() < count) {
            IsaacDisaster.LOGGER.warn("Multiplier rule {} removal underflow: held {}, requested {}; using 0",
                    id, current.stacks(), count);
        }
        int remaining = Math.max(0, current.stacks() - count);
        if (remaining == 0) rules.remove(id);
        else rules.put(id, new RuleStack(current.rule(), remaining));
    }

    public boolean suppressed(ResourceLocation entryId) {
        if (entryId == null) {
            IsaacDisaster.LOGGER.warn("Cannot evaluate multiplier suppression without an entry ID");
            return false;
        }
        return rules.values().stream().anyMatch(stack -> stack.stacks() > 0
                && stack.rule().targetEntryId().equals(entryId));
    }

    public void copyFrom(MultiplierRules source) {
        if (source == null) {
            IsaacDisaster.LOGGER.warn("Cannot copy multiplier rules from a missing source");
            return;
        }
        rules.clear();
        rules.putAll(source.rules);
    }

    public ListTag save() {
        ListTag result = new ListTag();
        for (RuleStack stack : rules.values()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", stack.rule().id().toString());
            tag.putString("source", stack.rule().sourceId().toString());
            tag.putString("target", stack.rule().targetEntryId().toString());
            tag.putInt("stacks", stack.stacks());
            result.add(tag);
        }
        return result;
    }

    public void load(ListTag list) {
        rules.clear();
        if (list == null) {
            IsaacDisaster.LOGGER.warn("Missing multiplier rule data; using an empty collection");
            return;
        }
        for (Tag value : list) {
            if (!(value instanceof CompoundTag tag)) {
                IsaacDisaster.LOGGER.warn("Ignored non-compound multiplier rule in player data");
                continue;
            }
            try {
                if (!tag.contains("id", Tag.TAG_STRING) || !tag.contains("source", Tag.TAG_STRING)
                        || !tag.contains("target", Tag.TAG_STRING) || !tag.contains("stacks", Tag.TAG_INT)) {
                    IsaacDisaster.LOGGER.warn("Ignored multiplier rule with missing fields in player data");
                    continue;
                }
                add(new RuleEntry(ResourceLocation.parse(tag.getString("id")),
                        ResourceLocation.parse(tag.getString("source")),
                        ResourceLocation.parse(tag.getString("target"))), tag.getInt("stacks"));
            } catch (RuntimeException error) {
                IsaacDisaster.LOGGER.warn("Ignored invalid multiplier rule in player data: {}", error.getMessage());
            }
        }
    }
}
