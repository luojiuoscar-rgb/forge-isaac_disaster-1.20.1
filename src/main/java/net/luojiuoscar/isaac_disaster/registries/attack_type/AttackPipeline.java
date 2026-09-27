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
            case FULL, PLAN_PREPARE_AND_EXECUTE -> executeGeneratedAttack(request);
            case PREPARE_AND_EXECUTE -> executePrepareAndExecute(request);
            case EXECUTE_ONLY -> executeExecuteOnly(request);
        };
    }

    /** Generates contexts, optionally after the FULL-mode attack-level cancellation check. */
    private static boolean executeGeneratedAttack(@NotNull AttackRequest request) {
        if (request.getPipelineMode() == AttackPipelineMode.FULL) {
            if (MinecraftForge.EVENT_BUS.post(new BeforePerformAttackEvent(
                    request.getOwner(), request.getAttackType()))) {
                return false;
            }
        }

        ServerPlayer player = Objects.requireNonNull(
                request.getPlayer(), "generated attacks require a server player");
        int bulletCount = request.getAttackType().getBulletCount(player);
        List<AttackContext> baseContexts = request.getAttackType().getAttackContexts(player, bulletCount)
                .stream()
                .filter(Objects::nonNull)
                .toList();
        return executeAttackPlan(request, baseContexts);
    }

    /** Runs the one-time attack-plan phase and finalizes its final context sequence. */
    private static boolean executeAttackPlan(@NotNull AttackRequest request,
                                             @NotNull List<AttackContext> baseContexts) {
        AttackPlan plan = new AttackPlan(request, baseContexts);
        AttackPlanEvent planEvent = new AttackPlanEvent(request, plan);
        MinecraftForge.EVENT_BUS.post(planEvent);
        return executePreparedContexts(request, plan, plan.finalizeContexts());
    }

    /** Creates a fixed plan wrapper for caller-provided contexts and prepares each context. */
    private static boolean executePrepareAndExecute(@NotNull AttackRequest request) {
        AttackPlan plan = new AttackPlan(request, request.getProvidedContexts());
        return executePreparedContexts(request, plan, plan.finalizeContexts());
    }

    /** Runs the per-context stage, omitting only contexts whose prepare event was cancelled. */
    private static boolean executePreparedContexts(@NotNull AttackRequest request, @NotNull AttackPlan plan,
                                                   @NotNull List<AttackContext> contexts) {
        List<AttackContext> preparedContexts = new ArrayList<>();
        for (int i = 0; i < contexts.size(); i++) {
            AttackContext attackContext = contexts.get(i).bindAttackTypeOrCopy(request.getAttackType());
            AttackContextPrepareEvent prepareEvent =
                    new AttackContextPrepareEvent(request, plan, attackContext, i);
            MinecraftForge.EVENT_BUS.post(prepareEvent);
            if (!prepareEvent.isCanceled()) {
                preparedContexts.add(attackContext);
            }
        }

        preparedContexts.forEach(AttackContext::freeze);
        request.getAttackType().performAttack(preparedContexts);
        if (request.shouldPlaySound()) {
            request.getAttackType().makeSound(request.getOwner());
        }
        return true;
    }

    /** Executes already-prepared contexts directly without publishing pipeline events. */
    private static boolean executeExecuteOnly(@NotNull AttackRequest request) {
        List<AttackContext> contexts = request.getProvidedContexts().stream()
                .map(context -> context.bindAttackTypeOrCopy(request.getAttackType())).toList();
        contexts.forEach(AttackContext::freeze);
        request.getAttackType().performAttack(contexts);
        if (request.shouldPlaySound()) {
            request.getAttackType().makeSound(request.getOwner());
        }
        return true;
    }

}
