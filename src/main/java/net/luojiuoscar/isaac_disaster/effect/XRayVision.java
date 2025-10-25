package net.luojiuoscar.isaac_disaster.effect;


import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class XRayVision extends MobEffect {
    protected XRayVision(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return java.util.Collections.emptyList();
    }


    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity instanceof Player player)) {
            entity.removeEffect(this);
            return;
        }

        List<LivingEntity> entities = LevelHelper.selectBySphere(
                player.level(), player.getX(), player.getY(), player.getZ(), StatManager.getNearbyRange() * Math.min((2 + amplifier * 0.5f), 10));

        for (LivingEntity target : entities){
            if (target instanceof Player) continue;

            if (target instanceof Mob){
                target.addEffect(new MobEffectInstance(
                        MobEffects.GLOWING,
                        80,
                        0,
                        false,
                        true,
                        true
                ));
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 40 == 0;
    }

}
