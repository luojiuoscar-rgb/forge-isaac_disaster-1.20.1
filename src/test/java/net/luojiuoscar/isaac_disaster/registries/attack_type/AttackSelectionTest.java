package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.AdditionalAttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.BasicAttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.DelegatingAttackType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AttackSelectionTest {
    private final Basic bullet = new Basic("bullet", 0);
    private final AttackCandidate fallback = candidate(bullet, 0, 0, 1);

    @Test
    void basicAndDelegatingCompeteButTheDelegatePayloadIsAlwaysBasic() {
        Basic laser = new Basic("laser", 100);
        Delegate eye = new Delegate("eye", 200);
        Delegate neptunus = new Delegate("neptunus", 10);
        AttackSelection selection = select(List.of(candidate(laser, 100, 0, 1),
                candidate(eye, 200, 0, 1), candidate(neptunus, 200, 1, 2)));
        assertSame(neptunus, selection.mainAttack());
        assertSame(laser, selection.baseAttack());
        assertEquals(200, selection.mainCandidate().priorityTier());
        assertEquals(1, selection.mainCandidate().priority());
        assertFalse(selection.baseAttack() instanceof DelegatingAttackType);
    }

    @Test
    void strongerBasicAttackWinsAgainstTheDelegate() {
        Basic laser = new Basic("laser", 100);
        Delegate delegate = new Delegate("delegate", 10);
        AttackSelection selection = select(List.of(candidate(laser, 100, 0, 1),
                candidate(delegate, 0, 10, 1)));
        assertSame(laser, selection.mainAttack());
        assertSame(laser, selection.baseAttack());
    }

    @Test
    void brimstoneOverridesEitherDelegateAndTheirCombination() {
        Basic brimstone = new Basic("brimstone", 0);
        Delegate eye = new Delegate("cursed_eye", 0);
        Delegate neptunus = new Delegate("neptunus", 0);
        AttackCandidate beam = candidate(brimstone, AttackPrio.BRIMSTONE, 1);
        AttackCandidate curse = candidate(eye, AttackPrio.CURSED_EYE, 1);
        AttackCandidate reserve = candidate(neptunus, AttackPrio.NEPTUNUS, 1);
        AttackCandidate beamOverride = candidate(brimstone, AttackPrio.BRIMSTONE_CURSED_EYE_COMBO, 2);
        AttackCandidate reserveCombo = candidate(neptunus, AttackPrio.NEPTUNUS_CURSED_EYE_COMBO, 2);
        AttackCandidate reserveLaserCombo = candidate(neptunus, AttackPrio.NEPTUNUS_LASER_COMBO, 2);
        Basic laser = new Basic("laser", 0);

        for (List<AttackCandidate> candidates : List.of(
                List.of(beam, reserve),
                List.of(beam, curse, beamOverride),
                List.of(beam, curse, reserve, beamOverride, reserveCombo),
                List.of(beam, curse, reserve, beamOverride, reserveCombo, reserveLaserCombo,
                        candidate(laser, AttackPrio.LASER, 1)))) {
            AttackSelection selection = select(candidates);
            assertSame(brimstone, selection.mainAttack());
            assertSame(brimstone, selection.baseAttack());
        }
    }

    @Test
    void cSectionOverridesCursedEyeIncludingExistingDelegateAndLaserCombinations() {
        Basic fetus = new Basic("c_section", 0);
        Basic laser = new Basic("laser", 0);
        Delegate eye = new Delegate("cursed_eye", 0);
        Delegate neptunus = new Delegate("neptunus", 0);
        AttackCandidate section = candidate(fetus, AttackPrio.C_SECTION, 1);
        AttackCandidate curse = candidate(eye, AttackPrio.CURSED_EYE, 1);
        AttackCandidate override = candidate(fetus, AttackPrio.C_SECTION_CURSED_EYE_COMBO, 2);
        AttackCandidate reserve = candidate(neptunus, AttackPrio.NEPTUNUS, 1);
        AttackCandidate reserveCombo = candidate(neptunus, AttackPrio.NEPTUNUS_CURSED_EYE_COMBO, 2);

        for (List<AttackCandidate> candidates : List.of(
                List.of(section, curse, override),
                List.of(section, curse, override, reserve, reserveCombo),
                List.of(section, curse, override, reserve, reserveCombo,
                        candidate(laser, AttackPrio.LASER, 1),
                        candidate(fetus, AttackPrio.C_SECTION_LASER_COMBO, 2),
                        candidate(neptunus, AttackPrio.NEPTUNUS_LASER_COMBO, 2)))) {
            AttackSelection selection = select(candidates);
            assertSame(fetus, selection.mainAttack());
            assertSame(fetus, selection.baseAttack());
        }
    }

    @Test
    void brimstoneOverrideWinsAgainstCSectionOverrideButPreservesTheHaemolacriaCombination() {
        Basic brimstone = new Basic("brimstone", 0);
        Basic fetus = new Basic("c_section", 0);
        Delegate eye = new Delegate("cursed_eye", 0);
        Delegate neptunus = new Delegate("neptunus", 0);
        List<AttackCandidate> candidates = List.of(
                candidate(brimstone, AttackPrio.BRIMSTONE, 1),
                candidate(fetus, AttackPrio.C_SECTION, 1),
                candidate(eye, AttackPrio.CURSED_EYE, 1),
                candidate(neptunus, AttackPrio.NEPTUNUS, 1),
                candidate(neptunus, AttackPrio.NEPTUNUS_CURSED_EYE_COMBO, 2),
                candidate(brimstone, AttackPrio.BRIMSTONE_CURSED_EYE_COMBO, 2),
                candidate(fetus, AttackPrio.C_SECTION_CURSED_EYE_COMBO, 2));
        AttackSelection selection = select(candidates);
        assertSame(brimstone, selection.mainAttack());
        assertSame(brimstone, selection.baseAttack());

        Basic haemolacria = new Basic("haemolacria", 0);
        List<AttackCandidate> withHaemolacria = new ArrayList<>(candidates);
        withHaemolacria.add(candidate(haemolacria, AttackPrio.HAEMOLACRIA, 1));
        withHaemolacria.add(candidate(fetus, AttackPrio.HAEMOLACRIA_C_SECTION_COMBO, 2));
        selection = select(withHaemolacria);
        assertSame(fetus, selection.mainAttack());
        assertSame(fetus, selection.baseAttack());
    }

    @Test
    void combinationPriorityAndSpecificityArePreservedForBaseSelection() {
        Basic laser = new Basic("laser", 100);
        Basic fetus = new Basic("fetus", 0);
        AttackCandidate combo = candidate(fetus, 100, 0, 2);
        AttackSelection selection = select(List.of(candidate(laser, 100, 0, 1), combo));
        assertSame(fetus, selection.mainAttack());
        assertSame(combo, selection.baseCandidate());
    }

    @Test
    void additionalAttacksCannotWinAndRunOnceInStableIdOrder() {
        Extra z = new Extra("z", 1000);
        Extra a = new Extra("a", 2000);
        AttackSelection selection = select(List.of(candidate(z, 1000, 0, 1),
                candidate(a, 2000, 0, 1), candidate(z, 3000, 0, 3)));
        assertSame(bullet, selection.mainAttack());
        assertSame(bullet, selection.baseAttack());
        assertEquals(List.of(a, z), selection.additionalAttacks());
        assertThrows(UnsupportedOperationException.class, () -> selection.additionalAttacks().clear());
    }

    @Test
    void inactiveAttacksDoNotParticipateAndTheBaseFallbackRemainsAvailable() {
        Basic disabledBase = new Basic("disabled", 500) {
            @Override public boolean isActive(AttackSelectionContext context) { return false; }
        };
        Extra disabledExtra = new Extra("disabled_extra", 1000) {
            @Override public boolean isActive(AttackSelectionContext context) { return false; }
        };
        Delegate delegate = new Delegate("delegate", 100);
        AttackSelection selection = select(List.of(candidate(disabledBase, 500, 0, 1),
                candidate(disabledExtra, 1000, 0, 1), candidate(delegate, 100, 0, 1)));
        assertSame(delegate, selection.mainAttack());
        assertSame(bullet, selection.baseAttack());
        assertTrue(selection.additionalAttacks().isEmpty());
    }

    @Test
    void missingAndOverlappingClassificationsNameTheInvalidAttack() {
        Stub missing = new Stub("missing", 0);
        Basic overlapping = new Overlapping();
        for (Stub attack : List.of(missing, overlapping)) {
            IllegalStateException error = assertThrows(IllegalStateException.class,
                    () -> select(List.of(candidate(attack, 0, 0, 1))));
            assertTrue(error.getMessage().contains(attack.getId().toString()));
        }
    }

    private AttackSelection select(List<AttackCandidate> candidates) {
        return AttackSelector.selectCandidates(new AttackSelectionContext(Map.of(), null), fallback, candidates);
    }

    private static AttackCandidate candidate(AttackType attack, int tier, double priority, int required) {
        return new AttackCandidate(attack.getId(), attack, attack.getId(), required, tier, priority);
    }

    private static AttackCandidate candidate(AttackType attack, AttackPrio priority, int required) {
        return candidate(attack, priority.getTier(), priority.getPriority(), required);
    }

    private static class Stub extends AttackType {
        private final ResourceLocation id;
        Stub(String name, double priority) {
            super(priority);
            id = ResourceLocation.fromNamespaceAndPath("test", name);
        }
        @Override public ResourceLocation getId() { return id; }
        @Override public List<AttackContext> getAttackContexts(ServerPlayer player, int count) { return List.of(); }
        @Override public void performAttack(List<AttackContext> contexts) { }
        @Override public void makeSound(LivingEntity entity) { }
        @Override public void shoot(AttackContext context) { }
    }

    private static class Basic extends Stub implements BasicAttackType {
        Basic(String name, double priority) { super(name, priority); }
    }
    private static class Delegate extends Stub implements DelegatingAttackType {
        Delegate(String name, double priority) { super(name, priority); }
    }
    private static class Extra extends Stub implements AdditionalAttackType {
        Extra(String name, double priority) { super(name, priority); }
    }
    private static final class Overlapping extends Basic implements AdditionalAttackType {
        Overlapping() { super("overlapping", 0); }
    }
}
