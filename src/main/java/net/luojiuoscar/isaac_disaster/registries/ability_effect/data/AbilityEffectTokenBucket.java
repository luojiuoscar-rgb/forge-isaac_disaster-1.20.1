package net.luojiuoscar.isaac_disaster.registries.ability_effect.data;

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