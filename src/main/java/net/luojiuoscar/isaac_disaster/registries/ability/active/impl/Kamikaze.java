package net.luojiuoscar.isaac_disaster.registries.ability.active.impl;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ActiveAbility;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Kamikaze extends ActiveAbility {
    public Kamikaze(int id, int level) {
        super(id, level);
    }

    @Override
    public void onFirstUse(ServerPlayer player, @Nullable ItemStack stack) {

    }

    @Override
    public void onTrigger(ServerPlayer player, ItemStack stack) {
        explode(player, 4, 35);
    }

    @Override
    public void onTriggerStronger(ServerPlayer player, ItemStack stack){
        explode(player, 7, 80);
    }

    @Override
    public void triggerSFX(ServerPlayer player) {

    }

    private void explode(Player player, float power, float damage){
        LevelHelper.explodeCustom(player, player.position(), power, damage, true);
        player.hurt(player.damageSources().genericKill(), (float) StatManager.MAX_HEALTH.getBonus());
    }


    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("item.isaac_disaster.kamikaze.lore.1")
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
