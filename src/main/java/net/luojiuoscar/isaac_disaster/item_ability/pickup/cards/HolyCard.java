package net.luojiuoscar.isaac_disaster.item_ability.pickup.cards;

import net.luojiuoscar.isaac_disaster.effect.custom.HolyShieldEffect;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPickup;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.PickupId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class HolyCard implements IPickup {
    @Override
    public int getItemId() {
        return PickupId.HOLY_CARD.getId();
    }

    @Override
    public void onUseEffect(Player player, ItemStack stack, InteractionHand hand) {
        HolyShieldEffect.stack(player, 1);
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(SoundEvents.BOOK_PAGE_TURN);
        player.playSound(ModSounds.HOLY_CARD.get());
    }

    @Override
    public List<Component> getDescription() {
        List<Component> description = new ArrayList<>();
        // 基础效果
        description.add(Component.translatable("item.isaac_disaster.holy_card.lore.1"));

        return description;
    }
}
