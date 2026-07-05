package net.luojiuoscar.isaac_disaster.loot;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Thread-local stack describing the active loot generation intent.
 *
 * <p>Natural vanilla loot does not pass through this helper, so an empty stack is interpreted as
 * {@link LootGenerationMode#NATURAL_DROP}. Calls made by this mod should use {@link #run} around
 * loot-table rolls whenever the roll has a more specific meaning.</p>
 */
public final class LootGenerationContext {
    private static final ThreadLocal<Deque<LootGenerationMode>> MODE_STACK =
            ThreadLocal.withInitial(ArrayDeque::new);

    private LootGenerationContext() {
    }

    /**
     * Pushes a loot generation mode for the current thread.
     */
    public static void push(LootGenerationMode mode) {
        MODE_STACK.get().push(Objects.requireNonNull(mode));
    }

    /**
     * Pops the current loot generation mode.
     */
    public static void pop() {
        Deque<LootGenerationMode> stack = MODE_STACK.get();
        if (!stack.isEmpty()) {
            stack.pop();
        }
        if (stack.isEmpty()) {
            MODE_STACK.remove();
        }
    }

    /**
     * Returns the current mode, treating an empty stack as natural loot generation.
     */
    public static LootGenerationMode currentMode() {
        Deque<LootGenerationMode> stack = MODE_STACK.get();
        return stack.isEmpty() ? LootGenerationMode.NATURAL_DROP : stack.peek();
    }

    /**
     * Returns the current nested loot generation depth.
     */
    public static int depth() {
        return MODE_STACK.get().size();
    }

    /**
     * Runs an action while the supplied mode is active.
     */
    public static void run(LootGenerationMode mode, Runnable action) {
        push(mode);
        try {
            action.run();
        } finally {
            pop();
        }
    }

    /**
     * Runs a supplier while the supplied mode is active.
     */
    public static <T> T supply(LootGenerationMode mode, Supplier<T> supplier) {
        push(mode);
        try {
            return supplier.get();
        } finally {
            pop();
        }
    }
}
