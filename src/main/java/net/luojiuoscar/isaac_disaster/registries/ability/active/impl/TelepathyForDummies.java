package net.luojiuoscar.isaac_disaster.registries.ability.active.impl;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.EffectManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ActiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TelepathyForDummies extends ActiveAbility {

    public TelepathyForDummies(int id, int level) {
        super(id, level);
    }

    @Override
    public void onFirstUse(ServerPlayer player, ItemStack stack, @javax.annotation.Nullable InteractionHand hand){
        StatManager.modifySetWithId(player, ModSetAbility.BOOK.getId(), 1);
    }

    @Override
    public void onTrigger(ServerPlayer player, ItemStack stack, @javax.annotation.Nullable InteractionHand hand) {
        EntityHelper.applyOrStackEffect(player, ModEffects.TELEPATHY.get(), 200, 0, false, true);
    }

    @Override
    public void onTriggerStronger(ServerPlayer player, ItemStack stack, @javax.annotation.Nullable InteractionHand hand){
        EntityHelper.applyOrStackEffect(player, ModEffects.TELEPATHY.get(), 400, 0, false, true);
    }

    @Override
    public void triggerSFX(ServerPlayer player) {
        player.level().playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("item.isaac_disaster.telepathy_for_dummies.lore.1")
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack) {
        List<Component> description = new ArrayList<>();

        description.addAll(ModSetAbility.BOOK.get().getSynergyDesc());

        if (ClientDataManager.getInstance().getCountFromId(ItemId.CAR_BATTERY.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.car_battery").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.increase_duration"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }

        return description;
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack){
        List<Component> description = new ArrayList<>();

        description.addAll(ModSetAbility.BOOK.get().getExtraDesc());
        description.add(EffectManager.TELEPATHY.getExplainDesc());

        return description;
    }
}
