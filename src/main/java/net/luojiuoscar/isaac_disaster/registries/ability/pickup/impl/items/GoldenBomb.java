package net.luojiuoscar.isaac_disaster.registries.ability.pickup.impl.items;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.PickupAbility;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class GoldenBomb extends PickupAbility {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModExecutableEffects.THROW_BOMB, context ->
                    !(context.getEntity() instanceof ServerPlayer player)
                            || !PlayerHelper.hasItem(ItemId.MR_MEGA.getId(), player)
            ),
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModExecutableEffects.THROW_MEGA_BOMB, context ->
                    (context.getEntity() instanceof ServerPlayer player)
                            && PlayerHelper.hasItem(ItemId.MR_MEGA.getId(), player)
            ),
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModExecutableEffects.ADD_COOLDOWN_TO_ITEM, context -> {
                if (!(context.getEntity() instanceof ServerPlayer player)) return false;
                if (PlayerHelper.hasItem(ItemId.FAST_BOMB.getId(), player)){
                    context.set(ContextKeys.DOUBLE, List.of(20.));
                }else {
                    context.set(ContextKeys.DOUBLE, List.of(100.));
                }
                return true;
            })
    ));

    public GoldenBomb() {
        super(TRIGGER);
    }

    @Override
    public void makeSound(ServerPlayer player) {
        SoundEvent sound = SoundEvents.TNT_PRIMED;
        player.playNotifySound(sound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public void shrinkAfterUse(@NotNull ItemStack stack){
    }

}
