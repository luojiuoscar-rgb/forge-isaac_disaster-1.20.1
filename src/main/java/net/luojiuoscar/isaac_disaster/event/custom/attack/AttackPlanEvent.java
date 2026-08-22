package net.luojiuoscar.isaac_disaster.event.custom.attack;

import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPipelineMode;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPlan;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackRequest;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Event fired while the attack plan is still mutable. */
public class AttackPlanEvent extends Event {
    private final @NotNull AttackRequest request;
    private final @NotNull AttackPlan plan;

    public AttackPlanEvent(@NotNull AttackRequest request, @NotNull AttackPlan plan) {
        this.request = request;
        this.plan = plan;
    }

    public @NotNull AttackRequest getRequest() {
        return request;
    }

    public @NotNull AttackPlan getPlan() {
        return plan;
    }

    public @NotNull AttackType getAttackType() {
        return request.getAttackType();
    }

    public @NotNull AttackOrigin getOrigin() {
        return request.getOrigin();
    }

    public @NotNull AttackPipelineMode getPipelineMode() {
        return request.getPipelineMode();
    }

    public @Nullable LivingEntity getOwner() {
        return request.getOwner();
    }

    public @Nullable ServerPlayer getPlayer() {
        return request.getPlayer();
    }

    public @NotNull List<AttackContext> getBaseContexts() {
        return plan.getBaseContexts();
    }

    public @NotNull List<AttackContext> getOriginalBaseContexts() {
        return plan.getOriginalBaseContexts();
    }

    public @NotNull List<AttackContext> getExtraContexts() {
        return plan.getExtraContexts();
    }

    public void replaceBaseContexts(@NotNull List<AttackContext> contexts) {
        plan.replaceBaseContexts(contexts);
    }

    public void appendExtraContexts(@NotNull List<AttackContext> contexts) {
        plan.appendExtraContexts(contexts);
    }
}
