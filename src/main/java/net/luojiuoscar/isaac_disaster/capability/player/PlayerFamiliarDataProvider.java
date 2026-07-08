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

/**
 * Capability provider for persistent Isaac familiar requirements.
 */
public class PlayerFamiliarDataProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<PlayerFamiliarData> PLAYER_FAMILIAR_DATA =
            CapabilityManager.get(new CapabilityToken<>() {});

    private PlayerFamiliarData playerFamiliarData = null;
    private final LazyOptional<PlayerFamiliarData> optional = LazyOptional.of(this::createPlayerFamiliarData);

    private PlayerFamiliarData createPlayerFamiliarData() {
        if (this.playerFamiliarData == null) {
            this.playerFamiliarData = new PlayerFamiliarData();
        }
        return this.playerFamiliarData;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PLAYER_FAMILIAR_DATA) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createPlayerFamiliarData().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerFamiliarData().loadNBTData(nbt);
    }
}
