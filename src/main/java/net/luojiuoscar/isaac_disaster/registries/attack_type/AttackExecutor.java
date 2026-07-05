package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.event.custom.attack.BeforePerformAttackEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;

/**
 * Shared helper for common attack execution flows.
 *
 * <p>This class does not replace direct calls to {@link AttackType#performAttack(List)} or
 * {@link AttackType#shoot(AttackContext)}. It only centralizes the standard player-fired flow so
 * callers can opt into consistent before-event, context-event, and sound behavior.</p>
 */
public final class AttackExecutor {
    private AttackExecutor() {
    }

    /**
     * Performs a normal player attack with both before-attack and context events enabled.
     *
     * @return {@code true} if the attack was executed, or {@code false} if it was canceled
     */
    public static boolean performPrimary(ServerPlayer player, AttackType attack) {
        return perform(player, attack, true, true, true);
    }

    /**
     * Performs an attack with a caller-selected event policy.
     */
    public static boolean perform(ServerPlayer player, AttackType attack, boolean sendEvent,
                                  boolean fireContextEvent, boolean playSound) {
        if (sendEvent) {
            BeforePerformAttackEvent event = new BeforePerformAttackEvent(player, attack);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.isCanceled()) return false;
        }

        int bulletCount = attack.getBulletCount(player);
        List<AttackContext> contexts = fireContextEvent
                ? attack.getAttackContextsWithEvent(player, bulletCount)
                : attack.getAttackContexts(player, bulletCount);

        return performRaw(player, attack, contexts, playSound);
    }

    /**
     * Performs an attack from prebuilt contexts.
     */
    public static boolean performRaw(ServerPlayer player, AttackType attack, List<AttackContext> contexts,
                                     boolean playSound) {
        attack.performAttack(contexts);
        if (playSound) attack.makeSound(player);
        return true;
    }
}
