package net.luojiuoscar.isaac_disaster.registries.ability.passive;

import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.registries.ability.IsaacItemAbility;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class PassiveAbility extends IsaacItemAbility {

    public PassiveAbility(int id, int level) {
        super(id, level);
    }

    public void onObtain(ServerPlayer player, @Nullable ItemStack stack){
        // effect
        handleObtain(player, stack);
        if (stack != null && !PassiveItem.hasBeenUsed(stack)){
            handleFirstObtain(player, stack);
        }
    }

    public void onRemove(ServerPlayer player, @Nullable ItemStack stack){
        handleRemove(player, stack);
    }

    abstract public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack);
    abstract public void handleObtain(ServerPlayer player, @Nullable ItemStack stack);
    abstract public void handleRemove(ServerPlayer player, @Nullable ItemStack stack);

    public void makeSound(ServerPlayer player){
        player.playNotifySound(ModSounds.DEFAULT_OBTAIN_ITEM.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}

