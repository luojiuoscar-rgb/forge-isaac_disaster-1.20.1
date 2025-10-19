package net.luojiuoscar.isaac_disaster.item_ability.active_item.items;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TelepathyForDummies implements IActiveItem {

    @Override
    public int getItemId() {
        return ItemId.TELEPATHY_FOR_DUMMIES.getId();
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
        EntityHelper.applyOrStackEffect(player, ModEffects.TELEPATHY.get(), 200, 0);
    }

    @Override
    public void onTriggeredEffectStronger(Player player){
        EntityHelper.applyOrStackEffect(player, ModEffects.TELEPATHY.get(), 400, 0);
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.TELEPATHY_FOR_DUMMIES.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.telepathy_for_dummies.lore.1")
        );
    }

    @Override
    public List<Component> getSynergyDescription() {
        List<Component> description = new ArrayList<>();

        description.add(Component.translatable("set.isaac_disaster.special.header")
                .append(Component.translatable("set.isaac_disaster.book"))
                .append(Component.literal("("+
                        Math.min(3, ClientDataManager.getInstance().getSetCountFromId(SetId.BOOK.getId())) + "/" +
                        SetManager.getInstance().getSetFromId(SetId.BOOK.getId()).getRequireCount()+")"
                )).withStyle(style -> style.withColor(ColorManager.SYNERGY)));

        if (ClientDataManager.getInstance().getCountFromId(ItemId.CAR_BATTERY.getId()) > 0){
            description.add(Component.translatable("item.isaac_disaster.car_battery").append(": ")
                    .append(Component.translatable("item.isaac_disaster.synergy.description.increase_duration"))
                    .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
        }

        return description;
    }

    @Override
    public List<Component> getExplain(){
        List<Component> description = new ArrayList<>();

        description.addAll(SetManager.getInstance().getSetFromId(SetId.BOOK.getId()).getDescription());
        description.addAll(EffectDescriptionManager.getInstance().getDescriptionFromId(EffectId.TELEPATHY.getId()));

        return description;
    }
}
