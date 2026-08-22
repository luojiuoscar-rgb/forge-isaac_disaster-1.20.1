package net.luojiuoscar.isaac_disaster.event.custom.attack;

import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPipelineMode;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPlan;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackRequest;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Event fired for each final attack context before it is executed. */
@Cancelable
public class AttackContextPrepareEvent extends Event {
    private final @NotNull AttackRequest request;
    private final @NotNull AttackPlan plan;
    private final @NotNull AttackContext attackContext;
    private final int contextIndex;

    public AttackContextPrepareEvent(@NotNull AttackRequest request, @NotNull AttackPlan plan,
                                     @NotNull AttackContext attackContext, int contextIndex) {
        this.request = request;
        this.plan = plan;
        this.attackContext = attackContext;
        this.contextIndex = contextIndex;
    }

    public @NotNull AttackRequest getRequest() {
        return request;
    }

    public @NotNull AttackPlan getPlan() {
        return plan;
    }

    public @NotNull AttackContext getAttackContext() {
        return attackContext;
    }

    public int getContextIndex() {
        return contextIndex;
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
}
