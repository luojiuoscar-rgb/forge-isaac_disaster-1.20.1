package net.luojiuoscar.isaac_disaster.registries.periodic_module;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

public class PeriodicModuleInstance {
    public ResourceLocation id;
    public int stacks;
    public int coolDown;
    private final PeriodicModule periodicModule;

    public PeriodicModuleInstance(ResourceLocation id, int stacks, int coolDown){
        this.id = id;
        this.stacks = stacks;
        this.coolDown = coolDown;

        IForgeRegistry<PeriodicModule> registry =
                RegistryManager.ACTIVE.getRegistry(ModPeriodicModules.PERIODIC_MODULE_KEY);
        this.periodicModule = registry == null ? null : registry.getValue(id);
    }

    public void trigger(LivingEntity entity, PeriodicModuleQueue queue){
        // 需要动态获取；可能存在可变的interval
        if (periodicModule == null || stacks <= 0) return;

        ExecutableEffectContext ctx = new ExecutableEffectContext(entity);
        ctx.set(ContextKeys.AMPLIFIER, (double) stacks);
        ctx.set(ContextKeys.TARGET_POSITION, entity.position());
        ctx.set(ContextKeys.PERIODIC_MODULE_QUEUE, queue);

        periodicModule.fire(ctx);

        // 完成任务后重置coolDown
        coolDown = periodicModule.getTickInterval(entity, stacks, queue);
    }
}
