package net.luojiuoscar.isaac_disaster.capability.entity;

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


public class ExtraDataProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<ExtraData> EXTRA_DATA_CAP = CapabilityManager.get(new CapabilityToken<ExtraData>() {});

    private ExtraData entityCap = null;
    private final LazyOptional<ExtraData> optional = LazyOptional.of(this::createExtraData);

    private ExtraData createExtraData() {
        if(this.entityCap == null){
            this.entityCap = new ExtraData();
        }

        return this.entityCap;
    }


    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == EXTRA_DATA_CAP){
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createExtraData().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createExtraData().loadNBTData(nbt);
    }
}
