package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPlan;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackRequest;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Owns a single attack's confirmed entity queue independently of its prepared contexts. */
public final class HeldMomsKnifeAttackPlan extends AttackPlan {
    private final Set<ResourceLocation> sources = new LinkedHashSet<>();
    private List<HeldMomsKnifeGroup.QueuedKnife> queue;
    private List<AttackContext> selectedContexts;

    public HeldMomsKnifeAttackPlan(AttackRequest request, List<AttackContext> contexts) {
        super(request, contexts);
    }

    /** Admits an entire contributed source group before the firing queue is confirmed. */
    public void includeSource(ResourceLocation sourceId) {
        ensureNotFrozen();
        sources.add(Objects.requireNonNull(sourceId, "sourceId"));
    }

    @Override
    public List<AttackContext> finalizeContexts() {
        if (selectedContexts == null) {
            List<AttackContext> contexts = super.finalizeContexts();
            ServerPlayer player = getRequest().getPlayer();
            queue = player == null ? List.of() : HeldMomsKnifeGroup.select(player, contexts, sources);
            selectedContexts = queue.stream().map(HeldMomsKnifeGroup.QueuedKnife::context).toList();
        }
        return selectedContexts;
    }

    public List<HeldMomsKnifeGroup.QueuedKnife> getQueue() {
        if (queue == null) throw new IllegalStateException("held knife plan has not been finalized");
        return queue;
    }
}
