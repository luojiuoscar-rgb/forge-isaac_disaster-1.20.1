package net.luojiuoscar.isaac_disaster.block.block_entity;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.ModBlockEntities;
import net.luojiuoscar.isaac_disaster.block.block_entity.misc.ItemDisplayContainerBlockEntity;
import net.luojiuoscar.isaac_disaster.block.custom.ItemDisplayContainerBlock;
import net.luojiuoscar.isaac_disaster.capability.misc.DisplayItemListCap;
import net.luojiuoscar.isaac_disaster.commands.gamerule.ModGameRules;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.manager.data.BlockData;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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

public class PedestalBlockEntity extends BlockEntity
        implements ItemDisplayContainerBlockEntity {
    public enum ContentSource {
        MANUAL,
        LOOT_TABLE
    }

    public enum GenerationState {
        NOT_CONFIGURED,
        READY,
        FAILED_RETRYABLE,
        GENERATED
    }

    public enum BreakPolicy {
        DROP_CONTENT,
        DISCARD_CONTENT
    }

    private final ItemStackHandler inventory = new ItemStackHandler(1){
        @Override
        protected int getStackLimit(int slot, @NotNull ItemStack stack) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot){
            updateItemIndex(getStackInSlot(slot));
            syncChanges();
        }
    };

    private float rotation;
    private final Set<BlockPos> linkedOffsets = new HashSet<>();
    private ContentSource contentSource = ContentSource.MANUAL;
    private String itemLootTable = "";
    private boolean generated = false;
    private boolean generationFailed = false;
    private boolean locked = false;
    private PedestalPrice price = PedestalPrice.free();
    private BreakPolicy breakPolicy = BreakPolicy.DROP_CONTENT;
    private boolean autoUseOnAcquire;
    private long nextGenerationAttemptTick;

    private void syncChanges() {
        setChanged();
        if (level instanceof ServerLevel) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public BreakPolicy getBreakPolicy() { return breakPolicy; }

    public void setBreakPolicy(BreakPolicy policy) {
        if (policy == null) {
            IsaacDisaster.LOGGER.warn("Null break policy for pedestal at {}; using DROP_CONTENT", worldPosition);
            policy = BreakPolicy.DROP_CONTENT;
        }
        breakPolicy = policy;
        syncChanges();
    }

    public boolean isAutoUseOnAcquire() { return autoUseOnAcquire; }

    public void setAutoUseOnAcquire(boolean autoUse) {
        autoUseOnAcquire = autoUse;
        syncChanges();
    }



    public PedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PEDESTAL_BLOCK_ENTITY.get(), pos, state);
    }

    /** Copies configuration for a fresh pedestal roll, excluding items, generation progress, and links. */
    public void copyFromOriginal(PedestalBlockEntity original){
        if (original == null) {
            IsaacDisaster.LOGGER.warn("Null source when copying pedestal configuration at {}; skipping copy", worldPosition);
            return;
        }
        this.contentSource = original.contentSource;
        this.itemLootTable = original.getItemLootTable();
        this.locked = original.isLocked();
        this.price = original.getPrice();
        this.breakPolicy = original.breakPolicy;
        this.autoUseOnAcquire = original.autoUseOnAcquire;
        this.generated = false;
        this.generationFailed = false;
        this.nextGenerationAttemptTick = 0;
        clearContent();
        clearLinkedPedestals();
        syncChanges();
    }


    public float getRenderingRotation(){
        rotation += 0.5f;
        if (rotation >= 360) rotation = 0;
        return rotation;
    }
    public boolean isGenerated() {return generated; }
    public void setGenerated(boolean generated) {
        this.generated = generated;
        this.generationFailed = false;
        this.nextGenerationAttemptTick = 0;
        syncChanges();
    }

    public ContentSource getContentSource() {
        return contentSource;
    }

    public void setContentSource(ContentSource source) {
        if (source == null) {
            IsaacDisaster.LOGGER.warn("Null content source for pedestal at {}; using MANUAL", worldPosition);
        }
        ContentSource next = source == null ? ContentSource.MANUAL : source;
        if (this.contentSource != next) {
            this.generated = false;
            this.generationFailed = false;
            this.nextGenerationAttemptTick = 0;
        }
        this.contentSource = next;
        syncChanges();
    }

    public GenerationState getGenerationState() {
        if (contentSource != ContentSource.LOOT_TABLE || itemLootTable.isEmpty()) {
            return GenerationState.NOT_CONFIGURED;
        }
        if (generated) return GenerationState.GENERATED;
        return generationFailed ? GenerationState.FAILED_RETRYABLE : GenerationState.READY;
    }

    public PedestalPrice getPrice() {
        return price;
    }

    public void setPrice(PedestalPrice price) {
        if (price == null) {
            IsaacDisaster.LOGGER.warn("Null price for pedestal at {}; using FREE", worldPosition);
        }
        this.price = price == null ? PedestalPrice.free() : price;
        syncChanges();
    }

    public boolean isLocked() {return locked; }
    public void setLocked(boolean locked){
        this.locked = locked;
        syncChanges();
    }

    public String getItemLootTable() { return itemLootTable; }
    public void setItemLootTable(String itemLootTable) {
        if (itemLootTable == null) {
            IsaacDisaster.LOGGER.warn("Null loot table for pedestal at {}; using an empty table", worldPosition);
        }
        this.itemLootTable = itemLootTable == null ? "" : itemLootTable;
        this.generated = false;
        this.generationFailed = false;
        this.nextGenerationAttemptTick = 0;
        if (!this.itemLootTable.isEmpty()) {
            this.contentSource = ContentSource.LOOT_TABLE;
        }
        syncChanges();
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
        syncChanges();
    }

    public void addLinkedPedestals(BlockPos pos){
        linkedOffsets.add(pos.subtract(this.worldPosition));
        syncChanges();
    }

    public void clearLinkedPedestals(){
        if (!linkedOffsets.isEmpty()) {
            linkedOffsets.clear();
            syncChanges();
        }
    }

    public void removeLinkedPedestal(BlockPos absolutePos) {
        if (linkedOffsets.remove(absolutePos.subtract(this.worldPosition))) {
            syncChanges();
        }
    }

    public static void removeFromAllLinks(BlockPos removedPos, ServerLevel level) {
        BlockData data = BlockData.get(level);
        if (level.getBlockEntity(removedPos) instanceof PedestalBlockEntity removed) {
            for (BlockPos linked : removed.getLinkedPedestals()) {
                if (!level.hasChunkAt(linked)) data.queuePedestalUnlink(linked, removedPos);
            }
        }
        for (BlockPos pedestalPos : BlockData.get(level).getAllPedestals()) {
            if (pedestalPos.equals(removedPos)) continue;
            if (level.hasChunkAt(pedestalPos)
                    && level.getBlockEntity(pedestalPos) instanceof PedestalBlockEntity pedestal) {
                pedestal.removeLinkedPedestal(removedPos);
            }
        }
    }

    public static void linkPedestals(BlockPos originalPos, BlockPos pos, ServerLevel level){
        if (!level.hasChunkAt(originalPos) || !level.hasChunkAt(pos)) return;
        BlockEntity originalBe = level.getBlockEntity(originalPos);
        BlockEntity be = level.getBlockEntity(pos);
        boolean locked = false;

        if (originalBe instanceof PedestalBlockEntity originalPedestal &&
                be instanceof PedestalBlockEntity pedestal) {

            Set<BlockPos> allLinked = new HashSet<>();
            allLinked.addAll(originalPedestal.getLinkedPedestals());
            allLinked.addAll(pedestal.getLinkedPedestals());
            allLinked.add(originalPos);
            allLinked.add(pos);

            // 连接底座
            for (BlockPos p : allLinked){
                if (level.hasChunkAt(p) && level.getBlockEntity(p) instanceof PedestalBlockEntity pbe){
                    locked = pbe.isLocked() || locked; // 记录上锁状态
                    pbe.setLinkedPedestals(allLinked);
                    level.sendBlockUpdated(p, pbe.getBlockState(), pbe.getBlockState(), 3);
                }
            }

            // 设置上锁状态
            for (BlockPos p : allLinked){
                if (level.hasChunkAt(p) && level.getBlockEntity(p) instanceof PedestalBlockEntity pbe){
                    pbe.setLocked(locked);
                } else if (!level.hasChunkAt(p)) {
                    BlockData.get(level).queuePedestalLinks(p, allLinked);
                    BlockData.get(level).queuePedestalLock(p, locked);
                }
            }
        }
    }

    public void unlockAll(){
        if (!(level instanceof ServerLevel serverLevel)) return;
        // 同时解锁全部链接的底座
        Set<BlockPos> allLinked = new HashSet<>(getLinkedPedestals());
        allLinked.add(worldPosition);
        for (BlockPos pos : allLinked){
            if (!level.hasChunkAt(pos)) {
                BlockData.get(serverLevel).queuePedestalLock(pos, false);
                continue;
            }
            if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof PedestalBlockEntity be){
                be.setLocked(false);
            }
        }
    }

    public void clearContent(){
        clearItemDisplayList();
    }

    public void drops(){
        if(level == null || level.isClientSide || breakPolicy != BreakPolicy.DROP_CONTENT || isLocked()) return;
        SimpleContainer inv = new SimpleContainer(inventory.getSlots());
        for(int i=0; i<inventory.getSlots(); i++) {
            inv.setItem(i, inventory.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    public ItemStack getItem(){ return inventory.getStackInSlot(0).copy(); }
    public void setItem(ItemStack stack){
        ItemStack normalized = stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        if (!normalized.isEmpty()) normalized.setCount(1);
        inventory.setStackInSlot(0, normalized);
    }

    private void updateItemIndex(ItemStack stack) {
        if (level instanceof ServerLevel serverLevel) {
            BlockData data = BlockData.get(serverLevel);
            if (stack.isEmpty()) data.removeItemBlock(worldPosition);
            else data.addItemBlock(worldPosition);
        }
    }

    /** Handles one ordinary right-click after lock/debug extensions are handled. */
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (level == null || level.isClientSide || isLocked()) return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand);
        if (getItem().isEmpty()) return insertItem(player, held);
        return held.isEmpty() ? acquireItem(player, hand) : InteractionResult.PASS;
    }

    private InteractionResult insertItem(Player player, ItemStack held) {
        if (held.isEmpty() || (contentSource != ContentSource.MANUAL && !player.isCreative())) {
            return InteractionResult.PASS;
        }
        if (contentSource != ContentSource.MANUAL) setContentSource(ContentSource.MANUAL);
        setItem(held);
        if (!player.isCreative()) held.shrink(1);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult acquireItem(Player player, InteractionHand hand) {
        if (!player.isCreative() && !payPrice(player)) return InteractionResult.SUCCESS;
        ItemStack acquired = getItem();
        consumeLinkedGroup();
        level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.7f, 1.0f);
        if (autoUseOnAcquire && getBlockState().getBlock() instanceof ItemDisplayContainerBlock container) {
            container.acquireItem(player, hand, acquired);
        }
        else player.setItemInHand(hand, acquired);
        return InteractionResult.SUCCESS;
    }

    /** Consuming a choice does not change its content source or pricing. */
    public void consumeContent() {
        generated = true;
        generationFailed = false;
        clearContent();
        clearLinkedPedestals();
        syncChanges();
    }

    private void consumeLinkedGroup() {
        ServerLevel serverLevel = (ServerLevel) level;
        Set<BlockPos> members = getLinkedPedestals();
        members.add(worldPosition);
        for (BlockPos pos : members) {
            if (!serverLevel.hasChunkAt(pos)) {
                BlockData.get(serverLevel).queuePedestalClear(pos);
            } else if (serverLevel.getBlockEntity(pos) instanceof PedestalBlockEntity member) {
                boolean hadItem = !member.getItem().isEmpty();
                member.consumeContent();
                if (member != this && hadItem) {
                    serverLevel.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.5,
                            pos.getZ() + 0.5, 10, 0, 0.2, 0, 0.05);
                }
            }
        }
    }

    private boolean payPrice(Player player) {
        if (price.isFree()) return true;
        if (price.type() == PedestalPrice.Type.MONEY) {
            return PlayerHelper.countMoney(player) >= price.amount()
                    && PlayerHelper.takeMoney(player, price.amount());
        }

        double cost = price.amount() * StatManager.MAX_HEALTH.getBonus();
        double maxHealth = player.getMaxHealth();
        double absorption = player.getAbsorptionAmount();
        if (maxHealth + absorption / 2 < cost) return false;

        double remaining = cost - maxHealth;
        StatManager.MAX_HEALTH.apply(player, -price.amount());
        if (remaining > 0) player.setAbsorptionAmount((float) (absorption - remaining * 2));
        return true;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level == null || level.isClientSide) return;

        BlockData manager = BlockData.get((ServerLevel) level);
        manager.addPedestal(worldPosition);
        BlockData.PendingPedestalUpdate update = manager.takePendingPedestalUpdate(worldPosition);
        if (update != null) {
            if (update.linkedPositions() != null) setLinkedPedestals(update.linkedPositions());
            for (BlockPos removed : update.removedLinks()) removeLinkedPedestal(removed);
            if (update.locked() != null) setLocked(update.locked());
        }
        if (manager.takePendingPedestalClear(worldPosition)) consumeContent();
        if (linkedOffsets.removeIf(offset -> level.hasChunkAt(worldPosition.offset(offset))
                && !(level.getBlockEntity(worldPosition.offset(offset)) instanceof PedestalBlockEntity))) {
            syncChanges();
        }
        updateItemIndex(getItem());
    }

    public static <T extends PedestalBlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (level.isClientSide) return;

        // 道具轮播
        blockEntity.tickRotate(level, level.getGameTime(), null);

        if (blockEntity.getContentSource() != ContentSource.LOOT_TABLE
                || blockEntity.isGenerated()
                || blockEntity.getItemLootTable().isEmpty()
                || !blockEntity.getItem().isEmpty()
                || level.getGameTime() < ((PedestalBlockEntity) blockEntity).nextGenerationAttemptTick
                || level.getGameRules().getBoolean(ModGameRules.DISABLE_PLACEHOLDER)) return;

        int range = 5;
        Player player = LevelHelper.findNearestOfType(level, pos.getCenter(), range, Player.class,
                e -> e instanceof Player p && !p.isCreative() && !p.isSpectator()); // 排除创造玩家和观察者

        if (player == null) return;

        blockEntity.tryLootItem((ServerLevel) level, player, pos);
    }

    @Override
    public boolean tryLootItem(ServerLevel serverLevel, Player player, BlockPos pos) {
        if (contentSource != ContentSource.LOOT_TABLE || itemLootTable.isEmpty()) return false;
        ResourceLocation tableId = ResourceLocation.tryParse(itemLootTable);
        if (tableId == null) {
            if (!generationFailed) {
                IsaacDisaster.LOGGER.warn("Invalid loot table '{}' for pedestal at {}; leaving generation retryable",
                        itemLootTable, worldPosition);
            }
            updateGenerationResult(false, serverLevel.getGameTime());
            return false;
        }
        try {
            boolean success = lootItem(serverLevel, player, pos,
                    tableId, this::clearContent);
            updateGenerationResult(success, serverLevel.getGameTime());
            return success;
        } catch (Exception e) {
            if (!generationFailed) {
                IsaacDisaster.LOGGER.warn("Failed to generate loot for pedestal at {} with table {}; leaving generation retryable",
                        worldPosition, itemLootTable, e);
            }
            updateGenerationResult(false, serverLevel.getGameTime());
        }
        return false;
    }

    private void updateGenerationResult(boolean success, long gameTime) {
        generated = success;
        generationFailed = !success;
        nextGenerationAttemptTick = success ? 0 : gameTime + 100;
        syncChanges();
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
        tag.putString("contentSource", contentSource.name());
        tag.putBoolean("locked", locked);
        tag.putBoolean("generated", generated);
        tag.putBoolean("generationFailed", generationFailed);
        tag.putString("priceType", price.type().name());
        tag.putInt("priceAmount", price.amount());
        tag.putString("breakPolicy", breakPolicy.name());
        tag.putBoolean("autoUseOnAcquire", autoUseOnAcquire);

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
        contentSource = ContentSource.MANUAL;
        price = PedestalPrice.free();
        locked = tag.getBoolean("locked");
        generated = tag.getBoolean("generated");
        generationFailed = tag.getBoolean("generationFailed");
        nextGenerationAttemptTick = 0;
        itemLootTable = tag.getString("itemLootTable");
        linkedOffsets.clear();
        displayItemListCap.clear();
        CompoundTag storedInventory = tag.getCompound("inventory").copy();
        storedInventory.putInt("Size", 1);
        inventory.deserializeNBT(storedInventory);
        ItemStack savedItem = getItem();
        loadItemDisplayCap(tag);

        if (tag.contains("contentSource")) {
            try {
                contentSource = ContentSource.valueOf(tag.getString("contentSource"));
            } catch (IllegalArgumentException ignored) {
                IsaacDisaster.LOGGER.warn("Invalid content source '{}' for pedestal at {}; using MANUAL",
                        tag.getString("contentSource"), worldPosition);
                contentSource = ContentSource.MANUAL;
            }
        }
        breakPolicy = BreakPolicy.DROP_CONTENT;
        autoUseOnAcquire = tag.getBoolean("autoUseOnAcquire");
        if (tag.contains("breakPolicy")) {
            try {
                breakPolicy = BreakPolicy.valueOf(tag.getString("breakPolicy"));
            } catch (IllegalArgumentException ignored) {
                IsaacDisaster.LOGGER.warn("Invalid break policy '{}' for pedestal at {}; using {}",
                        tag.getString("breakPolicy"), worldPosition, breakPolicy);
            }
        }
        if (tag.contains("priceType")) {
            try {
                PedestalPrice.Type type = PedestalPrice.Type.valueOf(tag.getString("priceType"));
                price = new PedestalPrice(type, tag.getInt("priceAmount"));
            } catch (IllegalArgumentException ignored) {
                IsaacDisaster.LOGGER.warn("Invalid price type '{}' for pedestal at {}; using FREE",
                        tag.getString("priceType"), worldPosition);
                price = PedestalPrice.free();
            }
        }
        // linkedOffsets
        if (tag.contains("linkedOffsets")) {
            ListTag listTag = tag.getList("linkedOffsets", 10);
            for (Tag t : listTag) {
                CompoundTag posTag = (CompoundTag) t;
                linkedOffsets.add(new BlockPos(
                        posTag.getInt("x"),
                        posTag.getInt("y"),
                        posTag.getInt("z")));
            }
        }
        setItem(getItemDisplayList().isEmpty() ? savedItem : getCurrentItemDisplay());
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
        if (contentSource != ContentSource.LOOT_TABLE || itemLootTable.isEmpty()) return;

        this.tryLootItem((ServerLevel) level, player, getBlockPos());
    }


    // ====== 道具轮播 ======
    private final DisplayItemListCap displayItemListCap = new PedestalDisplayItems(
            () -> setItem(getCurrentItemDisplay()));

    @Override
    public void addItemDisplay(ItemStack stack) {
        displayItemListCap.addItem(stack);
    }

    @Override
    public int getDisplayTickInterval() {
        return Math.max(1, ItemDisplayContainerBlockEntity.super.getDisplayTickInterval());
    }

    @Override
    public DisplayItemListCap getItemDisplayCap() {
        return displayItemListCap;
    }

}
