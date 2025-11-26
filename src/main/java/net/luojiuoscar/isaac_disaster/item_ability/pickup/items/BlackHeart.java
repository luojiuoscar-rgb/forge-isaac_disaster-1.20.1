package net.luojiuoscar.isaac_disaster.item_ability.pickup.items;

import net.luojiuoscar.isaac_disaster.effect.custom.NecronmiconShieldEffect;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IFoodPickup;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.PickupId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public class BlackHeart implements IFoodPickup {

    @Override
    public int getItemId() {
        return PickupId.BLACK_HEART.getId();
    }

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
    public void onUseSound(Player player) {
        player.playSound(ModSounds.BLACK_HEART.get());
    }
}
