package net.luojiuoscar.isaac_disaster.registries.ability.passive;

import net.luojiuoscar.isaac_disaster.event.custom.misc.PassiveItemFirstObtainEvent;
import net.luojiuoscar.isaac_disaster.event.custom.misc.PassiveItemObtainEvent;
import net.luojiuoscar.isaac_disaster.event.custom.misc.PassiveItemRemoveEvent;
import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.registries.ability.IsaacItemAbility;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IExecutableEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;

public abstract class PassiveAbility extends IsaacItemAbility {
    public PassiveAbility(int id, int level) {
        super(id, level);
    }

    public void onObtain(ServerPlayer player, @Nullable ItemStack stack){
        // effect
        PassiveItemObtainEvent e1 = new PassiveItemObtainEvent(player, stack, this);
        MinecraftForge.EVENT_BUS.post(e1);

        handleObtain(player, stack);

        if (stack != null && !PassiveItem.hasBeenUsed(stack)){
            PassiveItemFirstObtainEvent e2 = new PassiveItemFirstObtainEvent(player, stack, this);
            MinecraftForge.EVENT_BUS.post(e2);

            handleFirstObtain(player, stack);
        }
    }

    public void onRemove(ServerPlayer player, @Nullable ItemStack stack){
        PassiveItemRemoveEvent e = new PassiveItemRemoveEvent(player, stack, this);
        MinecraftForge.EVENT_BUS.post(e);

        handleRemove(player, stack);
    }

    /** "首次获取效果"默认在"获取效果"之后执行
     * 同时，此效果不应由ExecutableEffect驱动 */
    abstract public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack);
    abstract public void handleObtain(ServerPlayer player, @Nullable ItemStack stack);
    abstract public void handleRemove(ServerPlayer player, @Nullable ItemStack stack);

    protected ExecutableEffectContext getContext(ServerPlayer player, @Nullable ItemStack stack){
        ExecutableEffectContext context = new ExecutableEffectContext(player);
        context.set(ContextKeys.TARGET_POSITION, player.position());
        if (stack != null && !stack.isEmpty()){
            context.set(ContextKeys.ITEM, stack.getItem());
            context.set(ContextKeys.ITEM_STACK, stack);
        }

        return context;
    }

    protected void triggerEffect(IExecutableEffect effect, ServerPlayer player, @Nullable ItemStack stack){
        effect.apply(getContext(player, stack));
    }

    public void makeSound(ServerPlayer player){
        player.playNotifySound(ModSounds.DEFAULT_OBTAIN_ITEM.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}

