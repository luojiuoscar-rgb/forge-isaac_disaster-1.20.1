package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import oshi.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

@Immutable
public class CompositeTriggerView {
    private static final SimpleTrigger[] NO_TRIGGERS = new SimpleTrigger[0];
    private static final Selection[] NO_SELECTIONS = new Selection[0];
    private static final Comparator<SimpleTrigger> PRIORITY =
            (first, second) -> Double.compare(second.getPriority(), first.getPriority());
    private final List<SimpleTrigger> triggers;
    private volatile Selection primarySelection;
    private volatile Selection[] selections = NO_SELECTIONS;

    public CompositeTriggerView(List<SimpleTrigger> triggers) {
        this.triggers = List.copyOf(triggers);
    }

    public void fire(ExecutableEffectContext context, @Nullable TriggerType type) {
        for (SimpleTrigger trigger : triggers(type)) {
            context.set(ContextKeys.COMPOSITE_TRIGGER_VIEW, this);
            trigger.fire(context);
        }
    }

    SimpleTrigger[] triggers(@Nullable TriggerType type) {
        ResourceLocation id = type == null ? null : type.getId();
        Selection primary = primarySelection;
        if (primary == null) {
            // Racing first readers may compile twice; immutable results are equivalent.
            primary = compile(type, id);
            primarySelection = primary;
            return primary.triggers();
        }
        if (Objects.equals(primary.type(), id)) return primary.triggers();
        for (Selection selection : selections) {
            if (Objects.equals(selection.type(), id)) return selection.triggers();
        }
        return prepare(type, id);
    }

    private synchronized SimpleTrigger[] prepare(@Nullable TriggerType type, @Nullable ResourceLocation id) {
        Selection primary = primarySelection;
        if (Objects.equals(primary.type(), id)) return primary.triggers();
        for (Selection selection : selections) {
            if (Objects.equals(selection.type(), id)) return selection.triggers();
        }
        Selection selection = compile(type, id);
        Selection[] updated = Arrays.copyOf(selections, selections.length + 1);
        updated[selections.length] = selection;
        selections = updated;
        return selection.triggers();
    }

    private Selection compile(@Nullable TriggerType type, @Nullable ResourceLocation id) {
        // Registry lookup stays lazy. Only order/type membership are cached; predicates stay live.
        int count = 0;
        for (int i = 0; i < triggers.size(); i++) {
            if (type == null || triggers.get(i).type.is(type)) count++;
        }
        SimpleTrigger[] ordered = count == 0 ? NO_TRIGGERS : new SimpleTrigger[count];
        int index = 0;
        for (int i = 0; i < triggers.size(); i++) {
            SimpleTrigger trigger = triggers.get(i);
            if (type == null || trigger.type.is(type)) ordered[index++] = trigger;
        }
        if (count > 1) Arrays.sort(ordered, PRIORITY);
        return new Selection(id, ordered);
    }

    private record Selection(@Nullable ResourceLocation type, SimpleTrigger[] triggers) {}

    /** Returns true if the given effect is found */
    public boolean contains(IExecutableEffect effect){
        for (SimpleTrigger trigger : triggers){
            if (trigger.effect.get().equals(effect)){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "CompositeTrigger: " + triggers;
    }
}
