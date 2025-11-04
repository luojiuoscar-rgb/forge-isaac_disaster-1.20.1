package net.luojiuoscar.isaac_disaster.block.block_entity.chest;

import net.luojiuoscar.isaac_disaster.helper.PoolHelper;
import net.luojiuoscar.isaac_disaster.item.item.IsaacItem;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import javax.annotation.Nullable;
import java.util.List;

public abstract class ItemChestBlockEntity extends IsaacChestBlockEntity {
    public static final String DEFAULT_ITEM_LOOT_TABLE = LootTableManager.DEFAULT_ITEM_POOL.toString();

    public ItemChestBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
    }

    public float openness = 0.0F;
    public float oOpenness = 0.0F;
    private float rotation;
    public boolean clientShouldBeOpen = false;

    private double itemLootChance = 0.2;
    private boolean locked = false;
    private boolean displayingItem = false;
    private boolean opened = false;
    private String itemLootTable = DEFAULT_ITEM_LOOT_TABLE;

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
    public boolean isOpened(){
        return opened;
    }
    public void setOpened(boolean opened){
        this.opened = opened;
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

    public boolean tryLootItem(ServerLevel serverLevel, Player player, BlockPos pos){
        try {
            if (serverLevel.getRandom().nextDouble() >= getItemLootChance() || opened){
                setOpened(true);
                return false;
            }
            setOpened(true);

            ResourceLocation lootLoc = ResourceLocation.parse(itemLootTable);
            LootTable table = serverLevel.getServer().getLootData().getLootTable(lootLoc);

            LootParams.Builder builder = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.ORIGIN, this.getBlockPos().getCenter())
                    .withOptionalParameter(LootContextParams.THIS_ENTITY, player);

            List<ItemStack> items = table.getRandomItems(builder.create(LootContextParamSets.CHEST));
            ItemStack stack = items.get(0);


            if (!stack.isEmpty() && stack.getItem() instanceof IsaacItem isaacItem){
                stack.setCount(1);
                this.clearContent(); // 如果出道具则清空内容
                this.setItem(0, stack);

                int itemId = isaacItem.getItemId();
                PoolHelper.markAsRemoval(player, lootLoc, itemId); // 移出道具池

                setDisplayingItem(true);
                return true;
            }
        } catch (Exception e) {
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
        tag.putBoolean("locked", locked);
        tag.putBoolean("displayingItem", displayingItem);
        tag.putBoolean("opened", opened);
        tag.putString("itemLootTable", itemLootTable);
        tag.putDouble("itemLootChance", itemLootChance);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("locked")) this.locked = tag.getBoolean("locked");
        if (tag.contains("displayingItem")) this.displayingItem = tag.getBoolean("displayingItem");
        if (tag.contains("opened")) this.opened = tag.getBoolean("opened");
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
        load(tag);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
