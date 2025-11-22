package net.luojiuoscar.isaac_disaster.registries.recursive_module.impl;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.IRecursiveModule;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModuleQueue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MoneyIsPower implements IRecursiveModule {
    private static final UUID MONEY_IS_POWER_ADDER =
            UUID.nameUUIDFromBytes(("isaac_disaster:money_is_power_adder").getBytes(StandardCharsets.UTF_8));


    @Override
    public int getTickInterval(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {
        return 20;
    }

    @Override
    public void recursiveEffect(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {
        if (!(entity instanceof Player player)) return;

        int money = PlayerHelper.countMoney(player);
        double damage = money * Config.MONEY_IS_POWER_STRENGTH.get();
        StatManager.setModifier(player, MONEY_IS_POWER_ADDER, Attributes.ATTACK_DAMAGE,
                damage, null, null, 0);
    }

    @Override
    public void handleRemove(LivingEntity entity){
        AttributeInstance instance = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (instance == null) return;
        instance.removeModifier(MONEY_IS_POWER_ADDER);
    }
}
