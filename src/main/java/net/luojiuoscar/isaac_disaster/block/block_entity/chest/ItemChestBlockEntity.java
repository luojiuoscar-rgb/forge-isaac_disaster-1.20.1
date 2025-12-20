package net.luojiuoscar.isaac_disaster.block.block_entity.chest;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.block_entity.misc.ItemDisplayContainerBlockEntity;
import net.luojiuoscar.isaac_disaster.capability.misc.DisplayItemListCap;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;


public abstract class ItemChestBlockEntity extends IsaacChestBlockEntity implements ItemDisplayContainerBlockEntity {
    public static final String DEFAULT_ITEM_LOOT_TABLE = LootTableManager.DEFAULT_ITEM_POOL.toString();

    public ItemChestBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
        init();
    }

    abstract void init();

    public float openness = 0.0F;
    public float oOpenness = 0.0F;
    private float rotation;
    public boolean clientShouldBeOpen = false;

    private double itemLootChance = -1;
    private String itemLootTable = "";

    private boolean locked;
    private boolean displayingItem = false;


    public String getItemLootTable(){
        return itemLootTable;
    }
    public void setItemLootTable(String itemLootTable){
        this.itemLootTable = itemLootTable;
    }


    public float getRenderingRotation(){
        rotation += 0.5f;
        if (rotation >= 360) rotation = 0;
        return rotation;
    }

    public boolean isLocked() {return locked;}
    public void setLocked(boolean locked){
        this.locked = locked;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean isDisplayingItem() {
        return this.displayingItem;
    }
    public void setDisplayingItem(boolean displayingItem){
        this.displayingItem = displayingItem;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public double getItemLootChance(){
        return itemLootChance;
    }
    public void setItemLootChance(double c){
        this.itemLootChance = c;
    }

    @Override
    public boolean tryLootItem(ServerLevel serverLevel, Player player, BlockPos pos){
        try {
            if (serverLevel.getRandom().nextDouble() >= getItemLootChance() || opened){
                setOpened(true);
                return false;
            }
            setOpened(true);


            boolean spawned = lootItem(serverLevel, player, pos,
                    ResourceLocation.parse(itemLootTable), this::clearContent);
            if (spawned){
                setDisplayingItem(true);
                return true;
            }
        } catch (Exception e) {
            IsaacDisaster.LOGGER.error("Failed to generate loot for chests at {} with table {}", worldPosition, itemLootTable, e);
            itemLootTable = DEFAULT_ITEM_LOOT_TABLE;
        }
        return false;
    }


    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == 1) {
            this.clientShouldBeOpen = param > 0;
            return true;
        }
        return super.triggerEvent(id, param);
    }


    public static <T extends ItemChestBlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        blockEntity.tickRotate(level, level.getGameTime(), () -> {
            if (!blockEntity.getItemDisplayList().isEmpty()) {
                blockEntity.setItem(0, blockEntity.getCurrentItemDisplay());
                blockEntity.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        });

        // 开合动画
        if (level.isClientSide) {
            blockEntity.oOpenness = blockEntity.openness;

            if (blockEntity.isDisplayingItem() || blockEntity.clientShouldBeOpen) {
                blockEntity.openness = Math.min(blockEntity.openness + 0.1F, 1.0f);
            }else{
                blockEntity.openness = Math.max(blockEntity.openness - 0.1F, 0.0f);
            }
        }
    }

    @Override
    public float getOpenNess(float pPartialTicks) {
        return Mth.lerp(pPartialTicks, this.oOpenness, this.openness);
    }

    // 原版lootTable逻辑
    @Override
    public void unpackLootTable(@Nullable Player player) {
        if (locked || opened){ // 上锁的/已被开启的则不再触发掉落
            return;
        }
        super.unpackLootTable(player);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        saveItemDisplayCap(tag);
        tag.putBoolean("locked", locked);
        tag.putBoolean("displayingItem", displayingItem);
        tag.putString("itemLootTable", itemLootTable);
        tag.putDouble("itemLootChance", itemLootChance);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadItemDisplayCap(tag);
        if (tag.contains("locked")) this.locked = tag.getBoolean("locked");
        if (tag.contains("displayingItem")) this.displayingItem = tag.getBoolean("displayingItem");
        if (tag.contains("itemLootTable")) this.itemLootTable = tag.getString("itemLootTable");
        if (tag.contains("itemLootChance")) this.itemLootChance = tag.getDouble("itemLootChance");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }


    @Override
    public void clearContent(){
        clearItemDisplayList();
        super.clearContent();
    }

    @Override
    public void itemRollFromPlayer(Player player) {
        if (this.level == null || this.level.isClientSide) return;

        this.clearContent();

        boolean success = this.lootItem(
                (ServerLevel) level,
                player,
                getBlockPos(),
                ResourceLocation.parse(itemLootTable),
                this::clearContent
        );

        if (success) {
            // 立刻同步
            this.setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private final DisplayItemListCap displayItemListCap = new DisplayItemListCap();

    @Override
    public DisplayItemListCap getItemDisplayCap() {
        return displayItemListCap;
    }

    @Override
    public void setItemDisplay(ItemStack stack){
        this.setItem(0, stack);
    }

}
