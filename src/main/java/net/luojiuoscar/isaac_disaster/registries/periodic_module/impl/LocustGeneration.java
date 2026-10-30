package net.luojiuoscar.isaac_disaster.registries.periodic_module.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.PeriodicModule;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.PeriodicModuleQueue;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class LocustGeneration extends PeriodicModule {
    private final ResourceLocation familiar;

    public LocustGeneration(ResourceLocation familiar, ResourceLocation source) {
        super(new CompositeTrigger(List.of(new SimpleTrigger(ModTriggerTypes.EMTPY,
                ModExecutableEffects.ADD_FAMILIARS, null, source))));
        this.familiar = familiar;
    }

    @Override
    public int getInitialTick() {
        return StatManager.getTimeInterval(4);
    }

    @Override
    public int getTickInterval(LivingEntity entity, int amplifier, PeriodicModuleQueue queue) {
        return StatManager.getTimeInterval(4);
    }

    @Override
    public void fire(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return;
        int amplifier = Math.max(0, context.getOrDefault(ContextKeys.AMPLIFIER, 1.0D).intValue());
        int count = getGenerationCount(player, amplifier);
        if (count == 0) return;
        context.set(ContextKeys.RESOURCE_LOCATIONS, List.of(familiar));
        context.set(ContextKeys.COUNT, count);
        super.fire(context);
    }

    /** 为首次获取的单份饰品生成一批蝗虫，不触发其他副本或改变循环计时。 */
    public void generateForTrinket(ServerPlayer player, int amplifier) {
        ExecutableEffectContext context = new ExecutableEffectContext(player);
        context.set(ContextKeys.AMPLIFIER, (double) amplifier);
        fire(context);
    }

    protected int getGenerationCount(ServerPlayer player, int amplifier) {
        return amplifier;
    }
}
