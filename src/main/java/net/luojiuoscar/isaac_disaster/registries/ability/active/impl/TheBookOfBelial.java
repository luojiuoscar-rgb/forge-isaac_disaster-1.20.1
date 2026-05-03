package net.luojiuoscar.isaac_disaster.registries.ability.active.impl;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.DescriptionHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ActiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbility;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TheBookOfBelial extends ActiveAbility {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModExecutableEffects.THE_BOOK_OF_BELIAL)
    ));

    public TheBookOfBelial(int id, int level) {
        super(TRIGGER, id, level);
    }

    @Override
    public void onFirstUse(ServerPlayer player, ItemStack stack, @Nullable InteractionHand hand){
        StatManager.modifySetWithId(player, ModSetAbility.BOOK.getId(), 1);
    }

    @Override
    public void triggerSFX(ServerPlayer player) {
        player.level().playSound(null, player.blockPosition(), ModSounds.THE_BOOK_OF_BELIAL_USE.get(), SoundSource.PLAYERS);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("item.isaac_disaster.the_book_of_belial.lore.1"),
                Component.translatable("item.isaac_disaster.the_book_of_belial.lore.2"),
                Component.translatable("item.isaac_disaster.special.stackable")
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack) {
        List<Component> description = new ArrayList<>();

        description.addAll(ModSetAbility.BOOK.get().getSynergyDesc());

        if (ClientDataManager.getInstance().getCountFromId(ItemId.CAR_BATTERY.getId()) > 0){
            description.add(DescriptionHelper.getSynergyDesc(
                    Component.translatable("item.isaac_disaster.car_battery"),
                    Component.translatable("item.isaac_disaster.synergy.description.double")
            ));
        }
        if (ClientDataManager.getInstance().getCountFromId(ItemId.BLOOD_OF_THE_MARTYR.getId()) > 0){
            description.add(DescriptionHelper.getSynergyDesc(
                    Component.translatable("item.isaac_disaster.blood_of_the_martyr"),
                    Component.translatable("item.isaac_disaster.blood_of_the_martyr.synergy.lore.1")
            ));
        }
        return description;
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack){
        List<Component> description = new ArrayList<>();

        description.addAll(ModSetAbility.BOOK.get().getExtraDesc());

        description.add(Component.translatable("effect.isaac_disaster.power_of_belial").append(": ")
                .append(StatManager.DAMAGE_MULTIPLY_BASE.description(0.5)));

        return description;
    }
}
