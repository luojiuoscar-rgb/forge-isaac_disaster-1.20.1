package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackContextPrepareEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackPlanEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.BeforePerformAttackEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class AttackPipeline {
    private AttackPipeline() {
    }

    /** Executes the request through the default Forge-backed pipeline. */
    public static boolean executeRequest(@NotNull AttackRequest request) {
        Objects.requireNonNull(request, "request");
        return switch (request.getPipelineMode()) {
            case FULL, GROUP_AND_BULLET -> executeGeneratedAttack(request);
            case BULLET_ONLY -> executeProvidedContexts(request);
            case RAW -> executeRawAttack(request);
        };
    }

    /** Generates contexts from the attack type after the FULL-mode cancellation check. */
    private static boolean executeGeneratedAttack(@NotNull AttackRequest request) {
        if (request.getPipelineMode() == AttackPipelineMode.FULL) {
            LivingEntity owner = Objects.requireNonNull(request.getOwner(), "FULL attacks require an owner");
            if (MinecraftForge.EVENT_BUS.post(new BeforePerformAttackEvent(owner, request.getAttackType()))) {
                return false;
            }
        }

        ServerPlayer player = Objects.requireNonNull(
                request.getPlayer(), "generated attacks require a server player");
        int bulletCount = request.getAttackType().getBulletCount(player);
        List<AttackContext> baseContexts = request.getAttackType().getAttackContexts(player, bulletCount);
        return executeAttackPlan(request, baseContexts);
    }

    /** Runs the one-time attack-plan phase and then freezes its final contexts. */
    private static boolean executeAttackPlan(@NotNull AttackRequest request,
                                             @NotNull List<AttackContext> baseContexts) {
        AttackPlan plan = new AttackPlan(request, baseContexts);
        AttackPlanEvent planEvent = new AttackPlanEvent(request, plan);
        MinecraftForge.EVENT_BUS.post(planEvent);
        return executePreparedContexts(request, plan, plan.freezeContexts());
    }

    /** Creates a fixed plan wrapper for caller-provided contexts and prepares each context. */
    private static boolean executeProvidedContexts(@NotNull AttackRequest request) {
        AttackPlan plan = new AttackPlan(request, request.getProvidedContexts());
        return executePreparedContexts(request, plan, plan.freezeContexts());
    }

    /** Runs the per-context stage, omitting only contexts whose prepare event was cancelled. */
    private static boolean executePreparedContexts(@NotNull AttackRequest request, @NotNull AttackPlan plan,
                                                   @NotNull List<AttackContext> contexts) {
        List<AttackContext> preparedContexts = new ArrayList<>();
        for (int i = 0; i < contexts.size(); i++) {
            AttackContext attackContext = contexts.get(i);
            AttackContextPrepareEvent prepareEvent =
                    new AttackContextPrepareEvent(request, plan, attackContext, i);
            MinecraftForge.EVENT_BUS.post(prepareEvent);
            if (!prepareEvent.isCanceled()) {
                preparedContexts.add(attackContext);
            }
        }

        request.getAttackType().performAttack(preparedContexts);
        LivingEntity owner = request.getOwner();
        if (request.shouldPlaySound() && owner != null) {
            request.getAttackType().makeSound(owner);
        }
        return true;
    }

    /** Executes the provided contexts directly without publishing any pipeline events. */
    private static boolean executeRawAttack(@NotNull AttackRequest request) {
        request.getAttackType().performAttack(request.getProvidedContexts());
        LivingEntity owner = request.getOwner();
        if (request.shouldPlaySound() && owner != null) {
            request.getAttackType().makeSound(owner);
        }
        return true;
    }

}
