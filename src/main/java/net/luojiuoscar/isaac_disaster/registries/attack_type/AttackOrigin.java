package net.luojiuoscar.isaac_disaster.registries.attack_type;

/** Describes where an attack request originates. */
public enum AttackOrigin {
    /** Player-primary attack release, including delayed callbacks belonging to that release. */
    PLAYER_PRIMARY,
    /** Additional attack spawned by a passive or active ability. */
    ABILITY_EXTRA,
    /** Attack spawned by another bullet or projectile. */
    BULLET_SECONDARY,
    /** Attack created as a split child. */
    SPLIT_CHILD,
    /** Internal system-driven attack. */
    SYSTEM
}
