package net.luojiuoscar.isaac_disaster.registries.ability.active.impl;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ActiveAbility;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.EffectManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.SetManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.SetId;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TheBible extends ActiveAbility {
    public TheBible(int id, int level) {
        super(id, level);
    }

    @Override
    public void onFirstUse(ServerPlayer player, ItemStack stack){
        StatManager.modifySetWithId(player, SetId.BOOK.getId(), 1);
    }

    @Override
    public void onTrigger(ServerPlayer player, ItemStack stack) {
        MobEffectInstance effectInstance = new MobEffectInstance(
                ModEffects.TRANSCENDENCE.get(),
                (int) (StatManager.FLY_TIME.getBonus() * 2),
                0
        );
        player.addEffect(effectInstance, player);
    }

    @Override
    public void onTriggerStronger(ServerPlayer player, ItemStack stack){
        MobEffectInstance effectInstance = new MobEffectInstance(
                ModEffects.TRANSCENDENCE.get(),
                (int) (StatManager.FLY_TIME.getBonus() * 4),
                0
        );
        player.addEffect(effectInstance, player);
    }

    @Override
    public void triggerSFX(ServerPlayer player) {
        player.level().playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("attribute.isaac_disaster.fly_time")
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack) {
        List<Component> description = new ArrayList<>();

        description.addAll(SetManager.getInstance().getSetFromId(SetId.BOOK.getId()).getSynergyDescription());

        if (ClientDataManager.getInstance().getCountFromId(ItemId.CAR_BATTERY.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.car_battery").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.double"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }

        return description;
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack){
        List<Component> description = new ArrayList<>();

        description.addAll(SetManager.getInstance().getSetFromId(SetId.BOOK.getId()).getExplain());
        description.add(EffectManager.TRANSCENDENCE.getExplainDesc());


        return description;
    }
}
