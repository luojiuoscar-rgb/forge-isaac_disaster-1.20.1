package net.luojiuoscar.isaac_disaster.registries.ability;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class IsaacItemAbility {
    private final int id;
    private final int level;

    public IsaacItemAbility(int id, int level){
        this.id = id;
        this.level = level;
    }

    public final int getId() {
        return id;
    }

    public final int getLevel() {
        return level;
    }

    public List<Component> getDesc(@Nullable ItemStack stack){
        return List.of();
    }

    public List<Component> getExtraDesc(@Nullable ItemStack stack){
        return List.of();
    }

    public List<Component> getSynergyDesc(@Nullable ItemStack stack){
        return List.of();
    }
}
