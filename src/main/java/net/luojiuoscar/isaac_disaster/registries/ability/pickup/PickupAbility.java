package net.luojiuoscar.isaac_disaster.registries.ability.pickup;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public abstract class PickupAbility {
    protected final CompositeTrigger trigger;

    protected PickupAbility(CompositeTrigger trigger){
        this.trigger = trigger;
    }

    protected ExecutableEffectContext getCtx(ServerPlayer player, @Nullable ItemStack stack, @Nullable InteractionHand hand){
        ExecutableEffectContext ctx = new ExecutableEffectContext(player);
        ctx.set(ContextKeys.TARGET_POSITION, player.position());
        ctx.set(ContextKeys.AMPLIFIER, 1.);

        if (stack != null){
            ctx.set(ContextKeys.ITEM, stack.getItem());
            ctx.set(ContextKeys.DOUBLE, List.of((double) stack.getCount()));
        }
        if (hand != null){
            ctx.set(ContextKeys.HAND, hand);
        }
        return ctx;
    }

    public void onUse(ServerPlayer player, @Nullable ItemStack stack, @Nullable InteractionHand hand){
        ExecutableEffectContext ctx = getCtx(player, stack, hand);

        fire(ctx);

        if (!player.isCreative() && stack != null){
            shrinkAfterUse(stack);
        }
    }

    public void shrinkAfterUse(@NotNull ItemStack stack){
        stack.shrink(1);
    }

    public void fire(ExecutableEffectContext context){
        trigger.fire(context, null);
    }

    public abstract void makeSound(ServerPlayer player);
    public List<Component> getDesc(){
        return List.of();
    }
}
