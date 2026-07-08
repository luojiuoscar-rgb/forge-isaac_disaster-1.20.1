package net.luojiuoscar.isaac_disaster.entity.familiar.state;

/**
 * Minimal state machine contract for familiars whose behavior is driven by explicit states.
 *
 * @param <S> enum type that represents the familiar's possible states
 */
public interface FamiliarStateMachine<S extends Enum<S>> {
    S getFamiliarState();

    void setFamiliarState(S state);

    /**
     * Ticks behavior for the current familiar state.
     */
    void tickFamiliarState();

    /**
     * Switches to a new state through one shared entry point.
     */
    default void changeFamiliarState(S state) {
        setFamiliarState(state);
    }
}
