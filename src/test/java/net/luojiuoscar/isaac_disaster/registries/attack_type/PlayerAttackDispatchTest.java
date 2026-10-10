package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbility;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.AdditionalAttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.BasicAttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.IChargeableAttack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PlayerAttackDispatchTest {
    @Test
    void ownedAttackSnapshotCannotMutateOrFollowTheInternalCounts() throws Exception {
        Basic primary = new Basic("primary", new HashMap<>());
        PlayerAbility ability = abilityWith(selection(primary, List.of()));
        Map<ResourceLocation, Integer> counts = new HashMap<>();
        counts.put(primary.getId(), 1);
        Field countsField = PlayerAbility.class.getDeclaredField("attackType");
        countsField.setAccessible(true);
        countsField.set(ability, counts);

        var snapshot = ability.getAttackTypes();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.put(primary.getId(), 2));
        assertEquals(1, counts.get(primary.getId()));
        counts.put(primary.getId(), 3);
        assertEquals(1, snapshot.get(primary.getId()));
        assertEquals(3, ability.getAttackTypes().get(primary.getId()));
    }

    @Test
    void primaryAndAdditionalReceiveEachTickAndInputOnce() throws Exception {
        Map<ResourceLocation, Integer> charge = new HashMap<>();
        Basic primary = new Basic("primary", charge);
        Extra additional = new Extra("additional", charge);
        PlayerAbility ability = abilityWith(selection(primary, List.of(additional)));
        ability.tickAttacks(null);
        ability.handleAttackInput(null, true);
        ability.handleAttackInput(null, false);
        for (Recording attack : List.of(primary, additional)) {
            assertEquals(1, attack.ticks);
            assertEquals(1, attack.presses);
            assertEquals(1, attack.releases);
            assertEquals(3, attack.syncs);
        }
    }

    @Test
    void switchingThePrimaryPreservesAdditionalCharge() throws Exception {
        Map<ResourceLocation, Integer> charge = new HashMap<>();
        Basic oldPrimary = new Basic("old_primary", charge);
        Basic newPrimary = new Basic("new_primary", charge);
        Extra additional = new Extra("additional", charge);
        charge.put(oldPrimary.getChargeBarId(), 20);
        charge.put(additional.getChargeBarId(), 30);
        PlayerAbility ability = abilityWith(selection(oldPrimary, List.of(additional)));
        replace(ability, selection(newPrimary, List.of(additional)));
        assertFalse(charge.containsKey(oldPrimary.getChargeBarId()));
        assertEquals(30, charge.get(additional.getChargeBarId()));
        assertEquals(0, additional.clears);
        assertSame(newPrimary, ability.getAttackSelection().mainAttack());
    }

    @Test
    void removingTheLastAdditionalClearsOnlyItsOwnBar() throws Exception {
        Map<ResourceLocation, Integer> charge = new HashMap<>();
        Basic primary = new Basic("primary", charge);
        Extra additional = new Extra("additional", charge);
        charge.put(primary.getChargeBarId(), 20);
        charge.put(additional.getChargeBarId(), 30);
        PlayerAbility ability = abilityWith(selection(primary, List.of(additional)));
        // A count decrease that leaves the attack participating must not reset its charge.
        replace(ability, selection(primary, List.of(additional)));
        assertEquals(0, additional.clears);
        replace(ability, selection(primary, List.of()));
        assertEquals(1, additional.clears);
        assertEquals(20, charge.get(primary.getChargeBarId()));
        assertFalse(charge.containsKey(additional.getChargeBarId()));
        ability.tickAttacks(null);
        assertEquals(0, additional.ticks);
    }

    private static AttackSelection selection(Basic primary, List<AttackType> additional) {
        AttackCandidate candidate = new AttackCandidate(primary.getId(), primary, primary.getId(), 1, 0, 0);
        return new AttackSelection(candidate, candidate, additional);
    }

    private static PlayerAbility abilityWith(AttackSelection selection) throws Exception {
        // Avoid Forge registry bootstrap; these cases exercise only the cached dispatch/cleanup.
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Object unsafe = unsafeField.get(null);
        PlayerAbility ability = (PlayerAbility) unsafeClass.getMethod("allocateInstance", Class.class)
                .invoke(unsafe, PlayerAbility.class);
        Field selectionField = PlayerAbility.class.getDeclaredField("attackSelection");
        selectionField.setAccessible(true);
        selectionField.set(ability, selection);
        return ability;
    }

    private static void replace(PlayerAbility ability, AttackSelection selection) throws Exception {
        Method method = PlayerAbility.class.getDeclaredMethod("replaceAttackSelection",
                AttackSelection.class, ServerPlayer.class);
        method.setAccessible(true);
        method.invoke(ability, selection, null);
    }

    private abstract static class Recording extends AttackType implements IChargeableAttack {
        private final ResourceLocation id;
        private final Map<ResourceLocation, Integer> charge;
        int ticks, presses, releases, syncs, clears;
        Recording(String name, Map<ResourceLocation, Integer> charge) {
            super(0);
            id = ResourceLocation.fromNamespaceAndPath("test", name);
            this.charge = charge;
        }
        @Override public ResourceLocation getId() { return id; }
        @Override public ResourceLocation getChargeBarId() { return id; }
        @Override public List<AttackContext> getAttackContexts(ServerPlayer player, int count) { return List.of(); }
        @Override public void performAttack(List<AttackContext> contexts) { }
        @Override public void makeSound(LivingEntity entity) { }
        @Override public void shoot(AttackContext context) { }
        @Override public int getTotalCharge(Player player) { return 47; }
        @Override public boolean isChargeEligible(ServerPlayer player) { return true; }
        @Override public void onTick(ServerPlayer player) { ticks++; }
        @Override public void onPressed(ServerPlayer player) { presses++; }
        @Override public void onReleased(ServerPlayer player) { releases++; }
        @Override public void syncCharge(ServerPlayer player) { syncs++; }
        @Override public void clearCharge(ServerPlayer player) { clears++; charge.remove(id); }
    }

    private static final class Basic extends Recording implements BasicAttackType {
        Basic(String name, Map<ResourceLocation, Integer> charge) { super(name, charge); }
    }
    private static final class Extra extends Recording implements AdditionalAttackType {
        Extra(String name, Map<ResourceLocation, Integer> charge) { super(name, charge); }
    }
}
