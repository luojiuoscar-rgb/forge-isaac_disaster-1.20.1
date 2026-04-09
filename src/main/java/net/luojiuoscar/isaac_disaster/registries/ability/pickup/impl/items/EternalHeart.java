package net.luojiuoscar.isaac_disaster.registries.ability.pickup.impl.items;

import net.luojiuoscar.isaac_disaster.registries.ability.pickup.FoodPickupAbility;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;

import java.util.List;


public class EternalHeart extends FoodPickupAbility {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModExecutableEffects.ETERNAL_HEART)
    ));

    public EternalHeart() {
        super(TRIGGER);
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.ETERNAL_HEART.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}

