package net.luojiuoscar.isaac_disaster.capability.misc;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class ExplosionDataProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static Capability<ExplosionData> EXPLOSION_DATA;

    static {
        EXPLOSION_DATA = CapabilityManager.get(new CapabilityToken<>() {});
    }

    private final ExplosionData data = new ExplosionData();
    private final LazyOptional<ExplosionData> optional = LazyOptional.of(() -> data);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == EXPLOSION_DATA ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {}
}