package net.luojiuoscar.isaac_disaster.effect.custom;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class EternalHeartEffect extends MobEffect {
    public EternalHeartEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity instanceof Player player)) {
            entity.removeEffect(this);
            return;
        }
        MobEffectInstance instance = player.getEffect(this);
        if (instance == null) return;

        if (amplifier > 0 || instance.getDuration() < 20){
            active(player);
            entity.removeEffect(this);
        }
    }

    private void active(Player player){
        StatManager.MAX_HEALTH.apply(player,1);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.RED_HEART.get(), SoundSource.PLAYERS, 1.0f ,1.0f);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }


}
