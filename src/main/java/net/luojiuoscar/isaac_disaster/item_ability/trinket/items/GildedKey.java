package net.luojiuoscar.isaac_disaster.item_ability.trinket.items;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.trinket.ITrinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.TrinketId;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.ModRecursiveModule;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class GildedKey implements ITrinket {
    @Override
    public int getId() {
        return TrinketId.GILDED_KEY.getId();
    }

    @Override
    public void onFirstEquipped(LivingEntity entity, boolean isEnchanted){
        if (!(entity instanceof Player player)) return;
        PlayerHelper.giveItem(player, ModItems.KEY.get(), 1);
    }

    @Override
    public void onEquipped(LivingEntity entity, boolean isEnchanted) {
        StatManager.addRecursiveModule(entity, ModRecursiveModule.GILDED_KEY.getId(), isEnchanted ? 2 : 1);
    }

    @Override
    public void onUnequipped(LivingEntity entity, boolean isEnchanted) {
        StatManager.addRecursiveModule(entity, ModRecursiveModule.GILDED_KEY.getId(), isEnchanted ? -2 : -1);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(Component.translatable("item.isaac_disaster.gilded_key.lore.1"),
                Component.translatable("item.isaac_disaster.gilded_key.lore.2"),
                Component.translatable("item.isaac_disaster.gilded_key.lore.3"));
    }

    @Override
    public List<Component> getEnchantedDescription() {
        return List.of(Component.translatable("item.isaac_disaster.gilded_key.enchanted.lore.1")
                .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
    }
}
