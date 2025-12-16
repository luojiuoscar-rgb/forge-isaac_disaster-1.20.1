package net.luojiuoscar.isaac_disaster.registries.ability.active;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability.IsaacItemAbility;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public abstract class ActiveAbility extends IsaacItemAbility {
    public ActiveAbility(int id, int level) {
        super(id, level);
    }

    public void onUse(ServerPlayer player, @Nullable InteractionHand hand){
        ItemStack stack = hand != null ? player.getItemInHand(hand) : null;

        // 判断车载电池
        if (PlayerHelper.hasItem(ItemId.CAR_BATTERY.getId(), player)){
            onTriggerStronger(player, stack, hand);
        }else {
            onTrigger(player, stack, hand);
        }
    }

    abstract public void onFirstUse(ServerPlayer player, @Nullable ItemStack stack, @Nullable InteractionHand hand);

    abstract public void onTrigger(ServerPlayer player, @Nullable ItemStack stack, @Nullable InteractionHand hand);

    abstract public void onTriggerStronger(ServerPlayer player, @Nullable ItemStack stack, @Nullable InteractionHand hand);

    public void rechargeSFX(ServerPlayer player) {
        player.level().playSound(null, player.blockPosition(), ModSounds.BATTERY_SMALL.get(),
                SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    abstract public void triggerSFX(ServerPlayer player);

}
