package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.entity.player.Player;

public class IvBag implements IAbilityEffect {
    @Override
    public void apply(AbilityEffectContext context) {
        if (!(context.getEntity() instanceof Player player)) return;

        if (player.invulnerableTime > 0) return;

        float health = player.getHealth();
        float damage = (float) (StatManager.MAX_HEALTH.getBonus() * 0.5);

        // 红心足够时，优先扣除红心
        if (health > damage){
            player.setHealth(health - damage);
            // 虚假伤害效果
            player.hurt(player.damageSources().genericKill(), 0.01f);
        }
        // 不足时，直接伤害
        else{
            player.hurt(player.damageSources().genericKill(), damage);
        }

        // 缩短受伤冷却时间
        player.invulnerableTime = 10;
        LootHelper.spawnLootAtPos(player, player.position(), LootTableManager.RANDOM_COINS);
    }
}