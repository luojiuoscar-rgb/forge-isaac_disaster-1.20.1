package net.luojiuoscar.isaac_disaster.item_ability.active_item.items;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.active_item.IActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.SetId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.SetManager;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TheBookOfBelial implements IActiveItem {

    @Override
    public int getItemId() {
        return ItemId.THE_BOOK_OF_BELIAL.getId();
    }

    @Override
    public SoundEvent getSound() {
        return ModSounds.THE_BOOK_OF_BELIAL_USE.get();
    }

    @Override
    public void onFirstUse(Player player){
        StatManager.modifySetWithId(player, SetId.BOOK.getId(), 1);
    }

    @Override
    public void onTriggeredEffect(Player player) {
        EntityHelper.applyOrStackEffect(player, ModEffects.POWER_OF_BELIAL.get(), 240, 0, false, true);
    }

    @Override
    public void onTriggeredEffectStronger(Player player){
        onTriggeredEffect(player);
        onTriggeredEffect(player);
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.THE_BOOK_OF_BELIAL.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.the_book_of_belial.lore.1"),
                Component.translatable("item.isaac_disaster.the_book_of_belial.lore.2"),
                Component.translatable("item.isaac_disaster.special.stackable")
        );
    }

    @Override
    public List<Component> getSynergyDescription() {
        List<Component> description = new ArrayList<>();

        description.addAll(SetManager.getInstance().getSetFromId(SetId.BOOK.getId()).getSynergyDescription());

        if (ClientDataManager.getInstance().getCountFromId(ItemId.CAR_BATTERY.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.car_battery").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.double"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }
        if (ClientDataManager.getInstance().getCountFromId(ItemId.BLOOD_OF_THE_MARTYR.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.blood_of_the_martyr").append(": ")
                    .append(Component.translatable("item.isaac_disaster.blood_of_the_martyr.synergy.lore.1"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }
        return description;
    }

    @Override
    public List<Component> getExplain(){
        List<Component> description = new ArrayList<>();

        description.addAll(SetManager.getInstance().getSetFromId(SetId.BOOK.getId()).getExplain());

        description.add(Component.translatable("effect.isaac_disaster.power_of_belial").append(": ")
                .append(StatManager.DAMAGE_MULTIPLY_BASE.description(0.5)));

        return description;
    }
}
