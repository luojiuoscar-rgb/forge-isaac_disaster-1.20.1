package net.luojiuoscar.isaac_disaster.registries.familiar;

/** Immutable registry definition; each player owns their own allocation of its capacity. */
public final class FamiliarLimitGroup {
    private final int limit;

    public FamiliarLimitGroup(int limit) {
        if (limit < 0) limit = 0;
        this.limit = limit;
    }

    public int getLimit() { return limit; }
}
