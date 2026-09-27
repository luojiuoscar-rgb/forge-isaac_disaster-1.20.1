package net.luojiuoscar.isaac_disaster.bullet.core;

/** Selects the motion controller used by a lightweight bullet. */
public enum BulletSteeringMode {
    /** Turns toward the desired velocity under per-tick acceleration limits. */
    LIMITED,
    /** Reorients directly toward the selected target each simulation tick. */
    DIRECT
}
