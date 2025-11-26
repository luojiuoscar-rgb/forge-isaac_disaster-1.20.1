package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.event.custom.misc.ItemDisplayAddEvent;
import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.helper.PoolHelper;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.item.item.IsaacItem;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GlitchedCrown extends PassiveAbility {
    public GlitchedCrown(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("item.isaac_disaster.glitched_crown.lore.1")
        );
    }

    public static void addDisplayItem(ItemDisplayAddEvent event){
        ServerLevel level = event.getLevel();
        ResourceLocation tableId = event.getLootTable();
        Player player = event.getPlayer();
        BlockPos pos = event.getPos();

        for (int i = 0; i < 4; i++){
            List<ItemStack> items = LootHelper.generateLoot(level, tableId, new LootParams.Builder(level)
                            .withParameter(LootContextParams.ORIGIN, pos.getCenter())
                            .withOptionalParameter(LootContextParams.THIS_ENTITY, player),
                    LootContextParamSets.EMPTY);

            ItemStack stack = new ItemStack(ModPassiveItems.BREAKFAST.get()); // default
            if (!items.isEmpty()){
                stack = items.get(0);
            }

            if (stack.getItem() instanceof IsaacItem isaacItem){
                event.addDisplayItem(stack);
                PoolHelper.markAsRemoval(player, tableId, isaacItem.getId()); // 移出道具池
            }
        }
    }

}
