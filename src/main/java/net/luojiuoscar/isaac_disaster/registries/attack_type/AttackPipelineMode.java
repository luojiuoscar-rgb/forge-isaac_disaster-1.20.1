package net.luojiuoscar.isaac_disaster.registries.attack_type;

/** Declares which stages of the attack pipeline should run. */
public enum AttackPipelineMode {
    /** Run before-event, plan, per-context preparation, and attack execution. */
    FULL,
    /** Skip the before-event, but still run plan, preparation, and execution. */
    GROUP_AND_BULLET,
    /** Skip plan generation and only prepare and execute the provided contexts. */
    BULLET_ONLY,
    /** Bypass all pipeline events and execute the provided contexts directly. */
    RAW
}
