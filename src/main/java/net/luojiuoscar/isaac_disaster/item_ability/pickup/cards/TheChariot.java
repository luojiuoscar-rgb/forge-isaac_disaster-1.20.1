package net.luojiuoscar.isaac_disaster.item_ability.pickup.cards;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TheChariot implements ITarot {
    @Override
    public int getItemId() {
        return PickupId.THE_CHARIOT.getId();
    }

    @Override
    public void onUseEffect(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        int duration = 160;
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 0));
        player.addEffect(new MobEffectInstance(ModEffects.INVINCIBLE.get(), duration));
        player.addEffect(new MobEffectInstance(ModEffects.LACRIMAL_HYPOSECRETION.get(), duration));
        player.addEffect(new MobEffectInstance(ModEffects.RAMPAGE.get(), duration, 3));
    }

    @Override
    public void onUseEffectStronger(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        int duration = 320;
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 0));
        player.addEffect(new MobEffectInstance(ModEffects.INVINCIBLE.get(), duration));
        player.addEffect(new MobEffectInstance(ModEffects.LACRIMAL_HYPOSECRETION.get(), duration));
        player.addEffect(new MobEffectInstance(ModEffects.RAMPAGE.get(), duration, 3));
    }

    @Override
    public void onUseSound(Player player) {
        player.playNotifySound(SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.THE_CHARIOT.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public List<Component> getDescription() {
        List<Component> description = new ArrayList<>();
        // 基础效果
        description.add(Component.translatable("item.isaac_disaster.the_chariot.lore.1"));

        // 塔罗牌桌布
        if (ClientDataManager.getInstance().getCountFromId(ItemId.TAROT_CLOTH.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.tarot_cloth").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.increase_duration"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }

        return description;
    }
}
