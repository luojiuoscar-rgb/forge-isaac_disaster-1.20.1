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



public class PlayerStatModifierProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    //stat modifiers
    public static Capability<PlayerStatModifier> PLAYER_STAT_MODIFIER = CapabilityManager.get(new CapabilityToken<PlayerStatModifier>() {});


    private PlayerStatModifier statModifier = null;
    private final LazyOptional<PlayerStatModifier> optional = LazyOptional.of(this::createPlayerStatModifiers);

    private PlayerStatModifier createPlayerStatModifiers(){
        if(this.statModifier == null){
            this.statModifier = new PlayerStatModifier();
        }

        return this.statModifier;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == PLAYER_STAT_MODIFIER){
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createPlayerStatModifiers().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerStatModifiers().loadNBTData(nbt);
    }
}
