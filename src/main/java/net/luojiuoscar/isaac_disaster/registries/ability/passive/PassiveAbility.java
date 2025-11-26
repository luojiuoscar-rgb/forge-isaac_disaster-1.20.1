package net.luojiuoscar.isaac_disaster.registries.ability.passive;

import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.registries.ability.IsaacItemAbility;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PassiveItemModifyCountSyncS2CPacket;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
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
            ModMessages.sentToPlayer(new PassiveItemModifyCountSyncS2CPacket(
                    ForgeRegistries.ITEMS.getKey(stack.getItem()), 1), player);
        }
    }

    public void onRemove(ServerPlayer player, @Nullable ItemStack stack){
        handleRemove(player, stack);

        if (stack != null){
            ModMessages.sentToPlayer(new PassiveItemModifyCountSyncS2CPacket(ForgeRegistries.ITEMS.getKey(stack.getItem()), -1), player);
        }
    }

    abstract public void handleObtain(ServerPlayer player, @Nullable ItemStack stack);
    abstract public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack);
    abstract public void handleRemove(ServerPlayer player, @Nullable ItemStack stack);

    public void makeSound(LocalPlayer player){
        player.playSound(ModSounds.DEFAULT_OBTAIN_ITEM.get());
    }
}
