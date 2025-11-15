package net.luojiuoscar.isaac_disaster.effect.custom;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.event.custom.attack.BeforeCreateShootEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.List;

public class TheWizEffect extends MobEffect {
    public TheWizEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return Collections.emptyList();
    }

    public static void onTriggered(BeforeCreateShootEvent event){
        LivingEntity entity = event.getShooter();
        int amplifier = entity.getEffect(ModEffects.THE_WIZ.get()).getAmplifier();
        float xRot = event.getXRot();
        float yRot = event.getYRot();

        if (amplifier > 0){
            event.addExtraShot(Vec3.ZERO, xRot, yRot - 45);
            event.addExtraShot(Vec3.ZERO, xRot, yRot + 45);

        }else if (entity.getRandom().nextDouble() < 0.5){
            event.addExtraShot(Vec3.ZERO, xRot, yRot - 45);
        }else{
            event.addExtraShot(Vec3.ZERO, xRot, yRot + 45);
        }

        event.setCanceled(true);
    }
}
