package net.luojiuoscar.isaac_disaster.item_ability.pickup.items;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPickup;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.PickupId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public class BlackHeart implements IPickup {

    @Override
    public int getItemId() {
        return PickupId.BLACK_HEART.getId();
    }

    @Override
    public void onUseEffect(Player player, ItemStack stack, InteractionHand hand) {
        StatManager.gainAbsorption(player, 1);

        MobEffectInstance strength = new MobEffectInstance(
                MobEffects.DAMAGE_BOOST,
                600,
                1,
                false,
                true,
                true
        );

        // 增加一层死灵庇护
        PlayerHelper.addAmplifier(player, ModEffects.NECRONMICON_SHIELD.get());
        // 将效果施加给玩家
        player.addEffect(strength);
    }

    @Override
    public void onUseSound(Player player) {
        SoundEvent sound = ModSounds.BLACK_HEART.get();
        player.level().playSound(null, player.blockPosition(),
                sound, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
