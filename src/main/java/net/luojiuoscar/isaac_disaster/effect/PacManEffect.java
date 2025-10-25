package net.luojiuoscar.isaac_disaster.effect;


import net.luojiuoscar.isaac_disaster.event.custom.PacManEatEvent;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;

public class PacManEffect extends MobEffect {
    protected PacManEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public java.util.List<ItemStack> getCurativeItems() {
        return java.util.Collections.emptyList();
    }


    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity instanceof Player player)) {
            entity.removeEffect(this);
            return;
        }

        List<LivingEntity> entities = LevelHelper.selectBySphere(
                player.level(), player.getX(), player.getY(), player.getZ(), StatManager.getNearbyRange() * 0.8f);

        for (LivingEntity target : entities){
            if (target instanceof Player) continue;

            if (target instanceof Mob){
                target.addEffect(new MobEffectInstance(
                        ModEffects.PANIC.get(),
                        25,
                        0,
                        false,
                        true,
                        true
                ));
            }
        }

        // 造成伤害&吃
        LivingEntity target = LevelHelper.findNearestLivingEntity(player.level(), player.getX(), player.getY(), player.getZ(),
                1, e -> e != player);
        damageOrEat(player, amplifier, target);
    }

    private void damageOrEat(Player player, int amplifier, LivingEntity entity){
        if (entity == null) return;
        // 对免疫恐惧的生物不生效
        if (!entity.hasEffect(ModEffects.PANIC.get())) return;

        float damage = (amplifier * 2f + 4f) * (float) StatManager.getDamageBonus();


        if (entity.getHealth() > damage){
            entity.hurt(player.damageSources().generic(), damage);
        }else{
            if (!entity.level().isClientSide) {
                // 触发事件
                MinecraftForge.EVENT_BUS.post(new PacManEatEvent(player, entity));
                // 直接移除生物，不等同于死亡
                entity.remove(Entity.RemovalReason.DISCARDED);
                // 恢复生命
                float newHealth = Math.min(player.getMaxHealth(),
                        Math.max(1, StatManager.getHealthBonus() * 0.1f) + player.getHealth());
                player.setHealth(newHealth);
                // 恢复饥饿
                player.getFoodData().setFoodLevel(20);
                // 播放音效
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }

    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 8 == 0;
    }

}
