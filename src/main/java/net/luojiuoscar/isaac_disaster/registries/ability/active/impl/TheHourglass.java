package net.luojiuoscar.isaac_disaster.registries.ability.active.impl;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ActiveAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TheHourglass extends ActiveAbility {
    public TheHourglass(int id, int level) {
        super(id, level);
    }

    @Override
    public void onFirstUse(ServerPlayer player, @Nullable ItemStack stack, @javax.annotation.Nullable InteractionHand hand) {

    }

    @Override
    public void onTrigger(ServerPlayer player, ItemStack stack, @javax.annotation.Nullable InteractionHand hand) {
        List<LivingEntity> entities = LevelHelper.selectBySphere(player.level(),
                player.getX(), player.getY(), player.getZ(), StatManager.getNearbyRange());

        for (LivingEntity entity : entities){
            if (EntityHelper.isFriendly(entity, player)) continue;

            MobEffectInstance slowness = new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    160,
                    1
            );

            entity.addEffect(slowness);
        }
    }

    @Override
    public void onTriggerStronger(ServerPlayer player, ItemStack stack, @javax.annotation.Nullable InteractionHand hand){
        List<LivingEntity> entities = LevelHelper.selectBySphere(player.level(),
                player.getX(), player.getY(), player.getZ(), StatManager.getNearbyRange());

        for (LivingEntity entity : entities){
            if (EntityHelper.isFriendly(entity, player)) continue;

            MobEffectInstance slowness = new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    320,
                    2
            );

            entity.addEffect(slowness);
        }
    }

    @Override
    public void triggerSFX(ServerPlayer player) {
    }


    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("item.isaac_disaster.the_hourglass.lore.1")
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack) {
        List<Component> description = new ArrayList<>();

        if (ClientDataManager.getInstance().getCountFromId(ItemId.CAR_BATTERY.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.car_battery").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.stronger"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }
        return description;
    }
}
