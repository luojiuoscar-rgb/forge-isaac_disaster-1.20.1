package net.luojiuoscar.isaac_disaster.item_ability.pickup.cards;

import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPickup;
import net.luojiuoscar.isaac_disaster.manager.id_managers.PickupId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TwoOfHearts implements IPickup {
    @Override
    public int getItemId() {
        return PickupId.TWO_OF_HEARTS.getId();
    }

    @Override
    public void onUseEffect(Player player, ItemStack stack, InteractionHand hand) {
        float newHealth = Math.min(player.getMaxHealth(), player.getHealth() * 2);
        player.setHealth(newHealth);
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(SoundEvents.BOOK_PAGE_TURN);
        player.playSound(ModSounds.TWO_OF_HEARTS.get());
    }

    @Override
    public List<Component> getDescription() {
        List<Component> description = new ArrayList<>();
        // 基础效果
        description.add(Component.translatable("item.isaac_disaster.2_of_hearts.lore.1"));

        return description;
    }
}
