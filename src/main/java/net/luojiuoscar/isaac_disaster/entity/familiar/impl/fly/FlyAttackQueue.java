package net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly;

import net.luojiuoscar.isaac_disaster.helper.TrackingTargetRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.BiPredicate;

/** Owner-wide attack queue, planner and movement trail shared by all fly variants. */
final class FlyAttackQueue {
    private static final double GAP = 0.3;
    private static final double EPSILON = 1e-7;
    private final LinkedHashMap<UUID, Member> members = new LinkedHashMap<>();
    private final Map<UUID, Long> unreachableUntil = new HashMap<>();
    private final FlyAttackTrail trail = new FlyAttackTrail();
    private final FlyNavigation.RoutePlanner navigation = new FlyNavigation.RoutePlanner(
            (from, to) -> head() != null && FlyNavigation.clearPath(head().fly, from, to));
    private LivingEntity target;
    private Member leader;
    private long attemptedTick = Long.MIN_VALUE;
    private long budgetTick = Long.MIN_VALUE;
    private long admissionTick = Long.MIN_VALUE;
    private UUID nextCandidate;
    private int nextCandidateIndex;
    private final BiPredicate<ServerPlayer, LivingEntity> validTarget;

    FlyAttackQueue(BiPredicate<ServerPlayer, LivingEntity> validTarget) {
        this.validTarget = validTarget;
    }

    private Member head() {
        return leader;
    }

    void refresh(ServerPlayer owner, List<AbstractFlyFamiliarEntity> flies,
                 Predicate<UUID> registered, long tick) {
        unreachableUntil.entrySet().removeIf(entry -> entry.getValue() <= tick);
        if (target == null) return;
        if (!validTarget.test(owner, target)) {
            stop();
            return;
        }
        Member oldHead = head();
        members.values().removeIf(member -> member.fly.isRemoved() || member.fly.isRecallPending()
                || !registered.test(member.fly.getUUID()));
        admitNext(flies, tick);
        reconnect();
        if (head() != oldHead) navigation.clear();
        if (members.isEmpty()) return;
        double keepFrom = trail.endDistance();
        for (Member member : members.values()) {
            keepFrom = Math.min(keepFrom, member.joined ? member.progress : trail.startDistance());
        }
        trail.discardBefore(keepFrom);
        for (Member member : members.values()) {
            if (member.progress < trail.startDistance() - EPSILON) member.joined = false;
        }
    }

