package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

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
    public void onFirstObtain(Player player) {
        player.setHealth(player.getMaxHealth());
    }

    @Override
    public void onObtain(Player player) {
        StatManager.MAX_HEALTH.apply(player, 1);
        StatManager.SCALE.apply(player, 2.5);
        StatManager.MOVEMENT_SPEED.apply(player, 1.5);
        StatManager.DAMAGE.apply(player, 0.25);
        StatManager.DAMAGE_MULTIPLY_BASE.apply(player, 0.5);
        StatManager.RANGE.apply(player, 1);
        StatManager.BLOCK_REACH.apply(player, 1);
        StatManager.ENTITY_REACH.apply(player, 1);
        StatManager.modifySetWithId(player, SetId.FUN_GUY.getId(), 1);
    }

    @Override
    public void onRemove(Player player) {
        StatManager.MAX_HEALTH.apply(player, -1);
        StatManager.SCALE.apply(player, -2.5);
        StatManager.MOVEMENT_SPEED.apply(player, -1.5);
        StatManager.DAMAGE.apply(player, -0.25);
        StatManager.DAMAGE_MULTIPLY_BASE.apply(player, -0.5);
        StatManager.RANGE.apply(player, -1);
        StatManager.BLOCK_REACH.apply(player, -1);
        StatManager.ENTITY_REACH.apply(player, -1);
        StatManager.modifySetWithId(player, SetId.FUN_GUY.getId(), -1);
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.MAGIC_MUSHROOM.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("attribute.isaac_disaster.scale_up"),
                StatManager.MAX_HEALTH.description(1),
                Component.translatable("item.isaac_disaster.action.full_health"),
                StatManager.MOVEMENT_SPEED.description(1.5),
                StatManager.DAMAGE.description(0.25),
                StatManager.DAMAGE_MULTIPLY_BASE.description(0.5),
                StatManager.RANGE.description(1),
                StatManager.BLOCK_REACH.description(1),
                StatManager.ENTITY_REACH.description(1)
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
