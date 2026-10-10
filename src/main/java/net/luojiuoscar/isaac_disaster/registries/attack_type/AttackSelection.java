package net.luojiuoscar.isaac_disaster.registries.attack_type;

import java.util.List;
import java.util.Objects;

/** Immutable primary/base selection and the additional attacks currently participating for a player. */
public record AttackSelection(AttackCandidate mainCandidate, AttackCandidate baseCandidate,
                              List<AttackType> additionalAttacks) {
    public AttackSelection {
        Objects.requireNonNull(mainCandidate, "main candidate");
        Objects.requireNonNull(baseCandidate, "base candidate");
        additionalAttacks = List.copyOf(additionalAttacks);
    }

    public AttackType mainAttack() {
        return mainCandidate.attackType();
    }

    public AttackType baseAttack() {
        return baseCandidate.attackType();
    }
}
