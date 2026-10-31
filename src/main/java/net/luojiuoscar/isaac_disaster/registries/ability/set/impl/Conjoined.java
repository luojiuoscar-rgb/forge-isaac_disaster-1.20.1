package net.luojiuoscar.isaac_disaster.registries.ability.set.impl;

import net.luojiuoscar.isaac_disaster.registries.ability.set.SetAbility;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class Conjoined extends SetAbility {
    public Conjoined(int id) { super(Component.translatable("set.isaac_disaster.conjoined"), id, 3); }

    @Override public void onFirstObtain(Player player) { }
    @Override
    public void onObtainEffect(Player player) {
        StatManager.TEARS.apply(player, -0.3);
        StatManager.DAMAGE.apply(player, -0.3);
        StatManager.addTriggerModule(player, ModTriggerModules.CONJOINED.getId(), 1);
    }

    @Override
    public void onRemoveEffect(Player player) {
        StatManager.TEARS.apply(player, 0.3);
        StatManager.DAMAGE.apply(player, 0.3);
        StatManager.addTriggerModule(player, ModTriggerModules.CONJOINED.getId(), -1);
    }

    @Override
    public List<Component> getExtraDesc() {
        return List.of(
                getName().copy().append(": ").append(Component.translatable("set.isaac_disaster.conjoined.lore.1")),
                StatManager.TEARS.description(-0.3),
                StatManager.DAMAGE.description(-0.3)
        );
    }
}
