package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.event.custom.ItemDisplayAddEvent;
import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.item.custom.FoodPassiveItem;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BingeEater implements IPassiveItem {
    private static final ResourceLocation BINGE_EATER_POOL =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "pools/binge_eater");

    @Override
    public int getItemId() {
        return ItemId.BINGE_EATER.getId();
    }

    @Override
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {
        player.setHealth(player.getMaxHealth());
    }

    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {
        StatManager.MAX_HEALTH.apply(player, 1);
    }

    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {
        StatManager.MAX_HEALTH.apply(player, -1);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                StatManager.MAX_HEALTH.description(1),
                Component.translatable("item.isaac_disaster.action.full_health"),
                Component.translatable("item.isaac_disaster.binge_eater.lore.1"),
                Component.translatable("item.isaac_disaster.binge_eater.lore.2"),
                Component.translatable("item.isaac_disaster.binge_eater.lore.3"),
                Component.translatable("item.isaac_disaster.binge_eater.lore.4")
        );
    }

    public static void addDisplayItem(ItemDisplayAddEvent event){
        ServerLevel level = event.getLevel();
        Player player = event.getPlayer();
        BlockPos pos = event.getPos();

        List<ItemStack> items = LootHelper.generateLoot(level, BINGE_EATER_POOL, new LootParams.Builder(level)
                        .withParameter(LootContextParams.ORIGIN, pos.getCenter())
                        .withOptionalParameter(LootContextParams.THIS_ENTITY, player),
                LootContextParamSets.EMPTY);

        ItemStack stack = new ItemStack(ModItems.BREAKFAST.get()); // default
        if (!items.isEmpty()){
            stack = items.get(0);
        }

        if (stack.getItem() instanceof FoodPassiveItem){
            event.addDisplayItem(stack);
        }
    }

}
