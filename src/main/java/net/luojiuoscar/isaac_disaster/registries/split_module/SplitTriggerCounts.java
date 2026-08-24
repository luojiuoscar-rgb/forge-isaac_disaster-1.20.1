package net.luojiuoscar.isaac_disaster.registries.split_module;

import java.util.Objects;

/** Runtime split-trigger counters owned by one bullet object. */
public final class SplitTriggerCounts {
    private int blockHits = 0;
    private int entityHits = 0;
    private int endOfLife = 0;

    /** Returns an independent snapshot that cannot mutate the owning bullet's counters. */
    public SplitTriggerCounts copy() {
        SplitTriggerCounts copy = new SplitTriggerCounts();
        copy.blockHits = blockHits;
        copy.entityHits = entityHits;
        copy.endOfLife = endOfLife;
        return copy;
    }

    public void increment(SplitTriggerType type) {
        Objects.requireNonNull(type, "type");
        switch (type) {
            case BLOCK -> blockHits++;
            case ENTITY -> entityHits++;
            case END_OF_LIFE -> endOfLife++;
        }
    }

    public int getBlockHits() { return blockHits; }
    public int getEntityHits() { return entityHits; }
    public int getEndOfLife() { return endOfLife; }
}
