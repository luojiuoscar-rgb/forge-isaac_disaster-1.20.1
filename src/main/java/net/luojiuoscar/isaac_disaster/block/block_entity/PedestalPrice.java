package net.luojiuoscar.isaac_disaster.block.block_entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Immutable acquisition price for a pedestal item. */
public record PedestalPrice(Type type, int amount) {
    private static final Logger LOGGER = LoggerFactory.getLogger(PedestalPrice.class);

    public enum Type {
        FREE,
        LIFE,
        MONEY
    }

    public PedestalPrice {
        if (type == null || amount < 0 || (type == Type.FREE && amount != 0)) {
            LOGGER.warn("Invalid pedestal price (type={}, amount={}); using FREE with amount 0", type, amount);
            type = Type.FREE;
            amount = 0;
        } else if (amount == 0) {
            type = Type.FREE;
        }
    }

    public static PedestalPrice free() {
        return new PedestalPrice(Type.FREE, 0);
    }

    public static PedestalPrice life(int amount) {
        return new PedestalPrice(Type.LIFE, amount);
    }

    public static PedestalPrice money(int amount) {
        return new PedestalPrice(Type.MONEY, amount);
    }

    public boolean isFree() {
        return type == Type.FREE;
    }
}
