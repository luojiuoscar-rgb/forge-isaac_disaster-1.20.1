package net.luojiuoscar.isaac_disaster.registries.ability.pickup.impl.cards;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.TarotAbility;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TheHighPriestess extends TarotAbility {
    @Override
    public void onUseEffect(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        List<LivingEntity> entities = LevelHelper.selectBySphere(player.level(), player.getX(), player.getY(), player.getZ(), StatManager.getNearbyRange() * 0.6);
        double highestHealth = player.getHealth();
        LivingEntity target = player;

        for (LivingEntity entity : entities){
            double health = entity.getHealth();
            if (health > highestHealth) {
                highestHealth = health;
                target = entity;
            }
        }

        target.hurt(player.damageSources().generic(), (float) StatManager.DAMAGE.getBonus() * 100f);
    }

    @Override
    public void onUseEffectS(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        List<LivingEntity> entities = LevelHelper.selectBySphere(player.level(), player.getX(), player.getY(), player.getZ(), StatManager.getNearbyRange() * 0.6);
        double highestHealth = player.getHealth();
        LivingEntity target = player;

        for (LivingEntity entity : entities){
            double health = entity.getHealth();
            if (health > highestHealth) {
                highestHealth = health;
                target = entity;
            }
        }

        target.hurt(player.damageSources().generic(), (float) StatManager.DAMAGE.getBonus() * 200f);
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.THE_HIGH_PRIESTESS.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

    }

    @Override
    public List<Component> getDesc() {
        List<Component> description = new ArrayList<>();
        // 基础效果
        description.add(Component.translatable("item.isaac_disaster.the_high_priestess.lore.1"));
        description.add(Component.translatable("item.isaac_disaster.the_high_priestess.lore.2"));

        // 塔罗牌桌布
        if (ClientDataManager.getInstance().getCountFromId(ItemId.TAROT_CLOTH.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.tarot_cloth").append(": ")
                    .append(Component.translatable("item.isaac_disaster.the_high_priestess.synergy.lore.1"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }

        return description;
    }
}