    LivingEntity target(ServerPlayer owner, AbstractFlyFamiliarEntity caller,
                        List<AbstractFlyFamiliarEntity> flies, List<LivingEntity> candidates, long tick) {
        if (target != null && !validTarget.test(owner, target)) stop();
        if (target != null) return members.containsKey(caller.getUUID()) ? target : null;
        if (attemptedTick == tick || admissionTick == tick) return null;
        attemptedTick = tick;
        LivingEntity best = null;
        int bestPriority = Integer.MAX_VALUE;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : candidates) {
            if (unreachableUntil.containsKey(candidate.getUUID()) || !validTarget.test(owner, candidate)) continue;
            int priority = TrackingTargetRules.priority(owner.serverLevel(), owner, candidate);
            double distance = candidate.getBoundingBox().getCenter().distanceToSqr(caller.position());
            if (priority < bestPriority || priority == bestPriority && distance < bestDistance) {
                best = candidate;
                bestPriority = priority;
                bestDistance = distance;
            }
        }
        if (best == null) return null;
        int leaderIndex = -1;
        double nearestDistance = Double.MAX_VALUE;
        Vec3 targetCenter = best.getBoundingBox().getCenter();
        for (int i = 0; i < flies.size(); i++) {
            AbstractFlyFamiliarEntity fly = flies.get(i);
            if (fly.isRemoved() || fly.isRecallPending()) continue;
            double distance = fly.getBoundingBox().getCenter().distanceToSqr(targetCenter);
            if (distance < nearestDistance) {
                leaderIndex = i;
                nearestDistance = distance;
            }
        }
        if (leaderIndex < 0) return null;
        AbstractFlyFamiliarEntity first = flies.get(leaderIndex);
        target = best;
        trail.start(first.position());
        members.put(first.getUUID(), new Member(first));
        admissionTick = tick;
        rememberNext(flies, leaderIndex);
        reconnect();
        head().joined = true;
        return caller == first ? target : null;
    }

    private void admitNext(List<AbstractFlyFamiliarEntity> flies, long tick) {
        if (admissionTick == tick || members.size() >= flies.size()) return;
        int start = Math.min(nextCandidateIndex, flies.size() - 1);
        for (int i = 0; i < flies.size(); i++) {
            if (flies.get(i).getUUID().equals(nextCandidate)) {
                start = i;
                break;
            }
        }
        for (int offset = 0; offset < flies.size(); offset++) {
            int index = Math.floorMod(start - offset, flies.size());
            AbstractFlyFamiliarEntity fly = flies.get(index);
            rememberNext(flies, index);
            var state = fly.getFlyState();
            if (fly.isRemoved() || fly.isRecallPending() || members.containsKey(fly.getUUID())
                    || state == AbstractFlyFamiliarEntity.FlyState.ROAM
                    || state == AbstractFlyFamiliarEntity.FlyState.CHASE) continue;
            Member member = new Member(fly);
            if (members.isEmpty()) {
                trail.start(fly.position());
                member.joined = true;
            }
            members.put(fly.getUUID(), member);
            admissionTick = tick;
            return;
        }
    }

    private void rememberNext(List<AbstractFlyFamiliarEntity> flies, int index) {
        nextCandidateIndex = Math.floorMod(index - 1, flies.size());
        nextCandidate = flies.get(nextCandidateIndex).getUUID();
    }

    List<Vec3> steps(AbstractFlyFamiliarEntity fly, long tick) {
        Member member = members.get(fly.getUUID());
        if (member == null || target == null) return List.of();
        boolean leader = member == head();
        double speed = leader ? FlyOrbitLayout.ATTACK_SPEED : FlyOrbitLayout.MAX_SPEED;
        if (!member.joined) {
            Vec3 delta = trail.entrance().subtract(fly.position());
            if (delta.lengthSqr() > EPSILON * EPSILON) {
                return List.of(delta.scale(Math.min(1, speed / delta.length())));
            }
            if (member.previous != null && (!member.previous.joined
                    || member.previous.progress < trail.startDistance() + GAP)) return List.of();
            member.joined = true;
            member.progress = trail.startDistance();
        }
        double limit = leader ? trail.endDistance()
                : member.previous.joined ? member.previous.progress - GAP : member.progress;
        if (!leader || member.progress < trail.endDistance() - EPSILON) {
            return trail.steps(member.progress, limit, speed);
        }
        Vec3 goal = target.getBoundingBox().getCenter().add(0, -fly.getBbHeight() * 0.5, 0);
        int budget = tick == budgetTick ? 0 : FlyNavigation.TICK_NODE_BUDGET;
        budgetTick = tick;
        List<Vec3> result = navigation.nextSteps(fly.position(), goal, tick, budget, speed);
        if (navigation.failed()) {
            unreachableUntil.put(target.getUUID(), tick + 20);
            stop();
        }
        return result;
    }

    void moved(AbstractFlyFamiliarEntity fly, Vec3 from, Vec3 to) {
        Member member = members.get(fly.getUUID());
        if (member == null) return;
        if (!member.joined) {
            if (to.distanceToSqr(trail.entrance()) < EPSILON * EPSILON) {
                member.joined = true;
                member.progress = trail.startDistance();
            }
        } else if (member == head() && member.progress >= trail.endDistance() - EPSILON) {
            member.progress = trail.append(to);
        } else {
            member.progress = Math.min(trail.endDistance(), member.progress + from.distanceTo(to));
        }
    }

    void blocked(AbstractFlyFamiliarEntity fly) {
        if (head() != null && head().fly == fly) navigation.clear();
    }

    void remove(AbstractFlyFamiliarEntity fly) {
        Member oldHead = head();
        members.remove(fly.getUUID());
        if (members.isEmpty()) {
            leader = null;
            navigation.clear();
        }
        else {
            reconnect();
            if (head() != oldHead) navigation.clear();
        }
    }

    private void reconnect() {
        Member previous = null;
        leader = null;
        for (Member member : members.values()) {
            if (previous == null) leader = member;
            member.previous = previous;
            previous = member;
        }
    }

    private void stop() {
        target = null;
        leader = null;
        members.clear();
        trail.clear();
        navigation.clear();
    }

    private static final class Member {
        final AbstractFlyFamiliarEntity fly;
        Member previous;
        double progress;
        boolean joined;

        Member(AbstractFlyFamiliarEntity fly) { this.fly = fly; }
    }
}
