package net.luojiuoscar.isaac_disaster.item_ability.pickup.cards;

import net.luojiuoscar.isaac_disaster.block.ModBlockEntities;
import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
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

public class TheStars implements ITarot {
    @Override
    public int getItemId() {
        return PickupId.THE_STARS.getId();
    }

    @Override
    public void onUseEffect(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        boolean s = PlayerHelper.teleportToNearestIdentifier(
                (ServerPlayer) player, ModBlockEntities.PLANETARIUM_IDENTIFIER_BLOCK_ENTITY.getId());
        if (!s){
            PlayerHelper.teleportToNearestIdentifier(
                    (ServerPlayer) player, ModBlockEntities.TREASURE_IDENTIFIER_BLOCK_ENTITY.getId());
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f ,1.0f);

    }

    @Override
    public void onUseEffectStronger(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        onUseEffect(player, stack, hand); // teleport
        PlayerHelper.copyNearestPedestal((ServerPlayer) player, true);
    }

    @Override
    public void onUseSound(Player player) {
        player.playNotifySound(SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.THE_STARS.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public List<Component> getDescription() {
        List<Component> description = new ArrayList<>();
        // 基础效果
        description.add(Component.translatable("item.isaac_disaster.the_stars.lore.1"));

        // 塔罗牌桌布
        if (ClientDataManager.getInstance().getCountFromId(ItemId.TAROT_CLOTH.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.tarot_cloth").append(": ")
                    .append(Component.translatable("item.isaac_disaster.the_stars.tarot_cloth.lore.1"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }

        return description;
    }
}
