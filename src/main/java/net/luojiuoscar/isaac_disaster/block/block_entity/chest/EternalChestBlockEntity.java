package net.luojiuoscar.isaac_disaster.block.block_entity.chest;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.ModBlockEntities;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class EternalChestBlockEntity extends ItemChestBlockEntity {
    public EternalChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ETERNAL_CHEST_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void init(){
        this.setLocked(true);
        this.setItemLootChance(0.25);
        this.setItemLootTable(LootTableManager.POOL_LOCKED_CHEST.toString());
    }

    @Override
    public ResourceLocation getPresetLootTable(){
        return LootTableManager.LOCKED_CHEST;
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

            // 因为掉落物品会清空箱子，所以先弹出箱子内物品
            dropContent();
            this.lootTable = null;
            boolean s = super.lootItem(serverLevel, player, pos, ResourceLocation.parse(getItemLootTable()), this::clearContent);
            if (s){
                setDisplayingItem(true);
                return true;
            }
        } catch (Exception e) {
            IsaacDisaster.LOGGER.error("Failed to generate loot for eternal chests at {} with table {}", worldPosition, getItemLootTable(), e);
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

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("TryLootCount", tryLootCount);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("TryLootCount")) this.tryLootCount = tag.getInt("TryLootCount");
    }
}
