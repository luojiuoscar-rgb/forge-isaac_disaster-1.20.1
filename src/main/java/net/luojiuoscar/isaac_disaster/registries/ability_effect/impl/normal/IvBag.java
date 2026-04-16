package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.entity.player.Player;

public class IvBag implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof Player player)) return false;

        if (player.invulnerableTime > 0) return true;

        float health = player.getHealth();
        float damage = (float) (StatManager.MAX_HEALTH.getBonus() * 0.5);

        // 红心足够时，优先扣除红心
        if (health > damage){
            player.setHealth(health - damage);
            // 虚假伤害效果
            player.hurt(PlayerHelper.getSelfDamageSource(player), 0.01f);
        }
        // 不足时，直接伤害（扣除黄心或直接致死）
        else{
            player.hurt(PlayerHelper.getSelfDamageSource(player), damage);
        }

        // 缩短受伤冷却时间
        player.invulnerableTime = 10;
        LootHelper.spawnLootAtPos(player, player.position(), LootTableManager.RANDOM_COINS);
        return true;
    }
}