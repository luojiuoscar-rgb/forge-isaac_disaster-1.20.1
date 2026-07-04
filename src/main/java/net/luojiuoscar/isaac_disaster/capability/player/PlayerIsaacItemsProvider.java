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


public class PlayerIsaacItemsProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<PlayerIsaacItems> PLAYER_ISAAC_ITEMS = CapabilityManager.get(new CapabilityToken<PlayerIsaacItems>() {});

    private PlayerIsaacItems isaacItems = null;
    private final LazyOptional<PlayerIsaacItems> optional = LazyOptional.of(this::clreatePlayerIsaacItems);

    /**
     * 创建
     */
    private PlayerIsaacItems clreatePlayerIsaacItems() {
        if(this.isaacItems == null){
            this.isaacItems = new PlayerIsaacItems();
        }

        return this.isaacItems;
    }


    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == PLAYER_ISAAC_ITEMS){
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        clreatePlayerIsaacItems().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        clreatePlayerIsaacItems().loadNBTData(nbt);
    }
}
