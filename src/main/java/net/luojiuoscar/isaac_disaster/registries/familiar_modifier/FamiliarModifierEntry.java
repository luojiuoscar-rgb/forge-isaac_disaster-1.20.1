package net.luojiuoscar.isaac_disaster.registries.familiar_modifier;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.entity.familiar.AbstractIsaacFamiliarEntity;

import java.util.Objects;
import java.util.function.Predicate;

/** A registered source definition; player data stores its registry ID and count only. */
public record FamiliarModifierEntry(Parameter parameter, double amount,
                                    Predicate<AbstractIsaacFamiliarEntity> filter) {
    public enum Parameter { ENHANCEMENT, VISUAL_SCALE }

    public FamiliarModifierEntry {
        Objects.requireNonNull(parameter, "familiar modifier parameter");
        Objects.requireNonNull(filter, "familiar modifier filter");
        if (!Double.isFinite(amount)) {
            IsaacDisaster.LOGGER.warn("Non-finite familiar modifier amount {}; using 0", amount);
            amount = 0;
        }
    }
}
