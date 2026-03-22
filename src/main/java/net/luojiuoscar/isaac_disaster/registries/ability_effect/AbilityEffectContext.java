package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class AbilityEffectContext {

    /** 触发效果的实体本身 */
    @NotNull
    private final LivingEntity entity;

    private final Map<ContextKey<?>, Object> data = new HashMap<>();

    public AbilityEffectContext(LivingEntity entity) {
        this.entity = entity;
    }

    public LivingEntity getEntity() { return entity; }

    public <T> void set(ContextKey<T> key, T value) {
        data.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(ContextKey<T> key) {
        return (T) data.get(key);
    }

    public <T> T getOrDefault(ContextKey<T> key, T defaultValue) {
        T value = get(key);
        return value != null ? value : defaultValue;
    }

    public <T> boolean has(ContextKey<T> key) {
        return data.containsKey(key);
    }

    public AbilityEffectContext copy(@Nullable LivingEntity e) {
        LivingEntity newEntity = e != null ? e : this.entity;
        AbilityEffectContext ctx = new AbilityEffectContext(newEntity);
        ctx.data.putAll(this.data);
        return ctx;
    }
}