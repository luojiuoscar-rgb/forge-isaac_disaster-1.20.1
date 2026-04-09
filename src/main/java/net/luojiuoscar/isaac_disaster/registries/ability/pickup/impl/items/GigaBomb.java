package net.luojiuoscar.isaac_disaster.registries.ability.pickup.impl.items;

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

import java.util.List;

public class GigaBomb extends PickupAbility {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModExecutableEffects.THROW_GIGA_BOMB),
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModExecutableEffects.ADD_COOLDOWN_TO_ITEM,
                    context -> {
                context.set(ContextKeys.DOUBLE, List.of(50.));
                return true;
            })
    ));

    public GigaBomb() {
        super(TRIGGER);
    }

    @Override
    public void makeSound(ServerPlayer player) {
        SoundEvent sound = SoundEvents.TNT_PRIMED;
        player.playNotifySound(sound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}

