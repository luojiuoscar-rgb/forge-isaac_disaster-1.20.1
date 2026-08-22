package net.luojiuoscar.isaac_disaster.registries.attack_type;

/** Public entrypoint for structured attack execution. */
public final class AttackExecutor {
    private AttackExecutor() {
    }

    /** Executes the request through the default pipeline. */
    public static boolean perform(AttackRequest request) {
        return AttackPipeline.executeRequest(request);
    }
}
