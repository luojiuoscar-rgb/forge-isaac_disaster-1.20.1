package net.luojiuoscar.isaac_disaster.capability.player.flight;

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

/** Forge capability provider for {@link PlayerIsaacFlight}. */
public class PlayerIsaacFlightProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final Capability<PlayerIsaacFlight> PLAYER_ISAAC_FLIGHT =
            CapabilityManager.get(new CapabilityToken<>() {});

    private final LazyOptional<PlayerIsaacFlight> optional = LazyOptional.of(PlayerIsaacFlight::new);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == PLAYER_ISAAC_FLIGHT ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        optional.ifPresent(flight -> flight.saveNBTData(nbt));
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        optional.ifPresent(flight -> flight.loadNBTData(nbt));
    }
}
