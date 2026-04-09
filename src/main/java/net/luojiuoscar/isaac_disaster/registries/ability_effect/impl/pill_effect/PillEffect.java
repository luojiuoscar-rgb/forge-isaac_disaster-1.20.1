package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.interfaces.AddDamageIfLowQuality;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.interfaces.GiveBlackHeartIfLowQuality;
import net.minecraft.server.level.ServerPlayer;


public abstract class PillEffect implements IAbilityEffect {
    protected abstract PillEffect redirect(ServerPlayer player);

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (context.getEntity() instanceof ServerPlayer player){
            // redirect and use
            PillEffect effect = redirect(player);
            boolean isHorse = context.get(ContextKeys.BOOLEAN) != null && context.get(ContextKeys.BOOLEAN).get(0);

            // make sound
            effect.makeSounds(player, isHorse);

            // 需要在低质量的时候给黑心吗？
            if (effect instanceof GiveBlackHeartIfLowQuality){
                if (PlayerHelper.getPillQuality(player) < 0){
                    PlayerHelper.giveItem(player, ModItems.BLACK_HEART.get(), 1);
                }
            }
            // 需要在低质量的时候提供攻击力吗？
            if (effect instanceof AddDamageIfLowQuality){
                if (PlayerHelper.getPillQuality(player) < 0){
                    StatManager.DAMAGE.apply(player, isHorse ? 0.8 : 0.4);
                }
            }

            return effect.pillActive(player, isHorse, context);
        }
        return false;
    }

    protected abstract boolean pillActive(ServerPlayer player, boolean isHorse, ExecutableEffectContext context);

    public void makeSounds(ServerPlayer player, boolean isHorse){
        if (!isHorse){
            makeSound(player);
        }else {
            makeSoundH(player);
        }
    }

    public abstract void makeSound(ServerPlayer player);
    public abstract void makeSoundH(ServerPlayer player);

    public abstract String getDescriptionId(int pillQuality);
}
