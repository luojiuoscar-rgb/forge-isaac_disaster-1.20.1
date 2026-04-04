package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MoneyIsPower implements IAbilityEffect {
    public static final UUID MONEY_IS_POWER_ADDER =
            UUID.nameUUIDFromBytes(("isaac_disaster:money_is_power_adder").getBytes(StandardCharsets.UTF_8));

    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return true;

        int money = PlayerHelper.countMoney(player);
        double damage = money * Config.MONEY_IS_POWER_STRENGTH.get();
        StatManager.setModifier(player, MONEY_IS_POWER_ADDER, Attributes.ATTACK_DAMAGE,
                damage, null, null, 0);
        return true;
    }
}
