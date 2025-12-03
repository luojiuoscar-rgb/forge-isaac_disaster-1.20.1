package net.luojiuoscar.isaac_disaster.item_ability.pickup.items;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IFoodPickup;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.PickupId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public class DoubleRedHeart implements IFoodPickup {

    @Override
    public int getItemId() {
        return PickupId.DOUBLE_RED_HEART.getId();
    }

    @Override
    public void onUseEffect(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        // 恢复生命值
        StatManager.healHealth(player, 2);
        MobEffectInstance frailty = new MobEffectInstance(
                ModEffects.FRAILTY.get(),
                600,
                0,
                false,
                true,
                true
        );

        // 将效果施加给玩家
        player.addEffect(frailty);
    }

    @Override
    public void onUseSound(Player player) {
        player.playNotifySound(ModSounds.RED_HEART.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
