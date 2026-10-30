package net.luojiuoscar.isaac_disaster.event.custom.attack;

import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackRequest;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;
import java.util.Objects;

/** One notification per request, after its first successful launch (possibly on a later tick). */
public final class AttackFiredEvent extends Event {
    private final AttackRequest request;
    private final List<AttackContext> preparedContexts;
    private final AttackContext firstFiredContext;
    private final double chargeFraction;

    public AttackFiredEvent(AttackRequest request, List<AttackContext> preparedContexts,
                            AttackContext firstFiredContext, double chargeFraction) {
        this.request = Objects.requireNonNull(request, "request");
        this.preparedContexts = List.copyOf(preparedContexts);
        this.firstFiredContext = Objects.requireNonNull(firstFiredContext, "firstFiredContext");
        this.chargeFraction = Double.isFinite(chargeFraction) ? Math.max(0, Math.min(1, chargeFraction)) : 0;
    }

    public AttackRequest getRequest() { return request; }
    public AttackType getAttackType() { return request.getAttackType(); }
    public AttackOrigin getOrigin() { return request.getOrigin(); }
    public ResourceLocation getSourceId() { return request.getSourceId(); }
    public LivingEntity getOwner() { return request.getOwner(); }
    public List<AttackContext> getPreparedContexts() { return preparedContexts; }
    public AttackContext getFirstFiredContext() { return firstFiredContext; }
    public double getChargeFraction() { return chargeFraction; }
}
