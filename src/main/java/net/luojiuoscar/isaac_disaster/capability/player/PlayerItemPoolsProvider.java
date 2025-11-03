package net.luojiuoscar.isaac_disaster.capability.player;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class PlayerItemPoolsProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    //stat modifiers
    public static Capability<PlayerItemPools> PLAYER_ITEM_POOL = CapabilityManager.get(new CapabilityToken<PlayerItemPools>() {});


    private PlayerItemPools itemPools = null;
    private final LazyOptional<PlayerItemPools> optional = LazyOptional.of(this::createPlayerItemPools);

    private PlayerItemPools createPlayerItemPools(){
        if(this.itemPools == null){
            this.itemPools = new PlayerItemPools();
        }

        return this.itemPools;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == PLAYER_ITEM_POOL){
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createPlayerItemPools().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerItemPools().loadNBTData(nbt);
    }
}
