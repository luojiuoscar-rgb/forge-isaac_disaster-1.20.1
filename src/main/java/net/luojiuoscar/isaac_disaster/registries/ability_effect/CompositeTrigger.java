package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CompositeTrigger {
    public static final CompositeTrigger EMPTY = new CompositeTrigger(List.of());

    private final List<SimpleTrigger> triggers;
    private CompositeTriggerView cachedView;
    private boolean dirty = true;


    public CompositeTrigger(){
        this.triggers = new ArrayList<>();
    }

    public CompositeTrigger(List<SimpleTrigger> triggers) {
        this.triggers = new ArrayList<>(triggers);
    }

    public void fire(ExecutableEffectContext context, @Nullable TriggerType type) {
        context.set(ContextKeys.COMPOSITE_TRIGGER_VIEW, getView());
        getView().fire(context, type);
    }

    public CompositeTriggerView getView(){
        if (cachedView == null || dirty) {
            cachedView = new CompositeTriggerView(triggers);
            dirty = false;
        }
        return cachedView;
    }

    public void add(SimpleTrigger trigger){
        dirty = true;
        this.triggers.add(trigger);
    }

    public void addAll(List<SimpleTrigger> triggers){
        dirty = true;
        this.triggers.addAll(triggers);
    }

    public void addAll(CompositeTrigger compositeTrigger){
        dirty = true;
        this.triggers.addAll(compositeTrigger.triggers);
    }

    /** Remove the first encountered SimpleTrigger with given effect */
    public boolean remove(IExecutableEffect effect){
        dirty = true;

        SimpleTrigger toRemove = null;
        for (SimpleTrigger trigger : triggers){
            if (trigger.effect.get().equals(effect)){
                toRemove = trigger;
                break;
            }
        }

        if (toRemove != null){
            triggers.remove(toRemove);
            return true;
        }
        return false;
    }

    /** Remove all SimpleTriggers with given effect */
    public boolean removeAll(IExecutableEffect effect){
        dirty = true;

        List<SimpleTrigger> toRemove = new ArrayList<>();
        for (SimpleTrigger trigger : triggers){
            if (trigger.effect.get().equals(effect)){
                toRemove.add(trigger);
            }
        }

        if (!toRemove.isEmpty()){
            for (SimpleTrigger trigger : toRemove){
                this.triggers.remove(trigger);
            }
            return true;
        }
        return false;
    }

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