package net.luojiuoscar.isaac_disaster.registries.ability.pickup.impl.items;

import net.luojiuoscar.isaac_disaster.registries.ability.pickup.PickupAbility;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;

import java.util.List;


public class BlendedHeart extends PickupAbility {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModExecutableEffects.BLENDED_HEART)
    ));

    public BlendedHeart() {
        super(TRIGGER);
    }

    @Override
    public void makeSound(ServerPlayer player) {
        double health = player.getMaxHealth() - player.getHealth();
        if (health > 0) {
            player.playNotifySound(ModSounds.RED_HEART.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        }else {
            player.playNotifySound(ModSounds.SOUL_HEART.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }
}

