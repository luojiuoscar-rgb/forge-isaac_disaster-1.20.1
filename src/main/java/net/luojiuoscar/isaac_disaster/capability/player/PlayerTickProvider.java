package net.luojiuoscar.isaac_disaster.capability.player;

import net.minecraftforge.common.capabilities.*;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.LazyOptional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerTickProvider implements ICapabilitySerializable<CompoundTag> {

    public static final Capability<PlayerTick> RECOVERY_CAP =
            CapabilityManager.get(new CapabilityToken<>(){});

    private final PlayerTick instance = new PlayerTick();
    private final LazyOptional<PlayerTick> lazy = LazyOptional.of(() -> instance);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == RECOVERY_CAP ? lazy.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return instance.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.deserializeNBT(nbt);
    }
}
