package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public final class AttackRequest {
    private final @NotNull AttackType attackType;
    private final @NotNull LivingEntity owner;
    private final @NotNull AttackOrigin origin;
    private final @NotNull AttackPipelineMode pipelineMode;
    private final @NotNull List<AttackContext> providedContexts;
    /** Whether the pipeline should invoke {@link AttackType#makeSound(LivingEntity)} after execution. */
    private final boolean playSound;

    AttackRequest(@NotNull AttackType attackType, @NotNull LivingEntity owner,
                  @NotNull AttackOrigin origin, @NotNull AttackPipelineMode pipelineMode,
                  @NotNull List<AttackContext> providedContexts, boolean playSound) {
        this.attackType = Objects.requireNonNull(attackType, "attackType");
        this.owner = Objects.requireNonNull(owner, "owner");
        this.origin = Objects.requireNonNull(origin, "origin");
        this.pipelineMode = Objects.requireNonNull(pipelineMode, "pipelineMode");
        this.providedContexts = List.copyOf(Objects.requireNonNull(providedContexts, "providedContexts"));
        this.playSound = playSound;
    }

    /**
     * Creates a request that generates base contexts from the attack type.
     *
     * @param player the player initiating the attack
     * @param attackType the attack type to execute
     * @param origin where the attack came from
     * @param pipelineMode must be {@link AttackPipelineMode#FULL} or {@link AttackPipelineMode#PLAN_PREPARE_AND_EXECUTE}
     * @param playSound whether the pipeline should play the sound after execution; use {@code false} when the caller owns timing
     * @return a generated attack request
     */
    public static AttackRequest generated(@NotNull ServerPlayer player, @NotNull AttackType attackType,
                                          @NotNull AttackOrigin origin, @NotNull AttackPipelineMode pipelineMode,
                                          boolean playSound) {
        requireGeneratedMode(Objects.requireNonNull(pipelineMode, "pipelineMode"));
        Objects.requireNonNull(player, "player");
        return new AttackRequest(attackType, player,
                origin, pipelineMode, List.of(), playSound);
    }

    /**
     * Creates a request that starts from contexts already provided by the caller.
     *
     * @param owner the non-null owner of the attack
     * @param attackType the attack type to execute
     * @param origin where the attack came from
     * @param pipelineMode must be {@link AttackPipelineMode#PREPARE_AND_EXECUTE} or {@link AttackPipelineMode#EXECUTE_ONLY}
     * @param providedContexts the contexts to execute
     * @param playSound whether the pipeline should play the sound after execution; use {@code false} when the caller owns timing
     * @return a request backed by caller-provided contexts
     */
    public static AttackRequest withContexts(@NotNull LivingEntity owner, @NotNull AttackType attackType,
                                             @NotNull AttackOrigin origin, @NotNull AttackPipelineMode pipelineMode,
                                             @NotNull List<AttackContext> providedContexts, boolean playSound) {
        requireProvidedMode(Objects.requireNonNull(pipelineMode, "pipelineMode"));
        return new AttackRequest(attackType, owner, origin, pipelineMode, providedContexts, playSound);
    }

    private static void requireGeneratedMode(AttackPipelineMode pipelineMode) {
        if (pipelineMode != AttackPipelineMode.FULL && pipelineMode != AttackPipelineMode.PLAN_PREPARE_AND_EXECUTE) {
            throw new IllegalArgumentException("generated requests require FULL or PLAN_PREPARE_AND_EXECUTE mode");
        }
    }

    private static void requireProvidedMode(AttackPipelineMode pipelineMode) {
        if (pipelineMode != AttackPipelineMode.PREPARE_AND_EXECUTE && pipelineMode != AttackPipelineMode.EXECUTE_ONLY) {
            throw new IllegalArgumentException("withContexts requests require PREPARE_AND_EXECUTE or EXECUTE_ONLY mode");
        }
    }

    /** Returns the attack implementation that will execute this request. */
    public @NotNull AttackType getAttackType() {
        return attackType;
    }

    /** Returns the non-null entity that owns the attack. */
    public @NotNull LivingEntity getOwner() {
        return owner;
    }

    /** Returns the owner as a server player, or {@code null} for non-player attacks. */
    public @Nullable ServerPlayer getPlayer() {
        return owner instanceof ServerPlayer player ? player : null;
    }

    /** Returns the source category of this attack. */
    public @NotNull AttackOrigin getOrigin() {
        return origin;
    }

    /** Returns the explicitly selected pipeline stages. */
    public @NotNull AttackPipelineMode getPipelineMode() {
        return pipelineMode;
    }

    /** Returns the immutable contexts supplied by the caller, if this is a provided-context request. */
    public @NotNull List<AttackContext> getProvidedContexts() {
        return providedContexts;
    }

    public boolean shouldPlaySound() {
        return playSound;
    }
}
