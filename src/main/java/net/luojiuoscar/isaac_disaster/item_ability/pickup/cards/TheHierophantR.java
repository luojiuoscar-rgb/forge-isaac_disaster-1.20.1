package net.luojiuoscar.isaac_disaster.item_ability.pickup.cards;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.ITarot;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.PickupId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TheHierophantR implements ITarot {
    @Override
    public int getItemId() {
        return PickupId.THE_HIEROPHANT_R.getId();
    }

    @Override
    public void onUseEffect(Player player, ItemStack stack, InteractionHand hand) {
        PlayerHelper.giveItem(player, ModItems.BONE_HEART.get(), 2);
    }

    @Override
    public void onUseEffectStronger(Player player, ItemStack stack, InteractionHand hand) {
        PlayerHelper.giveItem(player, ModItems.BONE_HEART.get(), 3);
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(SoundEvents.BOOK_PAGE_TURN);
        player.playSound(ModSounds.THE_HIEROPHANT_R.get());
    }

    @Override
    public List<Component> getDescription() {
        List<Component> description = new ArrayList<>();
        // 基础效果
        description.add(Component.translatable("item.isaac_disaster.action.give_bone_heart", 2));

        // 塔罗牌桌布
        if (ClientDataManager.getInstance().getCountFromId(ItemId.TAROT_CLOTH.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.tarot_cloth").append(": ")
                    .append(Component.translatable("item.isaac_disaster.action.give_bone_heart", 3))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }

        return description;
    }
}
