package net.luojiuoscar.isaac_disaster.registries.attack_type;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AttackPlan {
    private final @NotNull AttackRequest request;
    private final @NotNull List<AttackContext> originalBaseContexts;
    private @NotNull List<AttackContext> currentBaseContexts;
    private final @NotNull List<AttackContext> extraContexts = new ArrayList<>();
    private @Nullable List<AttackContext> frozenContexts;

    public AttackPlan(@NotNull AttackRequest request, @NotNull List<AttackContext> baseContexts) {
        this.request = Objects.requireNonNull(request, "request");
        this.originalBaseContexts = new ArrayList<>(Objects.requireNonNull(baseContexts, "baseContexts"));
        this.currentBaseContexts = new ArrayList<>(baseContexts);
    }

    /** Returns the request whose attack this plan describes. */
    public @NotNull AttackRequest getRequest() {
        return request;
    }

    /** Returns the original generated base contexts before replacement. */
    public @NotNull List<AttackContext> getOriginalBaseContexts() {
        return List.copyOf(originalBaseContexts);
    }

    /** Returns the current mutable base-group view as an immutable snapshot. */
    public @NotNull List<AttackContext> getBaseContexts() {
        return List.copyOf(currentBaseContexts);
    }

    /** Returns contexts appended as extra attacks. */
    public @NotNull List<AttackContext> getExtraContexts() {
        return List.copyOf(extraContexts);
    }

    /** Replaces only the base group while the plan is still mutable. */
    public void replaceBaseContexts(@NotNull List<AttackContext> contexts) {
        ensureNotFrozen();
        this.currentBaseContexts = new ArrayList<>(Objects.requireNonNull(contexts, "contexts"));
    }

    /** Appends contexts without changing the base group. */
    public void appendExtraContexts(@NotNull List<AttackContext> contexts) {
        ensureNotFrozen();
        this.extraContexts.addAll(Objects.requireNonNull(contexts, "contexts"));
    }

    /** Freezes and returns the final base-plus-extra context sequence. */
    public @NotNull List<AttackContext> freezeContexts() {
        if (frozenContexts == null) {
            List<AttackContext> merged = new ArrayList<>(currentBaseContexts);
            merged.addAll(extraContexts);
            frozenContexts = List.copyOf(merged);
        }
        return frozenContexts;
    }

    public boolean isFrozen() {
        return frozenContexts != null;
    }

    private void ensureNotFrozen() {
        if (isFrozen()) {
            throw new IllegalStateException("attack plan is already frozen");
        }
    }
}
