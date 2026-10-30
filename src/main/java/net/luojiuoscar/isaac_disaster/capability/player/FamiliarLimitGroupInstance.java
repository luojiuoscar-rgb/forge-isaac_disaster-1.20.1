package net.luojiuoscar.isaac_disaster.capability.player;

import net.luojiuoscar.isaac_disaster.registries.familiar.FamiliarEntityType;
import net.luojiuoscar.isaac_disaster.registries.familiar.FamiliarLimitGroup;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** One player's allocations for a registered group; unrelated to entity spawn success. */
final class FamiliarLimitGroupInstance {
    private final FamiliarLimitGroup definition;
    private final Map<ResourceLocation, Integer> quotas = new HashMap<>();
    private int used;

    FamiliarLimitGroupInstance(FamiliarLimitGroup definition) {
        this.definition = definition;
    }

    int getQuota(ResourceLocation type) { return quotas.getOrDefault(type, 0); }

    FamiliarLimitGroupInstance copy() {
        FamiliarLimitGroupInstance result = new FamiliarLimitGroupInstance(definition);
        result.quotas.putAll(quotas);
        result.used = used;
        return result;
    }

    void clear() {
        quotas.clear();
        used = 0;
    }

    /** Preserve existing grants, then fill waiting requests in priority and registration order. */
    void reconcile(Collection<FamiliarEntry> entries, Function<ResourceLocation, FamiliarEntityType> resolver) {
        List<Member> members = new ArrayList<>();
        for (FamiliarEntry entry : entries) {
            FamiliarEntityType type = resolver.apply(entry.getType());
            if (type != null && type.getGroup() == definition) {
                members.add(new Member(entry, type));
                setQuota(entry.getType(), Math.min(getQuota(entry.getType()), Math.min(entry.getCount(), type.getLimit())));
            }
        }
        // Stable sorting preserves registration order among equally prioritized waiting types.
        List<Member> waiting = new ArrayList<>(members);
        waiting.sort(Comparator.comparingInt((Member member) -> member.type.getPriority()).reversed());
        for (Member incoming : waiting) {
            ResourceLocation id = incoming.entry.getType();
            int needed = Math.min(incoming.entry.getCount(), incoming.type.getLimit()) - getQuota(id);
            if (needed <= 0) continue;
            int free = Math.min(needed, definition.getLimit() - used);
            setQuota(id, getQuota(id) + free);
            needed -= free;
            while (needed > 0) {
                Member victim = lowestPriorityVictim(members, incoming.type.getPriority());
                if (victim == null) break;
                ResourceLocation victimId = victim.entry.getType();
                int displaced = Math.min(needed, getQuota(victimId));
                setQuota(victimId, getQuota(victimId) - displaced);
                setQuota(id, getQuota(id) + displaced);
                needed -= displaced;
            }
        }
    }

    private Member lowestPriorityVictim(List<Member> members, int incomingPriority) {
        Member result = null;
        for (Member member : members) {
            if (getQuota(member.entry.getType()) > 0 && member.type.getPriority() < incomingPriority
                    && (result == null || member.type.getPriority() <= result.type.getPriority())) result = member;
        }
        return result;
    }

    private void setQuota(ResourceLocation type, int count) {
        used = used - getQuota(type) + count;
        if (count == 0) quotas.remove(type);
        else quotas.put(type, count);
    }

    private record Member(FamiliarEntry entry, FamiliarEntityType type) { }
}
