package net.luojiuoscar.isaac_disaster.item_ability.active_item.items;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.active_item.IActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.EffectId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.SetId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.EffectDescriptionManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.SetManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BookOfShadow implements IActiveItem {

    @Override
    public int getItemId() {
        return ItemId.BOOK_OF_SHADOW.getId();
    }

    @Override
    public SoundEvent getSound() {
        return SoundEvents.BOOK_PAGE_TURN;
    }

    @Override
    public void onFirstUse(Player player){
        StatManager.modifySetWithId(player, SetId.BOOK.getId(), 1);
    }

    @Override
    public void onTriggeredEffect(Player player) {
        MobEffectInstance resistance = player.getEffect(MobEffects.DAMAGE_RESISTANCE);
        int duration = 200;
        if (resistance != null && resistance.getAmplifier() >= 4){
            duration += resistance.getDuration();
        }

        MobEffectInstance effectInstance = new MobEffectInstance(
                ModEffects.INVINCIBLE.get(),
                duration,
                0
        );
        player.addEffect(effectInstance, player);
    }

    @Override
    public void onTriggeredEffectStronger(Player player){
        onTriggeredEffect(player);
        onTriggeredEffect(player);
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.BOOK_OF_SHADOW.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.special.invincible", 10),
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


        return description;
    }

    @Override
    public List<Component> getExplain(){
        List<Component> description = new ArrayList<>();

        description.addAll(SetManager.getInstance().getSetFromId(SetId.BOOK.getId()).getExplain());
        description.addAll(EffectDescriptionManager.getInstance().getDescriptionFromId(EffectId.INVINCIBLE.getId()));

        return description;
    }
}
