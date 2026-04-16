package net.luojiuoscar.isaac_disaster.registries.ability.pickup.impl.items;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.PickupAbility;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.minecraft.server.level.ServerPlayer;


public class DoubleBomb extends PickupAbility {
    public DoubleBomb() {
        super(CompositeTrigger.EMPTY);
    }

    @Override
    public void fire(ExecutableEffectContext context) {
        if (context.getEntity() instanceof ServerPlayer player){
            PlayerHelper.giveItem(player, ModItems.BOMB.get(), 2);
        }
    }

    @Override
    public void makeSound(ServerPlayer player) {

    }
}

