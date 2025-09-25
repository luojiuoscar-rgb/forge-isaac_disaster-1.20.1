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


public class PlayerPassiveItemProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<PlayerPassiveItem> PLAYER_PASSIVE_ITEM = CapabilityManager.get(new CapabilityToken<PlayerPassiveItem>() {});

    private PlayerPassiveItem passiveItem = null;
    private final LazyOptional<PlayerPassiveItem> optional = LazyOptional.of(this::createPlayerPassiveItem);

    private PlayerPassiveItem createPlayerPassiveItem() {
        if(this.passiveItem == null){
            this.passiveItem = new PlayerPassiveItem();
        }

        return this.passiveItem;
    }


    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == PLAYER_PASSIVE_ITEM){
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createPlayerPassiveItem().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerPassiveItem().loadNBTData(nbt);
    }
}
