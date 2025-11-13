package net.luojiuoscar.isaac_disaster.item_ability.pickup.cards;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.ITarot;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id.PickupId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TheHangedMan implements ITarot {
    @Override
    public int getItemId() {
        return PickupId.THE_HANGED_MAN.getId();
    }

    @Override
    public void onUseEffect(Player player, ItemStack stack, InteractionHand hand) {
        player.addEffect(new MobEffectInstance(ModEffects.TRANSCENDENCE.get(), 600));

    }

    @Override
    public void onUseEffectStronger(Player player, ItemStack stack, InteractionHand hand) {
        player.addEffect(new MobEffectInstance(ModEffects.TRANSCENDENCE.get(), 1200));
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(SoundEvents.BOOK_PAGE_TURN);
        player.playSound(ModSounds.JUSTICE.get());
    }

    @Override
    public List<Component> getDescription() {
        List<Component> description = new ArrayList<>();

        description.add(StatManager.FLY_TIME.description(1));

        if (ClientDataManager.getInstance().getCountFromId(ItemId.TAROT_CLOTH.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.tarot_cloth").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.increase_duration"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }

        return description;
    }
}
