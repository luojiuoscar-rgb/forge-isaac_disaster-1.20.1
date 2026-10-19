package net.luojiuoscar.isaac_disaster.bullet.core;

/** Why an attached bullet stopped following its target. */
public enum BulletStickEndReason {
    EXPIRED,
    TARGET_DIED,
    TARGET_REMOVED,
    TARGET_CHANGED_DIMENSION,
    REMOVED_BY_EFFECT;

    public boolean isNatural() { return this == EXPIRED; }

    public boolean isForced() { return !isNatural(); }
}
