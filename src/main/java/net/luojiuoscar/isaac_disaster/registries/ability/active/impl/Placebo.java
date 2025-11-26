package net.luojiuoscar.isaac_disaster.registries.ability.active.impl;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.item.pickup.Pill;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ActiveAbility;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Placebo extends ActiveAbility {
    public Placebo(int id, int level) {
        super(id, level);
    }

    @Override
    public void onFirstUse(ServerPlayer player, @Nullable ItemStack stack) {

    }

    @Override
    public void onTrigger(ServerPlayer player, ItemStack stack) {
        ItemStack pill = player.getOffhandItem();
        if (!(pill.getItem() instanceof Pill item)) return;
        // 根据类型触发效果
        if (item.isHorsePill()){
            PillEffectManager.getInstance().getEffectFromPill(item.getPillId()).onUseH(player);
        }else{
            PillEffectManager.getInstance().getEffectFromPill(item.getPillId()).onUse(player);
        }
    }

    @Override
    public void onTriggerStronger(ServerPlayer player, ItemStack stack){
        ItemStack pill = player.getOffhandItem();
        if (!(pill.getItem() instanceof Pill item)) return;
        PillEffectManager.getInstance().getEffectFromPill(item.getPillId()).onUseH(player);
    }

    @Override
    public void triggerSFX(ServerPlayer player) {

    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("item.isaac_disaster.placebo.lore.1"),
                Component.translatable("item.isaac_disaster.placebo.lore.2")
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack) {
        List<Component> description = new ArrayList<>();

        if (ClientDataManager.getInstance().getCountFromId(ItemId.CAR_BATTERY.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.car_battery").append(": ")
                    .append(Component.translatable("item.isaac_disaster.placebo.synergy.car_battery.1"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }
        return description;
    }
}
