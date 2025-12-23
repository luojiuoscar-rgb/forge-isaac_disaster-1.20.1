package net.luojiuoscar.isaac_disaster.effect.custom;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public class GildingEffect extends MobEffect {
    public GildingEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }

    public static void onTriggered(LivingHurtEvent event){
        if (!(event.getEntity() instanceof Player player)) return;

        MobEffectInstance instance = player.getEffect(ModEffects.GILDING.get());

        int amplifier = instance.getAmplifier() + 1;
        LootHelper.spawnLootAtPos(player, player.position(), LootTableManager.RANDOM_COINS, amplifier);
        player.removeEffect(ModEffects.GILDING.get()); // 移除

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    public static void stack(LivingEntity entity, int count){
        MobEffect effect = ModEffects.GILDING.get();

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
