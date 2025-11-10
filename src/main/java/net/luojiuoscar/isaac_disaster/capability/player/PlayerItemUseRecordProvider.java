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

public class PlayerItemUseRecordProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static Capability<PlayerItemUseRecord> PLAYER_ITEM_USE_RECORD = CapabilityManager.get(new CapabilityToken<PlayerItemUseRecord>() {});

    private PlayerItemUseRecord record = null;
    private final LazyOptional<PlayerItemUseRecord> optional = LazyOptional.of(this::createRecord);

    private PlayerItemUseRecord createRecord() {
        if (record == null) {
            record = new PlayerItemUseRecord();
        }
        return record;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PLAYER_ITEM_USE_RECORD) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createRecord().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createRecord().loadNBTData(nbt);
    }
}
