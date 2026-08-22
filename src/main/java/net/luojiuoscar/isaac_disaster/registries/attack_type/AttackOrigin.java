package net.luojiuoscar.isaac_disaster.registries.attack_type;

/** Describes where an attack request originates. */
public enum AttackOrigin {
    /** Immediate player input. */
    PLAYER_PRIMARY,
    /** Player attack that executes later from a scheduled callback. */
    PLAYER_SCHEDULED,
    /** Additional attack spawned by a passive or active ability. */
    ABILITY_EXTRA,
    /** Attack spawned by another bullet or projectile. */
    BULLET_SECONDARY,
    /** Attack created as a split child. */
    SPLIT_CHILD,
    /** Internal system-driven attack. */
    SYSTEM
}
