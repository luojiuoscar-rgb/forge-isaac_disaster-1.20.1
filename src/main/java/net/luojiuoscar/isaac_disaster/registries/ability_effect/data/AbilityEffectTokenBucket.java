package net.luojiuoscar.isaac_disaster.registries.ability_effect.data;

/**
 * Global token bucket intended to bound the total number of ability-effect executions per server tick.
 *
 * <p>The bucket starts full, holds at most {@value #MAX_TOKENS} tokens, and replenishes
 * {@value #REFILL_PER_TICK} tokens when {@link #tick()} runs at the end of each server tick.
 * Callers must reserve their execution cost through {@link #tryConsume(int)} before applying an
 * effect. The bucket does not intercept effects by itself; without a caller consuming tokens, it
 * has no limiting effect.</p>
 */
public class AbilityEffectTokenBucket {
    private static final int MAX_TOKENS = 500;
    private static final int REFILL_PER_TICK = 20;

    private int tokens = MAX_TOKENS;

    private static final AbilityEffectTokenBucket INSTANCE = new AbilityEffectTokenBucket();

    private AbilityEffectTokenBucket() {}

    public static AbilityEffectTokenBucket getInstance() {
        return INSTANCE;
    }

    public void tick() {
        tokens = Math.min(MAX_TOKENS, tokens + REFILL_PER_TICK);
    }

    public boolean tryConsume(int cost) {
        if (tokens < cost) return false;
        tokens -= cost;
        return true;
    }

    public int getTokens() {
        return tokens;
    }
}
