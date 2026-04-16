package net.luojiuoscar.isaac_disaster.block.block_entity;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.ModBlockEntities;
import net.luojiuoscar.isaac_disaster.block.block_entity.misc.ItemDisplayContainerBlockEntity;
import net.luojiuoscar.isaac_disaster.capability.misc.DisplayItemListCap;
import net.luojiuoscar.isaac_disaster.commands.gamerule.ModGameRules;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.manager.data.BlockData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class PedestalBlockEntity extends BlockEntity implements ItemDisplayContainerBlockEntity {
    public final ItemStackHandler inventory = new ItemStackHandler(1){
        @Override
        protected int getStackLimit(int slot, @NotNull ItemStack stack) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot){
            if (level == null) return;
            setChanged();
            if (!level.isClientSide()){
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private float rotation;
    private final Set<BlockPos> linkedOffsets = new HashSet<>();
    private boolean isDecoration = true;
    private String itemLootTable = "";
    private boolean generated = false;
    private boolean locked = false;
    private int lifeCost = 0;
    private int moneyCost = 0;



    public PedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PEDESTAL_BLOCK_ENTITY.get(), pos, state);
    }

    public void copyFromOriginal(PedestalBlockEntity original){
        this.isDecoration = original.isDecoration();
        this.itemLootTable = original.getItemLootTable();
        this.locked = original.isLocked();
        setChanged();
    }


    public float getRenderingRotation(){
        rotation += 0.5f;
        if (rotation >= 360) rotation = 0;
        return rotation;
    }
    public boolean isGenerated() {return generated; }
    public void setGenerated(boolean generated) {
        this.generated = generated;
        setChanged();
    }

    public boolean isDecoration(){ return isDecoration; }
    public void setDecoration(boolean isDecoration){
        this.isDecoration = isDecoration;
        setChanged();
    }

    public boolean isLocked() {return locked; }
    public void setLocked(boolean locked){
        this.locked = locked;
        setChanged();
    }

    public String getItemLootTable() { return itemLootTable; }
    public void setItemLootTable(String itemLootTable) {
        this.itemLootTable = itemLootTable;
        setChanged();
    }

    public int getLifeCost() {return lifeCost; }
    public void setLifeCost(int c) {
        this.lifeCost = c;
        setChanged();
    }

    public int getMoneyCost() {return moneyCost; }
    public void setMoneyCost(int c) {
        this.moneyCost = c;
        setChanged();
    }

    /** 返回绝对坐标集合 */
    public Set<BlockPos> getLinkedPedestals() {
        Set<BlockPos> realPos = new HashSet<>();
        for (BlockPos offset : linkedOffsets){
            realPos.add(this.worldPosition.offset(offset));
        }
        if(realPos.isEmpty()) realPos.add(this.worldPosition);
        return realPos;
    }

    /** 设置绝对坐标集合，内部转为偏移存储 */
    public void setLinkedPedestals(Set<BlockPos> absolutePos){
        linkedOffsets.clear();
        for(BlockPos pos : absolutePos){
            linkedOffsets.add(pos.subtract(this.worldPosition));
        }
        isDecoration = false;
        setChanged();
    }

    public void addLinkedPedestals(BlockPos pos){
        linkedOffsets.add(pos);
        setChanged();
    }

    public void clearLinkedPedestals(){ linkedOffsets.clear(); }

    public static void linkPedestals(BlockPos originalPos, BlockPos pos, ServerLevel level){
        BlockEntity originalBe = level.getBlockEntity(originalPos);
        BlockEntity be = level.getBlockEntity(pos);
        boolean locked = false;

        if (originalBe instanceof PedestalBlockEntity originalPedestal &&
                be instanceof PedestalBlockEntity pedestal) {

            Set<BlockPos> allLinked = new HashSet<>();
            allLinked.addAll(originalPedestal.getLinkedPedestals());
            allLinked.addAll(pedestal.getLinkedPedestals());

            // 连接底座
            for (BlockPos p : allLinked){
                if (level.getBlockEntity(p) instanceof PedestalBlockEntity pbe){
                    locked = pbe.isLocked() || locked; // 记录上锁状态
                    pbe.setLinkedPedestals(allLinked);
                    level.sendBlockUpdated(p, pbe.getBlockState(), pbe.getBlockState(), 3);
                }
            }

            // 设置上锁状态
            for (BlockPos p : allLinked){
                if (level.getBlockEntity(p) instanceof PedestalBlockEntity pbe){
                    pbe.setLocked(locked);
                }
            }
        }
    }

    public void unlockAll(){
        if (level == null) return;
        // 同时解锁全部链接的底座
        Set<BlockPos> allLinked = new HashSet<>(getLinkedPedestals());
        for (BlockPos pos : allLinked){
            if (level.getBlockEntity(pos) instanceof PedestalBlockEntity be){
                be.setLocked(false);
                level.sendBlockUpdated(pos, be.getBlockState(), be.getBlockState(), 3); // update
            }
        }
    }

    public void clearContent(){
        clearItemDisplayList();
        inventory.setStackInSlot(0, ItemStack.EMPTY);
    }

    public void drops(){
        if(level == null || !isDecoration || isLocked()) return;
        SimpleContainer inv = new SimpleContainer(inventory.getSlots());
        for(int i=0; i<inventory.getSlots(); i++) {
            inv.setItem(i, inventory.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    public ItemStack getItem(){ return inventory.getStackInSlot(0); }
    public void setItem(ItemStack stack){ inventory.setStackInSlot(0, stack); }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level == null || level.isClientSide) return;

        BlockData manager = BlockData.get((ServerLevel) level);
        manager.addPedestal(worldPosition);
    }

    public static <T extends PedestalBlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (level.isClientSide) return;
        if (blockEntity.isDecoration()) return;

        // 道具轮播
        blockEntity.tickRotate(level, level.getGameTime(), () -> {
            if (!blockEntity.getItemDisplayList().isEmpty()) {
                blockEntity.setItem(blockEntity.getCurrentItemDisplay());
            }
        });

        if (blockEntity.isGenerated() || level.getGameRules().getBoolean(ModGameRules.DISABLE_PLACEHOLDER)) return;

        int range = 5;
        Player player = LevelHelper.findNearestOfType(level, pos.getCenter(), range, Player.class,
                e -> e instanceof Player p && !p.isCreative() && !p.isSpectator()); // 排除创造玩家和观察者

        if (player == null) return;

        blockEntity.tryLootItem((ServerLevel) level, player, pos);
        blockEntity.setGenerated(true);
    }

    @Override
    public boolean tryLootItem(ServerLevel serverLevel, Player player, BlockPos pos) {
        try {
            return lootItem(serverLevel, player, pos, ResourceLocation.parse(itemLootTable), this::clearContent);
        } catch (Exception e) {
            IsaacDisaster.LOGGER.error("Failed to generate loot for pedestal at {} with table {}", worldPosition, itemLootTable, e);
            itemLootTable = "";
            isDecoration = true;
            generated = true;
        }
        return false;
    }

    @Override
    public void setItemDisplay(ItemStack stack) {
        setItem(stack);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag){
        super.saveAdditional(tag);
        saveItemDisplayCap(tag);

        tag.put("inventory", inventory.serializeNBT());
        tag.putBoolean("isDecoration", isDecoration);
        tag.putBoolean("locked", locked);
        tag.putBoolean("generated", generated);
        tag.putInt("lifeCost", lifeCost);
        tag.putInt("moneyCost", moneyCost);

        if (!itemLootTable.isEmpty())
            tag.putString("itemLootTable", itemLootTable);

        ListTag listTag = new ListTag();
        for(BlockPos offset : linkedOffsets){
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", offset.getX());
            posTag.putInt("y", offset.getY());
            posTag.putInt("z", offset.getZ());
            listTag.add(posTag);
        }
        tag.put("linkedOffsets", listTag);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        loadItemDisplayCap(tag);

        if (tag.contains("inventory")) inventory.deserializeNBT(tag.getCompound("inventory"));
        if (tag.contains("isDecoration")) isDecoration = tag.getBoolean("isDecoration");
        if (tag.contains("locked")) locked = tag.getBoolean("locked");
        if (tag.contains("generated")) generated = tag.getBoolean("generated");
        if (tag.contains("lifeCost")) lifeCost = tag.getInt("lifeCost");
        if (tag.contains("moneyCost")) moneyCost = tag.getInt("moneyCost");
        if (tag.contains("itemLootTable")) itemLootTable = tag.getString("itemLootTable");

        // linkedOffsets
        if (tag.contains("linkedOffsets")) {
            linkedOffsets.clear();
            ListTag listTag = tag.getList("linkedOffsets", 10);
            for (Tag t : listTag) {
                CompoundTag posTag = (CompoundTag) t;
                linkedOffsets.add(new BlockPos(
                        posTag.getInt("x"),
                        posTag.getInt("y"),
                        posTag.getInt("z")));
            }
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket(){ return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public @NotNull CompoundTag getUpdateTag(){ return saveWithoutMetadata(); }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt){ handleUpdateTag(pkt.getTag()); }

    @Override
    public void itemRollFromPlayer(Player player) {
        if (this.level == null || this.level.isClientSide) return;

        this.clearContent();
        this.tryLootItem((ServerLevel) level, player, getBlockPos());
    }


    // ====== 道具轮播 ======
    private final DisplayItemListCap displayItemListCap = new DisplayItemListCap();

    @Override
    public DisplayItemListCap getItemDisplayCap() {
        return displayItemListCap;
    }

}
