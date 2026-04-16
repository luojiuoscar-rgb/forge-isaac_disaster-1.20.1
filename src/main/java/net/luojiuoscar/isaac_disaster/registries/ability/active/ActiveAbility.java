package net.luojiuoscar.isaac_disaster.registries.ability.active;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability.IsaacItemAbility;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public abstract class ActiveAbility extends IsaacItemAbility {
    protected final CompositeTrigger trigger;

    public ActiveAbility(CompositeTrigger trigger, int id, int level) {
        super(id, level);
        this.trigger = trigger;
    }

    public CompositeTrigger getTrigger() {
        return trigger;
    }

    public void onUse(ServerPlayer player, @Nullable InteractionHand hand){
        ItemStack stack = hand != null ? player.getItemInHand(hand) : null;
        ExecutableEffectContext context = getCtx(player, stack, hand,
                PlayerHelper.hasItem(ItemId.CAR_BATTERY.getId(), player) // 车载电池
                        ? getStrongerAmplifier() : getNormalAmplifier());

        trigger.fire(context, null);
    }

    /** 首次使用时触发 */
    public void onFirstUse(ServerPlayer player, @Nullable ItemStack stack, @Nullable InteractionHand hand){}

    public double getNormalAmplifier(){
        return 1;
    }
    public double getStrongerAmplifier(){
        return 2;
    }

    /** 如果想要修改ctx，需要将效果注册成AbilityEffectEntry */
    protected final ExecutableEffectContext getCtx(ServerPlayer player, ItemStack stack,
                                                   @Nullable InteractionHand hand, double amplifier){
        ExecutableEffectContext ctx = new ExecutableEffectContext(player);
        ctx.set(ContextKeys.AMPLIFIER, amplifier);
        ctx.set(ContextKeys.ITEM_STACK, stack);
        ctx.set(ContextKeys.HAND, hand);
        ctx.set(ContextKeys.TARGET_POSITION, player.position());

        return ctx;
    }

    public void rechargeSFX(ServerPlayer player) {
        player.level().playSound(null, player.blockPosition(), ModSounds.BATTERY_SMALL.get(),
                SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    public void triggerSFX(ServerPlayer player){};

    @Override
    abstract public List<Component> getSynergyDesc(@org.jetbrains.annotations.Nullable ItemStack stack);
}
