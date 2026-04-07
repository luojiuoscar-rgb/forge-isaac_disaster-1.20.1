package net.luojiuoscar.isaac_disaster.effect.custom;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public class NecronmiconShieldEffect extends MobEffect {
    public NecronmiconShieldEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }

    public static void onTriggered(LivingHurtEvent event){
        Entity attacker = event.getSource().getEntity();
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (event.getAmount() <= Math.max(1.0f, StatManager.MAX_HEALTH.getBonus() * 0.25f)) return;
        // 伤害来源不能是拥有死灵庇护的玩家；否则不生效
        if (attacker instanceof Player attackerplayer &&
                attackerplayer.hasEffect(ModEffects.NECRONMICON_SHIELD.get())) return;

        // effect
       // ModActiveAbility.THE_NECRONMICON.get().onTrigger(player, null, null);
        // remove 1 amplifier
        NecronmiconShieldEffect.stack(player, -1);
        // sounds
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.BLACK_HEART_ACTIVE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    public static void stack(LivingEntity entity, int count){
        MobEffect effect = ModEffects.NECRONMICON_SHIELD.get();

        int amplifier = entity.getEffect(effect) == null ? -1 : entity.getEffect(effect).getAmplifier();
        amplifier += count;

        entity.removeEffect(effect);
        if (amplifier < 0) return;

        MobEffectInstance newEffect = new MobEffectInstance(
                effect,
                -1,
                amplifier,
                false,
                false,
                true
        );
        entity.addEffect(newEffect);
    }
}
