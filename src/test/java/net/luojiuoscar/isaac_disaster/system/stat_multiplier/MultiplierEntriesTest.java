package net.luojiuoscar.isaac_disaster.system.stat_multiplier;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiplierEntriesTest {
    private static final ResourceLocation ATTRIBUTE = ResourceLocation.parse("minecraft:generic.attack_damage");
    private static final ResourceLocation CRICKET = ResourceLocation.parse("isaac_disaster:crickets_body");
    private static final ResourceLocation BLOOD = ResourceLocation.parse("isaac_disaster:haemolacria");
    private static final MultiplierEntry BLOOD_RANGE = entry("blood_range", BLOOD, -0.2,
            AttributeModifier.Operation.MULTIPLY_TOTAL);

    @Test
    void oneUuidTracksZeroOneTwoOneZeroStacksAndConfigChanges() {
        MultiplierEntries entries = new MultiplierEntries();
        MultiplierRules rules = new MultiplierRules();
        AttributeInstance attribute = attribute();

        entries.add(BLOOD_RANGE, 1);
        entries.reconcile(ignored -> attribute, false, rules);
        assertEquals(8.0, attribute.getValue(), 1.0E-12);
        assertEquals(-0.2, attribute.getModifier(BLOOD_RANGE.uuid()).getAmount(), 1.0E-12);

        entries.add(BLOOD_RANGE, 1);
        entries.reconcile(ignored -> attribute, false, rules);
        assertEquals(2, entries.snapshot().get(BLOOD_RANGE.id()).stacks());
        assertEquals(1, attribute.getModifiers().size());
        assertEquals(8.0, attribute.getValue(), 1.0E-12);

        entries.reconcile(ignored -> attribute, true, rules);
        assertEquals(-0.36, attribute.getModifier(BLOOD_RANGE.uuid()).getAmount(), 1.0E-12);
        assertEquals(6.4, attribute.getValue(), 1.0E-12);
        assertEquals(1, attribute.getModifiers().size());

        entries.remove(BLOOD_RANGE.id(), 1);
        entries.reconcile(ignored -> attribute, true, rules);
        assertEquals(8.0, attribute.getValue(), 1.0E-12);
        entries.remove(BLOOD_RANGE.id(), 1);
        entries.reconcile(ignored -> attribute, true, rules);
        assertTrue(entries.snapshot().isEmpty());
        assertNull(attribute.getModifier(BLOOD_RANGE.uuid()));
    }

    @Test
    void multiplyBaseAggregatesAdditivelyAndTotalMultipliesPerCopy() {
        MultiplierEntry base = entry("base", BLOOD, -0.2, AttributeModifier.Operation.MULTIPLY_BASE);
        MultiplierEntries entries = new MultiplierEntries();
        AttributeInstance attribute = attribute();
        attribute.addPermanentModifier(new AttributeModifier(UUID.randomUUID(), "legacy", 2.0,
                AttributeModifier.Operation.ADDITION));
        entries.add(base, 2);
        entries.reconcile(ignored -> attribute, true, new MultiplierRules());
        assertEquals(-0.4, attribute.getModifier(base.uuid()).getAmount(), 1.0E-12);
        assertEquals(7.2, attribute.getValue(), 1.0E-12);

        entries.remove(base.id(), 2);
        entries.add(BLOOD_RANGE, 2);
        entries.reconcile(ignored -> attribute, true, new MultiplierRules());
        assertEquals(7.68, attribute.getValue(), 1.0E-12);
        assertNull(attribute.getModifier(base.uuid()));
    }

    @Test
    void heldRulesSuppressAndRestoreWithoutRequiringAMultiplierFromTheirSource() {
        MultiplierRules rules = new MultiplierRules();
        MultiplierRules.RuleEntry rule = rule("cricket_blocks_blood", CRICKET, BLOOD_RANGE.id());
        MultiplierEntries entries = new MultiplierEntries();
        AttributeInstance attribute = attribute();

        entries.add(BLOOD_RANGE, 1);
        entries.reconcile(ignored -> attribute, false, rules);
        assertEquals(8.0, attribute.getValue(), 1.0E-12);
        rules.add(rule, 2);
        entries.reconcile(ignored -> attribute, false, rules);
        assertNull(attribute.getModifier(BLOOD_RANGE.uuid()));
        rules.remove(rule.id(), 1);
        entries.reconcile(ignored -> attribute, true, rules);
        assertNull(attribute.getModifier(BLOOD_RANGE.uuid()));
        rules.remove(rule.id(), 1);
        entries.reconcile(ignored -> attribute, false, rules);
        assertEquals(8.0, attribute.getValue(), 1.0E-12);
    }

    @Test
    void rulesAreIndependentOfOrderAndOfOtherSuppressedEntries() {
        MultiplierEntry cricketRange = entry("cricket_range", CRICKET, -0.2,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        MultiplierRules rules = new MultiplierRules();
        rules.add(rule("blood_blocks_cricket", BLOOD, cricketRange.id()), 1);
        rules.add(rule("cricket_blocks_blood", CRICKET, BLOOD_RANGE.id()), 1);
        MultiplierEntries entries = new MultiplierEntries();
        entries.add(cricketRange, 1);
        entries.add(BLOOD_RANGE, 1);
        AttributeInstance attribute = attribute();

        entries.reconcile(ignored -> attribute, false, rules);
        assertEquals(10.0, attribute.getValue(), 1.0E-12);
        assertTrue(attribute.getModifiers().isEmpty());
        assertEquals(2, entries.snapshot().size());
        rules.remove(rule("cricket_blocks_blood", CRICKET, BLOOD_RANGE.id()).id(), 1);
        entries.reconcile(ignored -> attribute, false, rules);
        assertEquals(8.0, attribute.getValue(), 1.0E-12);
        assertNull(attribute.getModifier(cricketRange.uuid()));
    }

    @Test
    void distinctEntriesFromOneSourceKeepDistinctUuids() {
        MultiplierEntries entries = new MultiplierEntries();
        MultiplierEntry damage = entry("blood_damage", BLOOD, 0.5,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        entries.add(BLOOD_RANGE, 1);
        entries.add(damage, 1);
        AttributeInstance attribute = attribute();
        entries.reconcile(ignored -> attribute, false, new MultiplierRules());
        assertEquals(2, attribute.getModifiers().size());
        assertEquals(12.0, attribute.getValue(), 1.0E-12);
    }

    @Test
    void capabilityNbtAndCopyKeepRulesSeparateFromLegacyModifiers() {
        PlayerStatModifier original = new PlayerStatModifier();
        UUID legacyId = UUID.randomUUID();
        original.setModifierValue(legacyId, 2.0, null, null, Attributes.ATTACK_DAMAGE, 0);
        original.getMultiplierEntries().add(BLOOD_RANGE, 2);
        MultiplierRules.RuleEntry rule = rule("cricket_blocks_blood", CRICKET, BLOOD_RANGE.id());
        original.getMultiplierRules().add(rule, 2);
        CompoundTag saved = new CompoundTag();
        original.saveNBTData(saved);

        assertEquals(1, saved.getList("player_modifiers", 10).size());
        assertEquals(1, saved.getList("multiplier_entries", 10).size());
        assertEquals(1, saved.getList("multiplier_rules", 10).size());
        assertTrue(saved.getList("multiplier_entries", 10).getCompound(0).hasUUID("uuid"));

        PlayerStatModifier restored = new PlayerStatModifier();
        restored.loadNBTData(saved);
        assertEquals(2.0, restored.getStatInstance(legacyId).getActualValue());
        assertEquals(original.getMultiplierEntries().snapshot(), restored.getMultiplierEntries().snapshot());
        assertEquals(original.getMultiplierRules().snapshot(), restored.getMultiplierRules().snapshot());

        PlayerStatModifier cloned = new PlayerStatModifier();
        cloned.copyFrom(restored, null);
        assertEquals(restored.getMultiplierEntries().snapshot(), cloned.getMultiplierEntries().snapshot());
        assertEquals(restored.getMultiplierRules().snapshot(), cloned.getMultiplierRules().snapshot());

        MultiplierEntries copiedEntries = new MultiplierEntries();
        copiedEntries.copyFrom(restored.getMultiplierEntries());
        MultiplierRules copiedRules = new MultiplierRules();
        copiedRules.copyFrom(restored.getMultiplierRules());
        assertEquals(restored.getMultiplierEntries().snapshot(), copiedEntries.snapshot());
        assertEquals(restored.getMultiplierRules().snapshot(), copiedRules.snapshot());
    }

    @Test
    void reconciliationPreservesLegacyAndForeignModifiers() {
        MultiplierEntries entries = new MultiplierEntries();
        AttributeInstance attribute = attribute();
        UUID legacyId = UUID.randomUUID();
        attribute.addPermanentModifier(new AttributeModifier(legacyId, "legacy", 2.0,
                AttributeModifier.Operation.ADDITION));
        entries.add(BLOOD_RANGE, 2);
        entries.reconcile(ignored -> attribute, true, new MultiplierRules());
        assertEquals(7.68, attribute.getValue(), 1.0E-12);
        entries.remove(BLOOD_RANGE.id(), 2);
        entries.reconcile(ignored -> attribute, false, new MultiplierRules());
        assertEquals(12.0, attribute.getValue(), 1.0E-12);
        assertFalse(attribute.getModifier(legacyId) == null);

        attribute.addPermanentModifier(new AttributeModifier(BLOOD_RANGE.uuid(), "foreign", 0.5,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        entries.add(BLOOD_RANGE, 1);
        entries.reconcile(ignored -> attribute, false, new MultiplierRules());
        assertEquals("foreign", attribute.getModifier(BLOOD_RANGE.uuid()).getName());
    }

    @Test
    void invalidDefinitionsAndSavedDataDoNotReplaceExistingState() {
        MultiplierEntries entries = new MultiplierEntries();
        entries.add(BLOOD_RANGE, 1);
        entries.add(new MultiplierEntry(BLOOD_RANGE.id(), UUID.randomUUID(), BLOOD, ATTRIBUTE, -0.2,
                AttributeModifier.Operation.MULTIPLY_TOTAL), 1);
        entries.add(new MultiplierEntry(ResourceLocation.parse("isaac_disaster:test_other"),
                BLOOD_RANGE.uuid(), BLOOD, ATTRIBUTE, -0.2,
                AttributeModifier.Operation.MULTIPLY_TOTAL), 1);
        entries.add(new MultiplierEntry(BLOOD_RANGE.id(), BLOOD_RANGE.uuid(), BLOOD, ATTRIBUTE, Double.NaN,
                AttributeModifier.Operation.MULTIPLY_TOTAL), 1);
        assertEquals(1, entries.snapshot().get(BLOOD_RANGE.id()).stacks());

        ListTag saved = entries.save();
        CompoundTag invalid = saved.getCompound(0).copy();
        invalid.putString("uuid", "invalid");
        saved.add(invalid);
        CompoundTag malformedId = saved.getCompound(0).copy();
        malformedId.putString("id", "%%%invalid%%%");
        saved.add(malformedId);
        MultiplierEntries loaded = new MultiplierEntries();
        loaded.load(saved);
        assertEquals(entries.snapshot(), loaded.snapshot());

        entries.remove(BLOOD_RANGE.id(), 2);
        assertTrue(entries.snapshot().isEmpty());
    }

    @Test
    void stacksHaveNoArtificialMaximumAndOverflowKeepsExistingValue() {
        MultiplierEntries entries = new MultiplierEntries();
        entries.add(BLOOD_RANGE, Integer.MAX_VALUE);
        entries.add(BLOOD_RANGE, 1);
        assertEquals(Integer.MAX_VALUE, entries.snapshot().get(BLOOD_RANGE.id()).stacks());
        assertEquals(0, new MultiplierEntries.EntryStack(BLOOD_RANGE, -1).stacks());

        MultiplierRules rules = new MultiplierRules();
        MultiplierRules.RuleEntry rule = rule("block_blood", CRICKET, BLOOD_RANGE.id());
        rules.add(rule, Integer.MAX_VALUE);
        rules.add(rule, 1);
        assertEquals(Integer.MAX_VALUE, rules.snapshot().get(rule.id()).stacks());
        assertEquals(0, new MultiplierRules.RuleStack(rule, -1).stacks());
    }

    @Test
    void ruleStacksRejectConflictsAndBadDataWithoutOverwriting() {
        MultiplierRules rules = new MultiplierRules();
        MultiplierRules.RuleEntry rule = rule("block_blood", CRICKET, BLOOD_RANGE.id());
        rules.add(rule, 2);
        rules.add(new MultiplierRules.RuleEntry(rule.id(), BLOOD, rule.targetEntryId()), 1);
        rules.add(rule, 0);
        assertEquals(2, rules.snapshot().get(rule.id()).stacks());
        rules.remove(rule.id(), 1);
        assertTrue(rules.suppressed(BLOOD_RANGE.id()));

        ListTag saved = rules.save();
        CompoundTag invalid = saved.getCompound(0).copy();
        invalid.putString("target", "%%%invalid%%%");
        saved.add(invalid);
        MultiplierRules loaded = new MultiplierRules();
        loaded.load(saved);
        assertEquals(rules.snapshot(), loaded.snapshot());
        loaded.remove(rule.id(), 2);
        assertFalse(loaded.suppressed(BLOOD_RANGE.id()));
    }

    private static AttributeInstance attribute() {
        AttributeInstance attribute = new AttributeInstance(Attributes.ATTACK_DAMAGE, ignored -> {});
        attribute.setBaseValue(10.0);
        return attribute;
    }

    private static MultiplierEntry entry(String name, ResourceLocation source, double amount,
                                         AttributeModifier.Operation operation) {
        ResourceLocation id = ResourceLocation.parse("isaac_disaster:test_" + name);
        UUID uuid = UUID.nameUUIDFromBytes(id.toString().getBytes(StandardCharsets.UTF_8));
        return new MultiplierEntry(id, uuid, source, ATTRIBUTE, amount, operation);
    }

    private static MultiplierRules.RuleEntry rule(String name, ResourceLocation source, ResourceLocation target) {
        return new MultiplierRules.RuleEntry(ResourceLocation.parse("isaac_disaster:test_" + name), source, target);
    }
}
