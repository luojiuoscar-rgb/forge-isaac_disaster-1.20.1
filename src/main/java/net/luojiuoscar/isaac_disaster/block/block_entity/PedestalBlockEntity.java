package net.luojiuoscar.isaac_disaster.block.block_entity;

import net.luojiuoscar.isaac_disaster.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class PedestalBlockEntity extends BlockEntity {
    public final ItemStackHandler inventory = new ItemStackHandler(1){
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
    private Set<BlockPos> linkedOffsets = new HashSet<>();
    private boolean isDecoration = true;

    public PedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PEDESTAL_BLOCK_ENTITY.get(), pos, state);
    }

    public float getRenderingRotation(){
        rotation += 0.5f;
        if (rotation >= 360) rotation = 0;
        return rotation;
    }

    public boolean isDecoration(){ return isDecoration; }
    public void setDecoration(boolean isDecoration){ this.isDecoration = isDecoration; }

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
    }

    public void clearLinkedPedestals(){ linkedOffsets.clear(); }

    public static void linkPedestals(BlockPos originalPos, BlockPos pos, ServerLevel level){
        BlockEntity originalBe = level.getBlockEntity(originalPos);
        BlockEntity be = level.getBlockEntity(pos);

        if (originalBe instanceof PedestalBlockEntity originalPedestal &&
                be instanceof PedestalBlockEntity pedestal) {

            Set<BlockPos> allLinked = new HashSet<>();
            allLinked.addAll(originalPedestal.getLinkedPedestals());
            allLinked.addAll(pedestal.getLinkedPedestals());

            for (BlockPos p : allLinked){
                if (level.getBlockEntity(p) instanceof PedestalBlockEntity pbe){
                    pbe.setLinkedPedestals(allLinked);
                    pbe.setDecoration(false);
                }
            }
        }
    }

    public void clearContents(){ inventory.setStackInSlot(0, ItemStack.EMPTY); }

    public void drops(){
        if(level == null || !isDecoration) return;
        SimpleContainer inv = new SimpleContainer(inventory.getSlots());
        for(int i=0;i<inventory.getSlots();i++) inv.setItem(i, inventory.getStackInSlot(i));
        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    public ItemStack getItem(){ return inventory.getStackInSlot(0); }
    public void setItem(ItemStack stack){ inventory.setStackInSlot(0, stack); }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag){
        super.saveAdditional(tag);
        tag.put("inventory", inventory.serializeNBT());
        tag.putBoolean("isDecoration", isDecoration);

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
    public void load(@NotNull CompoundTag tag){
        super.load(tag);
        if(tag.contains("inventory")) inventory.deserializeNBT(tag.getCompound("inventory"));
        isDecoration = tag.getBoolean("isDecoration");

        linkedOffsets.clear();
        if(tag.contains("linkedOffsets")){
            ListTag listTag = tag.getList("linkedOffsets", 10);
            for(Tag t : listTag){
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
}
