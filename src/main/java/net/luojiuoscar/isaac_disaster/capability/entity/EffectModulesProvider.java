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


public class EffectModulesProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    //stat modifiers
    public static Capability<EffectModules> EFFECT_MODULES = CapabilityManager.get(new CapabilityToken<EffectModules>() {});


    private EffectModules effectModules = null;
    private final LazyOptional<EffectModules> optional = LazyOptional.of(this::createEffectModule);

    private EffectModules createEffectModule(){
        if(this.effectModules == null){
            this.effectModules = new EffectModules();
        }
        return this.effectModules;
    }


    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == EFFECT_MODULES){
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createEffectModule().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createEffectModule().loadNBTData(nbt);
    }
}
