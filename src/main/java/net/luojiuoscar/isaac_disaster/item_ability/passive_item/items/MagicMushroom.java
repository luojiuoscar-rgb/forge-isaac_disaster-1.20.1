package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.TextHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.SetId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.SetManager;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MagicMushroom implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.MAGIC_MUSHROOM.getId();
    }

    @Override
    public void onObtainSound(Player player) {
        SoundEvent defaultSound = ModSounds.MAGIC_MUSHROOM_OBTAIN.get();
        player.playSound(defaultSound, 1.0F, 1.0F);
    }

    @Override
    public void onObtain(Player player) {

    }

    @Override
    public void onDirectObtain(Player player) {
        StatManager.modifyMaxHealth(player, 1);
        player.setHealth(player.getMaxHealth());
        StatManager.modifyScaleAdder(player, 2.5);
        StatManager.modifyMovementSpeedAdder(player, 1.5);
        StatManager.modifyDamageAdder(player, 0.25);
        StatManager.modifyDamageMultiplier(player, StatManager.getDamageMultiplier1());
        StatManager.modifyRangeAdder(player, 1);
        StatManager.modifyBlockReachAdder(player, 1);
        StatManager.modifyEntityReachAdder(player, 1);
    }

    @Override
    public void onRemove(Player player) {
        StatManager.modifyMaxHealth(player, -1);
        StatManager.modifyScaleAdder(player, -2.5);
        StatManager.modifyMovementSpeedAdder(player, -1.5);
        StatManager.modifyDamageAdder(player, -0.25);
        StatManager.modifyDamageMultiplier(player, -StatManager.getDamageMultiplier1());
        StatManager.modifyRangeAdder(player, -1);
        StatManager.modifyBlockReachAdder(player, -1);
        StatManager.modifyEntityReachAdder(player, -1);
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.MAGIC_MUSHROOM.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.attribute.scale_up"),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.health", StatManager.getHealthBonus()),
                Component.translatable("item.isaac_disaster.action.full_health"),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.movement_speed",1500*StatManager.getMovementSpeedBonus()),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.damage", 0.25*StatManager.getDamageBonus()),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.damage_multiplier", 100*StatManager.getDamageMultiplier1()),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.bullet_range", StatManager.getRangeBonus()),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.block_reach", StatManager.getBlockReachBonus()),
                TextHelper.formatAttribute("item.isaac_disaster.attribute.entity_reach", StatManager.getEntityReachBonus())
        );
    }

    @Override
    public List<Component> getSynergyDescription(){
        return SetManager.getInstance().getSetFromId(SetId.FUN_GUY.getId()).getSynergyDescription();
    }

    @Override
    public List<Component> getExplain(){
        return SetManager.getInstance().getSetFromId(SetId.FUN_GUY.getId()).getExplain();
    }
}
