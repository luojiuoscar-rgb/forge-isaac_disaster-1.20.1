package net.luojiuoscar.isaac_disaster.block.block_entity.chest;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public abstract class IsaacChestBlockEntity extends ChestBlockEntity implements MenuProvider {
    public IsaacChestBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
    }

    abstract public ResourceLocation getLidResourceLocation();
    abstract ResourceLocation getPresetLootTable();

    @Override
    abstract public @NotNull Component getDisplayName();

    protected boolean opened = false;

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

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("opened")) this.opened = tag.getBoolean("opened");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("opened", opened);
    }

    private boolean presetApplied = false;
    @Override
    public void unpackLootTable(@Nullable Player player) {
        if (!presetApplied){
            if (!opened && this.lootTable == null) {
                this.setLootTable(getPresetLootTable(), 0);
                presetApplied = true;
            }
        }
        super.unpackLootTable(player);
    }

    public ResourceLocation getLootTable(){
        return this.lootTable;
    }
}

