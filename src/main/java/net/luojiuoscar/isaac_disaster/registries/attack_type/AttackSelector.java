package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.registries.attack_type.combination.AttackCombinationRule;
import net.luojiuoscar.isaac_disaster.registries.attack_type.combination.ModCombinationRules;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.AdditionalAttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.BasicAttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.DelegatingAttackType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Rebuilds the player's attack selection when owned attack types or player eligibility change. */
public final class AttackSelector {
    private static final Comparator<AttackCandidate> STRONGEST_FIRST = Comparator
            .<AttackCandidate>comparingInt(AttackCandidate::priorityTier).reversed()
            .thenComparing(Comparator.<AttackCandidate>comparingDouble(AttackCandidate::priority).reversed())
            .thenComparing(Comparator.<AttackCandidate>comparingInt(AttackCandidate::requiredAttackCount).reversed())
            .thenComparing(candidate -> candidate.attackTypeId().toString())
            .thenComparing(candidate -> candidate.ruleId().toString());

    private AttackSelector() {
    }

    public static AttackSelection select(Map<ResourceLocation, Integer> ownedAttackTypes) {
        return select(new AttackSelectionContext(ownedAttackTypes, null));
    }

    public static AttackSelection select(AttackSelectionContext context) {
        AttackType bullet = ModAttackTypes.BULLET.get();
        AttackCandidate fallback = new AttackCandidate(ModAttackTypes.BULLET.getId(), bullet,
                ModAttackTypes.BULLET.getId(), 1, bullet.getPriorityTier(), bullet.getPriority());
        IForgeRegistry<AttackType> registry =
                RegistryManager.ACTIVE.getRegistry(ModAttackTypes.ATTACK_TYPE_KEY);
        List<AttackCandidate> candidates = new ArrayList<>();
        if (registry != null) {
            for (var entry : context.attackTypes().entrySet()) {
                if (entry.getValue() <= 0) continue;
                AttackType attack = registry.getValue(entry.getKey());
                if (attack == null) continue;
                candidates.add(new AttackCandidate(entry.getKey(), attack, entry.getKey(), 1,
                        attack.getPriorityTier(), attack.getPriority()));
            }
            collectCombinationCandidates(context, registry, candidates);
        }
        return selectCandidates(context, fallback, candidates);
    }

    /** Builds a deterministic result from classified candidates and their effective priorities. */
    static AttackSelection selectCandidates(AttackSelectionContext context, AttackCandidate fallback,
                                           List<AttackCandidate> candidates) {
        validateClassification(fallback.attackType());
        List<AttackCandidate> base = new ArrayList<>();
        List<AttackCandidate> delegating = new ArrayList<>();
        Map<String, AttackType> additional = new TreeMap<>();
        List<AttackCandidate> all = new ArrayList<>(candidates);
        all.add(fallback);
        for (AttackCandidate candidate : all) {
            AttackType attack = candidate.attackType();
            validateClassification(attack);
            if (!attack.isActive(context)) continue;
            if (attack instanceof BasicAttackType) base.add(candidate);
            else if (attack instanceof DelegatingAttackType) delegating.add(candidate);
            else if (attack instanceof AdditionalAttackType) additional.put(candidate.attackTypeId().toString(), attack);
        }

        AttackCandidate bestBase = base.stream().min(STRONGEST_FIRST).orElse(fallback);
        List<AttackCandidate> primary = new ArrayList<>(base);
        primary.addAll(delegating);
        // The ordinary bullet remains the executable fallback even if no base is active.
        primary.add(bestBase);
        AttackCandidate bestMain = primary.stream().min(STRONGEST_FIRST).orElse(bestBase);
        return new AttackSelection(bestMain, bestBase, List.copyOf(additional.values()));
    }

    private static void validateClassification(AttackType attack) {
        int categories = (attack instanceof BasicAttackType ? 1 : 0)
                + (attack instanceof DelegatingAttackType ? 1 : 0)
                + (attack instanceof AdditionalAttackType ? 1 : 0);
        if (categories != 1) {
            throw new IllegalStateException("Attack " + attack.getId()
                    + " must implement exactly one of BasicAttackType, DelegatingAttackType, AdditionalAttackType");
        }
    }

    private static void collectCombinationCandidates(AttackSelectionContext context,
                                                     IForgeRegistry<AttackType> attackRegistry,
                                                     List<AttackCandidate> candidates) {
        IForgeRegistry<AttackCombinationRule> rules =
                RegistryManager.ACTIVE.getRegistry(ModCombinationRules.ATTACK_COMBINATION_RULE_KEY);
        if (rules == null) return;
        for (AttackCombinationRule rule : rules.getValues()) {
            if (!rule.matches(context.attackTypes())) continue;
            if (!areRequiredAttacksActive(rule, context, attackRegistry)) continue;
            ResourceLocation resultId = rule.getResultAttackType();
            AttackType result = attackRegistry.getValue(resultId);
            if (result == null) continue;
            candidates.add(new AttackCandidate(resultId, result, rules.getKey(rule),
                    rule.getRequiredAttackCount(), rule.getPriorityTier(), rule.getPriority()));
        }
    }

    private static boolean areRequiredAttacksActive(AttackCombinationRule rule,
                                                   AttackSelectionContext context,
                                                   IForgeRegistry<AttackType> registry) {
        for (ResourceLocation id : rule.getRequiredAttackTypes()) {
            AttackType attack = registry.getValue(id);
            if (attack == null) return false;
            validateClassification(attack);
            if (!attack.isActive(context)) return false;
        }
        return true;
    }
}
