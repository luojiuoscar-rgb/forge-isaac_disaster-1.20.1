package net.luojiuoscar.isaac_disaster.registries.attack_type;

/** Declares which stages of the attack pipeline should run. */
public enum AttackPipelineMode {
    /** Run Before, Plan, Prepare, and Execute. */
    FULL,
    /** Run Plan, Prepare, and Execute; the caller owns the attack-level Before stage. */
    PLAN_PREPARE_AND_EXECUTE,
    /** Run Prepare and Execute for caller-provided contexts. */
    PREPARE_AND_EXECUTE,
    /** Run Execute only for already-prepared caller-provided contexts. */
    EXECUTE_ONLY
}
