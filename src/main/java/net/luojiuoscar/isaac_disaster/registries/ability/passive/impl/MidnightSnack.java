package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.item.custom.FoodPassiveItem;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MidnightSnack extends PassiveAbility {

    public MidnightSnack(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.healHealth(player, 1.0f);
        if (PlayerHelper.hasItem(ItemId.BINGE_EATER.getId(), (ServerPlayer) player) && stack != null){
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 1));
        }
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.MAX_HEALTH.apply(player, 1);

        if (PlayerHelper.hasItem(ItemId.BINGE_EATER.getId(), player) && stack != null){
            FoodPassiveItem.setBingeEater(stack, true);
            onFoodObtainEffect(player, stack);
        }
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.MAX_HEALTH.apply(player, -1);

        if (PlayerHelper.hasItem(ItemId.BINGE_EATER.getId(), player) && stack != null){
            FoodPassiveItem.setBingeEater(stack, true);
            onFoodObtainEffect(player, stack);
        }
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                StatManager.MAX_HEALTH.description(1)
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack) {
        List<Component> description = new ArrayList<>();

        if (ClientDataManager.getInstance().getCountFromId(ItemId.BINGE_EATER.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.binge_eater").append(": ").withStyle(style -> style.withColor(ColorManager.SYNERGY))
                    .append(StatManager.LUCK.description(1, Style.EMPTY.withColor(ColorManager.SYNERGY))));
            description.add(StatManager.TEARS_CORRECTION.description(1, Style.EMPTY.withColor(ColorManager.SYNERGY)));
            description.add(StatManager.MOVEMENT_SPEED.description(-0.15, Style.EMPTY.withColor(ColorManager.SYNERGY)));

        }

        return description;
    }

    public void onFoodObtainEffect(Player player, @Nullable ItemStack stack) {
        StatManager.LUCK.apply(player, 1);
        StatManager.TEARS_CORRECTION.apply(player, 1);
        StatManager.MOVEMENT_SPEED.apply(player, -0.15);
    }

    public void onFoodRemoveEffect(Player player, @Nullable ItemStack stack) {
        StatManager.LUCK.apply(player, -1);
        StatManager.TEARS_CORRECTION.apply(player, -1);
        StatManager.MOVEMENT_SPEED.apply(player, 0.15);
    }
}
