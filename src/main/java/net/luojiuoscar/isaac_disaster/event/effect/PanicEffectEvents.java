package net.luojiuoscar.isaac_disaster.event.effect;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.effect.custom.PanicEffect;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID)
public final class PanicEffectEvents {
    private PanicEffectEvents() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof Mob mob && PanicEffect.suppressesAttacks(mob)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onProjectileSpawn(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof Projectile projectile
                && projectile.getOwner() instanceof Mob mob && PanicEffect.suppressesAttacks(mob)) {
            event.setCanceled(true);
        }
    }
}
