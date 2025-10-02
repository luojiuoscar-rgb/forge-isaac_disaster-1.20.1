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


public class EntityEffectProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<EntityEffect> ENTITY_CAP = CapabilityManager.get(new CapabilityToken<EntityEffect>() {});

    private EntityEffect entityCap = null;
    private final LazyOptional<EntityEffect> optional = LazyOptional.of(this::createEntityCap);

    /**
     * 创建
     */
    private EntityEffect createEntityCap() {
        if(this.entityCap == null){
            this.entityCap = new EntityEffect();
        }

        return this.entityCap;
    }


    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ENTITY_CAP){
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createEntityCap().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createEntityCap().loadNBTData(nbt);
    }
}
