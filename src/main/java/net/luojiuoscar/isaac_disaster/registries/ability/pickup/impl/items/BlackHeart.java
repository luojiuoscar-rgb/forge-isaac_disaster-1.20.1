package net.luojiuoscar.isaac_disaster.registries.ability.pickup.impl.items;

import net.luojiuoscar.isaac_disaster.effect.custom.NecronmiconShieldEffect;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.FoodPickupAbility;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;


public class BlackHeart extends FoodPickupAbility {
    @Override
    public void onUseEffect(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        StatManager.gainAbsorption(player, 1);

        MobEffectInstance strength = new MobEffectInstance(
                MobEffects.DAMAGE_BOOST,
                600,
                1,
                false,
                true,
                true
        );
        player.addEffect(strength);
        // 增加一层死灵庇护
        NecronmiconShieldEffect.stack(player, 1);
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.BLACK_HEART.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
