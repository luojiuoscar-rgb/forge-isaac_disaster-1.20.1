package net.luojiuoscar.isaac_disaster.registries.ability.pickup.impl.cards;

import net.luojiuoscar.isaac_disaster.registries.ability.pickup.PickupAbility;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.List;

public class PokersBack extends PickupAbility {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModExecutableEffects.POKERS_BACK)
    ));

    public PokersBack() {
        super(TRIGGER);
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public List<Component> getDesc() {
        List<Component> description = new ArrayList<>();

        description.add(Component.translatable("item.isaac_disaster.tarots_back.lore.1"));

        return description;
    }
}

