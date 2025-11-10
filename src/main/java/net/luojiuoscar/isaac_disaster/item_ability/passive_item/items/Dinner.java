package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IFoodPassiveItem;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
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

public class Dinner implements IPassiveItem, IFoodPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.DINNER.getId();
    }

    @Override
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {
        StatManager.healHealth(player, 1.0f);
        if (PlayerHelper.hasItem(ItemId.BINGE_EATER.getId(), (ServerPlayer) player) && stack != null){
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 1));
        }
    }

    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {
        StatManager.MAX_HEALTH.apply(player, 1);
        onFoodObtain(player, stack);
    }

    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {
        StatManager.MAX_HEALTH.apply(player, -1);
        onFoodRemove(player, stack);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                StatManager.MAX_HEALTH.description(1)
        );
    }


    @Override
    public List<Component> getSynergyDescription() {
        List<Component> description = new ArrayList<>();

        if (ClientDataManager.getInstance().getCountFromId(ItemId.BINGE_EATER.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.binge_eater").append(": ").withStyle(style -> style.withColor(ColorManager.SYNERGY))
                    .append(StatManager.BULLET_SPEED.description(1, Style.EMPTY.withColor(ColorManager.SYNERGY))));
            description.add(StatManager.TEARS.description(0.7, Style.EMPTY.withColor(ColorManager.SYNERGY)));
            description.add(StatManager.MOVEMENT_SPEED.description(-0.15, Style.EMPTY.withColor(ColorManager.SYNERGY)));

        }

        return description;
    }

    @Override
    public void onFoodObtainEffect(Player player, @Nullable ItemStack stack) {
        StatManager.BULLET_SPEED.apply(player, 1);
        StatManager.TEARS.apply(player, 0.7);
        StatManager.MOVEMENT_SPEED.apply(player, -0.15);
    }

    @Override
    public void onFoodRemoveEffect(Player player, @Nullable ItemStack stack) {
        StatManager.BULLET_SPEED.apply(player, -1);
        StatManager.TEARS.apply(player, -0.7);
        StatManager.MOVEMENT_SPEED.apply(player, 0.15);
    }
}
