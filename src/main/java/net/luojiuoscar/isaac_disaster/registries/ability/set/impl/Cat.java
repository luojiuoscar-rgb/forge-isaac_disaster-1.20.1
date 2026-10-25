package net.luojiuoscar.isaac_disaster.registries.ability.set.impl;

import net.luojiuoscar.isaac_disaster.registries.ability.set.SetAbility;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.helper.DescriptionHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import static net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal.Cat.chance;

public class Cat extends SetAbility {
    public Cat(int id) {
        super(Component.translatable("set.isaac_disaster.cat"), id, 3);
    }

    @Override
    public void onFirstObtain(Player player) {
    }

    @Override
    public void onObtainEffect(Player player) {
        StatManager.FLY_TIME.apply(player, 1);
        StatManager.addTriggerModule(player, ModTriggerModules.CAT.getId(), 1);
    }

    @Override
    public void onRemoveEffect(Player player) {
        StatManager.FLY_TIME.apply(player, -1);
        StatManager.addTriggerModule(player, ModTriggerModules.CAT.getId(), -1);
    }

    @Override
    public List<Component> getExtraDesc() {
        return List.of(
                getName().copy().append(": ").append(StatManager.FLY_TIME.description(1)),
                Component.translatable("set.isaac_disaster.cat.lore.1"),
                Component.translatable("set.isaac_disaster.cat.lore.2", DescriptionHelper.dynamicNumber(chance() * 100))
        );
    }
}
