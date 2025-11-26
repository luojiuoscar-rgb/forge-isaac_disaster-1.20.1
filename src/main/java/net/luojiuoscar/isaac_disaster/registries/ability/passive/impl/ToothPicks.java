package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ToothPicks extends PassiveAbility {
    public ToothPicks(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.TEARS.apply(player, 1);
        StatManager.BULLET_SPEED.apply(player, 0.8);
        StatManager.ATTACK_SPEED.apply(player, 1);
        StatManager.ATTACK_KNOCKBACK.apply(player, 0.5);
        StatManager.addBulletColor(player, ModBulletColors.BLOOD_TEAR.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.TEARS.apply(player, -1);
        StatManager.BULLET_SPEED.apply(player, -0.8);
        StatManager.ATTACK_SPEED.apply(player, -1);
        StatManager.ATTACK_KNOCKBACK.apply(player, -0.5);
        StatManager.addBulletColor(player, ModBulletColors.BLOOD_TEAR.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                StatManager.TEARS.description(1),
                StatManager.BULLET_SPEED.description(-0.8),
                StatManager.ATTACK_SPEED.description(1),
                StatManager.ATTACK_KNOCKBACK.description(0.5)
        );
    }


    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack) {
        List<Component> description = new ArrayList<>();

        if (ClientDataManager.getInstance().getCountFromId(ItemId.BINGE_EATER.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.binge_eater").append(": ").withStyle(style -> style.withColor(ColorManager.SYNERGY))
                    .append(StatManager.BULLET_SPEED.description(1, Style.EMPTY.withColor(ColorManager.SYNERGY))));
            description.add(StatManager.TEARS.description(0.7, Style.EMPTY.withColor(ColorManager.SYNERGY)));
            description.add(StatManager.MOVEMENT_SPEED.description(-0.15, Style.EMPTY.withColor(ColorManager.SYNERGY)));

        }

        return description;
    }
}
