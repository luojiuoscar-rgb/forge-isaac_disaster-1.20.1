package net.luojiuoscar.isaac_disaster.manager.attack;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.event.custom.attack.PlayerPerformAttackEvent;
import net.luojiuoscar.isaac_disaster.manager.attack.managers.AttackType;
import net.luojiuoscar.isaac_disaster.manager.attack.types.BulletAttack;
import net.luojiuoscar.isaac_disaster.manager.attack.types.LaserAttack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class AttackManager {
    private static final AttackManager INSTANCE = new AttackManager();
    private AttackManager() {}
    public static AttackManager getInstance() {
        return INSTANCE;
    }

    private final Map<Integer, IAttackType> attacks = new HashMap<>();

    public void registerAttack(IAttackType item) {
        int itemId = item.getId();
        if (attacks.containsKey(itemId)) {
            throw new IllegalArgumentException("攻击类型ID已存在: " + itemId);
        }
        attacks.put(itemId, item);
    }

    public void registerAttacks(IAttackType... items) {
        for (IAttackType item : items) {
            registerAttack(item);
        }
    }

    // =========== 工具方法 ===========
    public void playerPerformAttack(ServerPlayer player) {
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(playerAbility -> {
            int attackId = playerAbility.getBestBulletType();
            ResourceLocation colorRl = playerAbility.getBestBulletColor();
            Map<ResourceLocation, Integer> trajectories = playerAbility.getTrajectories();

            var context = new IAttackType.AttackContext(colorRl, new HashSet<>(), trajectories);

            PlayerPerformAttackEvent event = new PlayerPerformAttackEvent(player, attackId, context);
            MinecraftForge.EVENT_BUS.post(event);

            attackId = event.getAttackTypeId();
            context = event.getContext();

            IAttackType attack = attacks.getOrDefault(attackId, attacks.get(AttackType.BULLET.getId()));
            if (attack != null) {
                attack.performAttack(player, context);
            }
        });
    }



    public void init() {
        // 注册所有道具实例
        registerAttacks(
                new BulletAttack(),
                new LaserAttack()
        );
    }
}