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


public class TriggerModuleProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    //stat modifiers
    public static Capability<TriggerModule> TRIGGER_MODULES = CapabilityManager.get(new CapabilityToken<TriggerModule>() {});


    private TriggerModule triggerModule = null;
    private final LazyOptional<TriggerModule> optional = LazyOptional.of(this::createTriggerModule);

    private TriggerModule createTriggerModule(){
        if(this.triggerModule == null){
            this.triggerModule = new TriggerModule();
        }
        return this.triggerModule;
    }


    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == TRIGGER_MODULES){
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createTriggerModule().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createTriggerModule().loadNBTData(nbt);
    }
}
