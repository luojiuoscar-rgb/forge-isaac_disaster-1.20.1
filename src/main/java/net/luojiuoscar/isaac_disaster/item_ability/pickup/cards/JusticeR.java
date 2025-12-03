package net.luojiuoscar.isaac_disaster.item_ability.pickup.cards;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.ITarot;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.PickupId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class JusticeR implements ITarot {
    @Override
    public int getItemId() {
        return PickupId.JUSTICE_R.getId();
    }

    @Override
    public void onUseEffect(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        PlayerHelper.giveItem(player, ModItems.LOCKED_CHEST_ITEM.get(), player.getRandom().nextInt(2,5));
    }

    @Override
    public void onUseEffectStronger(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        PlayerHelper.giveItem(player, ModItems.LOCKED_CHEST_ITEM.get(), player.getRandom().nextInt(4,15));
    }

    @Override
    public void onUseSound(Player player) {
        player.playNotifySound(SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.JUSTICE_R.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public List<Component> getDescription() {
        List<Component> description = new ArrayList<>();
        // 基础效果
        description.add(Component.translatable("item.isaac_disaster.justice_r.lore.1"));

        // 塔罗牌桌布
        if (ClientDataManager.getInstance().getCountFromId(ItemId.TAROT_CLOTH.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.tarot_cloth").append(": ")
                    .append(Component.translatable("item.isaac_disaster.justice_r.tarot_cloth.lore.1"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }

        return description;
    }
}

