package net.luojiuoscar.isaac_disaster.effect;


import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;


public class SoulStateEffect extends MobEffect {
    protected SoulStateEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public java.util.List<ItemStack> getCurativeItems() {
        return java.util.Collections.emptyList();
    }

    public static void onTriggered(ProjectileImpactEvent event){
        if (!(event.getRayTraceResult() instanceof EntityHitResult entityHit &&
                entityHit.getEntity() instanceof LivingEntity living)) return;

        if (living.hasEffect(ModEffects.SOUL_STATE.get())) {
            // 取消命中事件
            event.setCanceled(true);
        }
    }
}
