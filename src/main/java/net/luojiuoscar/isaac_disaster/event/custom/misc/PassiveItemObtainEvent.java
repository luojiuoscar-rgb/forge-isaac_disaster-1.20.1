package net.luojiuoscar.isaac_disaster.event.custom.misc;

import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

public class PassiveItemObtainEvent extends Event {
    private final ServerPlayer player;
    private final PassiveAbility ability;
    @Nullable
    private final ItemStack stack;


    public PassiveItemObtainEvent(ServerPlayer player, @Nullable ItemStack stack, PassiveAbility ability){
        this.player = player;
        this.ability = ability;
        this.stack = stack;
    }

    public PassiveAbility getAbility() {
        return ability;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    @Nullable
    public ItemStack getStack() {
        return stack;
    }
}
