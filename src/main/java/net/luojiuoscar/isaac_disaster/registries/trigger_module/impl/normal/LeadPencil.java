package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.LeadPencilShotCounter;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.RegistryManager;

import java.util.List;

public final class LeadPencil extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.ATTACK_PLAN, ModExecutableEffects.LEAD_PENCIL_SHOT_COUNTER)
    ));

    public LeadPencil() {
        super(TRIGGER);
    }

    @Override
    public void attachToBullet(ExecutableEffectContext context, AttackContext attackContext) {
        if (!(context.getEntity() instanceof ServerPlayer)) return;
        if (!ModAttackTypes.BULLET.getId().equals(attackContext.getRootTypeId())
                || ModAttackTypes.C_SECTION.getId().equals(attackContext.getTypeId())) return;
        if (context.getEntity().getRandom().nextDouble() < 0.5) {
            applyBloodColor(attackContext);
        }
    }

    @Override
    public void onRemove(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) return;
        boolean stillOwned = entity.getCapability(EffectModulesProvider.EFFECT_MODULES)
                .map(modules -> modules.getTriggerModules().contains(ModTriggerModules.LEAD_PENCIL.getId()))
                .orElse(false);
        if (!stillOwned) {
            LeadPencilShotCounter.clearProgress(player);
        }
    }

    /** Applies the blood-tear tint without replacing a higher-priority projectile color. */
    public static void applyBloodColor(AttackContext attackContext) {
        var registry = RegistryManager.ACTIVE.getRegistry(ModBulletColors.BULLET_COLOR_KEY);
        if (registry == null) return;
        var blood = registry.getValue(ModBulletColors.BLOOD_TEAR.getId());
        if (blood == null) return;
        var current = attackContext.getColorRl() == null ? null : registry.getValue(attackContext.getColorRl());
        if (current == null || current.priority() < blood.priority()) {
            attackContext.setColorRl(ModBulletColors.BLOOD_TEAR.getId());
        }
    }
}
