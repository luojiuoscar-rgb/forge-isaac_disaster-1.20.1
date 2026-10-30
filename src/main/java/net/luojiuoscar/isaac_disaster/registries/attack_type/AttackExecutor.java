package net.luojiuoscar.isaac_disaster.registries.attack_type;

/** Public entrypoint for structured attack execution. */
public final class AttackExecutor {
    private AttackExecutor() {
    }

    /** Accepts a request into the pipeline; actual launches are reported by AttackFiredEvent. */
    public static boolean perform(AttackRequest request) {
        return AttackPipeline.executeRequest(request);
    }
}
