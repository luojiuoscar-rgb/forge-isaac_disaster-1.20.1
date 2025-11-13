package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id.SetId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.SetManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Synthoil implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.SYNTHOIL.getId();
    }

    @Override
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {
        StatManager.DAMAGE.apply(player, 1);
        StatManager.RANGE.apply(player, 1);
        StatManager.BLOCK_REACH.apply(player, 1);
        StatManager.ENTITY_REACH.apply(player, 1);
        StatManager.modifySetWithId(player, SetId.SPUN.getId(), 1);
    }

    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {
        StatManager.DAMAGE.apply(player, -1);
        StatManager.RANGE.apply(player, -1);
        StatManager.BLOCK_REACH.apply(player, -1);
        StatManager.ENTITY_REACH.apply(player, -1);
        StatManager.modifySetWithId(player, SetId.SPUN.getId(), -1);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                StatManager.DAMAGE.description(1),
                StatManager.RANGE.description(1),
                StatManager.BLOCK_REACH.description(1),
                StatManager.ENTITY_REACH.description(1)
        );
    }

    @Override
    public List<Component> getSynergyDescription(){
        return List.of(
                Component.translatable("set.isaac_disaster.special.header")
                        .append(Component.translatable("set.isaac_disaster.spun"))
                        .append(Component.literal("("+
                                Math.min(3,ClientDataManager.getInstance().getSetCountFromId(SetId.SPUN.getId())) + "/" +
                                SetManager.getInstance().getSetFromId(SetId.SPUN.getId()).getRequireCount()+")"
                        )).withStyle(
                                style -> style.withColor(ColorManager.SYNERGY)
                        )
        );
    }

    @Override
    public List<Component> getExplain(){
        return SetManager.getInstance().getSetFromId(SetId.SPUN.getId()).getExplain();
    }
}
