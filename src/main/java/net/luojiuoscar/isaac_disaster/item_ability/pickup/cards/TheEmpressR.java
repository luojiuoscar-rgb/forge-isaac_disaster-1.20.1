package net.luojiuoscar.isaac_disaster.item_ability.pickup.cards;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.effect.custom.FragileHeartEffect;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.ITarot;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.PickupId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TheEmpressR implements ITarot {
    @Override
    public int getItemId() {
        return PickupId.THE_EMPRESS_R.getId();
    }

    @Override
    public void onUseEffect(Player player, ItemStack stack, InteractionHand hand) {
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 600, 1));
        FragileHeartEffect.stack(player, 1);
    }

    @Override
    public void onUseEffectStronger(Player player, ItemStack stack, InteractionHand hand) {
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 1200, 1));
        FragileHeartEffect.stack(player, 1);
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(SoundEvents.BOOK_PAGE_TURN);
        player.playSound(ModSounds.THE_EMPRESS_R.get());
    }

    @Override
    public List<Component> getDescription() {
        List<Component> description = new ArrayList<>();

        description.add(Component.translatable("item.isaac_disaster.the_empress_r.lore.1"));

        if (ClientDataManager.getInstance().getCountFromId(ItemId.TAROT_CLOTH.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.tarot_cloth").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.increase_duration"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }

        return description;
    }
}
