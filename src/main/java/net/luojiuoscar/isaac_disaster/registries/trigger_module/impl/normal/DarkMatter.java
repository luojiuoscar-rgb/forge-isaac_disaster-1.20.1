package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraft.world.entity.LivingEntity;
import java.util.List;

public final class DarkMatter extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.HIT_ENTITY_RESTRICTED, ModExecutableEffects.DARK_MATTER,
                    context -> context.getEntity().getRandom().nextDouble() < chance(context.getEntity()))
    ));

    public DarkMatter() { super(TRIGGER); }

    static double chance(double luck) { return 1.0 / Math.max(1.0, 3.0 - Math.floor(luck * 0.1)); }
    public static double chance(LivingEntity entity) { return chance(getLuck(entity)); }

    @Override
    public void attachToBullet(ExecutableEffectContext context, AttackContext attackContext) {
        if (context.getEntity().getRandom().nextDouble() >= chance(context.getEntity())) return;
        attackContext.setColorRl(ModBulletColors.DARK_MATTER.getId());
        attackContext.addSimpleTrigger(new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE,
                ModExecutableEffects.DARK_MATTER));
    }
}
