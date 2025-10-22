package net.luojiuoscar.isaac_disaster.item_ability.trinket.items;

import net.luojiuoscar.isaac_disaster.helper.TextHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.trinket.ITrinket;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.TrinketId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class LuckyToe implements ITrinket {
    @Override
    public int getId() {
        return TrinketId.LUCKY_TOE.getId();
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.LUCKY_TOE.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(TextHelper.formatAttribute("item.isaac_disaster.attribute.luck", StatManager.getLuckBonus()));
    }

    @Override
    public List<Component> getEnchantedDescription() {
        return List.of(Component.translatable("item.isaac_disaster.synergy.description.double")
                .withStyle(style -> style.withColor(ColorManager.SYNERGY)));
    }

    @Override
    public void onEquipped(LivingEntity entity, boolean isEnchanted){
        if (!(entity instanceof Player player)) return;
        if (isEnchanted) {
            StatManager.modifyLuckAdder(player, 2);
        } else {
            StatManager.modifyLuckAdder(player, 1);
        }

    };
    @Override
    public void onUnequipped(LivingEntity entity, boolean isEnchanted){
        if (!(entity instanceof Player player)) return;
        if (isEnchanted) {
            StatManager.modifyLuckAdder(player, -2);
        } else {
            StatManager.modifyLuckAdder(player, -1);
        }
    };

}
