package net.luojiuoscar.isaac_disaster.registries.recursive_module;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

public class RecursiveModuleInstance {
    public ResourceLocation id;
    public int stacks;
    public int coolDown;
    private final IRecursiveModule recursiveModule;

    public RecursiveModuleInstance(ResourceLocation id, int stacks, int coolDown){
        this.id = id;
        this.stacks = stacks;
        this.coolDown = coolDown;

        IForgeRegistry<IRecursiveModule> registry =
                RegistryManager.ACTIVE.getRegistry(ModRecursiveModule.RECURSIVE_MODULE_KEY);
        this.recursiveModule = registry == null ? null : registry.getValue(id);
    }

    public void trigger(LivingEntity entity, RecursiveModuleQueue queue){
        // 需要动态获取；可能存在可变的interval
        if (recursiveModule == null) return;

        AbilityEffectContext ctx = new AbilityEffectContext(entity);
        ctx.set(ContextKeys.AMPLIFIER, stacks);
        ctx.set(ContextKeys.TARGET_POSITION, entity.position());
        ctx.set(ContextKeys.RECURSIVE_MODULE_QUEUE, queue);


        recursiveModule.fire(ctx);

        // 完成任务后重置coolDown
        coolDown = recursiveModule.getTickInterval(entity, stacks, queue);
    }
}
