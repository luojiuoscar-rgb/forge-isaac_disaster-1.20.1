package net.luojiuoscar.isaac_disaster.block.block_entity.chest;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.ModBlockEntities;
import net.luojiuoscar.isaac_disaster.helper.PoolHelper;
import net.luojiuoscar.isaac_disaster.item.item.IsaacItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class EternalChestBlockEntity extends ItemChestBlockEntity {
    public EternalChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ETERNAL_CHEST_BLOCK_ENTITY.get(), pos, state);

        this.setLocked(true);
        this.setItemLootChance(0.25);
    }

    @Override
    public ResourceLocation getLidResourceLocation() {
        return ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/eternal_chest_lid");
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.isaac_disaster.eternal_chest");
    }

    private int tryLootCount = 0;

    @Override
    public boolean tryLootItem(ServerLevel serverLevel, Player player, BlockPos pos){
        try {
            if (isOpened()) return false;
            tryLootCount++;

            // 除首次以外每次打开时箱子内原有的内容会掉出
            if (tryLootCount > 1){
                dropContent();
            }

            if (serverLevel.getRandom().nextDouble() >= getItemLootChance() || tryLootCount == 1){
                unpackLootTable(player);
                setLocked(true);
                return false;
            }
            setOpened(true);
            if (serverLevel.getRandom().nextDouble() < 0.33) return false;

            ResourceLocation lootLoc = ResourceLocation.parse(getItemLootTable());
            LootTable table = serverLevel.getServer().getLootData().getLootTable(lootLoc);

            LootParams.Builder builder = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.ORIGIN, this.getBlockPos().getCenter())
                    .withOptionalParameter(LootContextParams.THIS_ENTITY, player);

            List<ItemStack> items = table.getRandomItems(builder.create(LootContextParamSets.CHEST));
            ItemStack stack = items.get(0);

            if (!stack.isEmpty() && stack.getItem() instanceof IsaacItem isaacItem){
                stack.setCount(1);
                this.dropContent();
                this.lootTable = null; // 不清空lootTable mc会假定箱子是空的从而无法渲染get(0)的item
                this.setItem(0, stack);

                int itemId = isaacItem.getItemId();
                PoolHelper.markAsRemoval(player, lootLoc, itemId);

                setDisplayingItem(true);
                return true;
            }

        } catch (Exception e) {
            setItemLootTable(DEFAULT_ITEM_LOOT_TABLE);
        }
        return false;
    }

    private void dropContent(){
        if(level == null || isOpened()) return;

        SimpleContainer inv = new SimpleContainer(this.getContainerSize());

        for(int i=0; i<this.getContainerSize(); i++){
            inv.setItem(i, this.getItem(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inv);
        this.clearContent();
    }

    @Override
    public void unpackLootTable(@Nullable Player pPlayer) {
        ResourceLocation lt = this.lootTable;
        super.unpackLootTable(pPlayer);
        this.lootTable = lt; // 重设lootTable
    }
}
