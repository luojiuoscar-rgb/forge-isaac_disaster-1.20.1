package net.luojiuoscar.isaac_disaster.helper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

/** Shared hostility ordering with source-specific candidate eligibility. */
public final class TrackingTargetRules {
    private TrackingTargetRules() {
    }

    /** Preserves the existing bullet ordering before distance is considered. */
    public static int priority(ServerLevel level, LivingEntity owner, LivingEntity candidate) {
        if (candidate instanceof Enemy) {
            boolean aggro = false;
            if (candidate instanceof Monster monster) {
                aggro = monster.getTarget() == owner;
                if (monster instanceof NeutralMob neutral && owner instanceof Player player) {
                    aggro |= neutral.isAngryAt(player);
                }
            }
            return aggro ? 0 : 1;
        }
        if (candidate instanceof Mob) return 2;
        if (candidate instanceof Player player && owner instanceof Player ownerPlayer
                && level.getServer().isPvpAllowed() && ownerPlayer.canHarmPlayer(player)
                && !ownerPlayer.isAlliedTo(player)) return 3;
        return Integer.MAX_VALUE;
    }

    /** Preserves ordinary tracking eligibility, including nonhostile mobs. */
    public static boolean eligible(ServerLevel level, LivingEntity owner, LivingEntity candidate) {
        return candidate.isAlive() && candidate != owner && !candidate.isInvulnerable()
                && !EntityHelper.isFriendly(candidate, owner)
                && priority(level, owner, candidate) != Integer.MAX_VALUE;
    }

    /** Hostile/owner-threat/PvP eligibility shared by every fly and spider familiar variant. */
    public static boolean eligibleFlyOrSpider(ServerLevel level, ServerPlayer owner, LivingEntity candidate) {
        if (candidate instanceof Animal || !eligible(level, owner, candidate)) return false;
        if (candidate instanceof Player player) return !player.isCreative() && !player.isSpectator();
        return candidate instanceof Enemy
                || candidate instanceof Mob mob && mob.getTarget() == owner
                || candidate instanceof NeutralMob neutral && neutral.isAngryAt(owner);
    }
}
