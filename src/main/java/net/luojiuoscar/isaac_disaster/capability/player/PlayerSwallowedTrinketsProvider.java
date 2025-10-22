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


public class PlayerSwallowedTrinketsProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    //stat modifiers
    public static Capability<PlayerSwallowedTrinkets> PLAYER_SWALLOWED_TRINKETS = CapabilityManager.get(new CapabilityToken<PlayerSwallowedTrinkets>() {});


    private PlayerSwallowedTrinkets swallowedTrinkets = null;
    private final LazyOptional<PlayerSwallowedTrinkets> optional = LazyOptional.of(this::createPlayerSwallowedTrinkets);

    private PlayerSwallowedTrinkets createPlayerSwallowedTrinkets(){
        if(this.swallowedTrinkets == null){
            this.swallowedTrinkets = new PlayerSwallowedTrinkets();
        }

        return this.swallowedTrinkets;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == PLAYER_SWALLOWED_TRINKETS){
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createPlayerSwallowedTrinkets().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerSwallowedTrinkets().loadNBTData(nbt);
    }
}
