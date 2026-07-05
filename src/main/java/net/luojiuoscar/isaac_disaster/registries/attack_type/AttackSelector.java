package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.registries.attack_type.combination.AttackCombinationRule;
import net.luojiuoscar.isaac_disaster.registries.attack_type.combination.ModCombinationRules;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Builds and ranks attack candidates for a player ability cache refresh.
 *
 * <p>This selector is intentionally designed for cache rebuilds rather than per-shot work. It
 * scans registered attack types and combination rules only when the player's owned attack type map
 * changes or when existing saved data needs to be refreshed.</p>
 */
public final class AttackSelector {
    private static final Comparator<AttackCandidate> STRONGEST_FIRST = Comparator
            .<AttackCandidate>comparingInt(AttackCandidate::priorityTier).reversed()
            .thenComparing(Comparator.<AttackCandidate>comparingDouble(AttackCandidate::priority).reversed())
            .thenComparing(Comparator.<AttackCandidate>comparingInt(AttackCandidate::requiredAttackCount).reversed())
            .thenComparing(candidate -> candidate.attackTypeId().toString())
            .thenComparing(candidate -> candidate.ruleId().toString());

    private AttackSelector() {
    }

    /**
     * Selects the strongest deterministic attack candidate from currently owned attack types.
     *
     * @param ownedAttackTypes attack type id to owned count map
     * @return chosen attack type, falling back to the base bullet attack when registries are not ready
     */
    public static AttackSelection select(Map<ResourceLocation, Integer> ownedAttackTypes) {
        return select(new AttackSelectionContext(ownedAttackTypes, null));
    }

    /**
     * Selects the strongest deterministic attack candidate from a player-aware context.
     */
    public static AttackSelection select(AttackSelectionContext context) {
        Map<ResourceLocation, Integer> ownedAttackTypes = context.attackTypes();
        IForgeRegistry<AttackType> attackRegistry =
                RegistryManager.ACTIVE.getRegistry(ModAttackType.ATTACK_TYPE_KEY);

        AttackType fallbackAttack = ModAttackType.BULLET.get();
        ResourceLocation fallbackId = ModAttackType.BULLET.getId();
        if (attackRegistry == null) {
            return new AttackSelection(fallbackId, fallbackAttack,
                    fallbackAttack.getPriorityTier(), fallbackAttack.getPriority());
        }

        List<AttackCandidate> candidates = new ArrayList<>();
        candidates.add(new AttackCandidate(fallbackId, fallbackAttack, fallbackId, 1,
                fallbackAttack.getPriorityTier(), fallbackAttack.getPriority()));
        collectSingleAttackCandidates(ownedAttackTypes, attackRegistry, candidates);
        collectCombinationCandidates(context, attackRegistry, candidates);

        Optional<AttackCandidate> best = candidates.stream()
                .filter(candidate -> candidate.attackType().isActive(context))
                .min(STRONGEST_FIRST);

        AttackCandidate selected = best.orElse(candidates.get(0));
        return new AttackSelection(selected.attackTypeId(), selected.attackType(),
                selected.priorityTier(), selected.priority());
    }

    /**
     * Picks a lower-priority attack type for delegating attacks such as Neptunus or Cursed Eye.
     */
    public static AttackType pickLowerAttackType(AttackType source, Map<ResourceLocation, Integer> ownedAttackTypes,
                                                 int index) {
        return pickLowerAttackType(source, new AttackSelectionContext(ownedAttackTypes, null), index);
    }

    /**
     * Picks a lower-priority attack type for delegating attacks from a player-aware context.
     */
    public static AttackType pickLowerAttackType(AttackType source, AttackSelectionContext context, int index) {
        return pickLowerAttackType(source.getPriorityTier(), source.getPriority(), context, index);
    }

    /**
     * Picks a lower-priority non-delegating attack type from an explicit priority position.
     *
     * <p>Delegating attacks pass the selected candidate priority here. This keeps a combination
     * result such as Neptunus + Laser from falling back to Neptunus' base bullet-tier priority when
     * it decides which concrete attack should be fired.</p>
     */
    public static AttackType pickLowerAttackType(int sourcePriorityTier, double sourcePriority,
                                                 AttackSelectionContext context, int index) {
        Map<ResourceLocation, Integer> ownedAttackTypes = context.attackTypes();
        IForgeRegistry<AttackType> attackRegistry =
                RegistryManager.ACTIVE.getRegistry(ModAttackType.ATTACK_TYPE_KEY);
        if (attackRegistry == null) return ModAttackType.BULLET.get();

        List<AttackCandidate> candidates = new ArrayList<>();
        collectSingleAttackCandidates(ownedAttackTypes, attackRegistry, candidates);
        collectCombinationCandidates(context, attackRegistry, candidates);
        candidates.add(new AttackCandidate(ModAttackType.BULLET.getId(), ModAttackType.BULLET.get(),
                ModAttackType.BULLET.getId(), 1, ModAttackType.BULLET.get().getPriorityTier(),
                ModAttackType.BULLET.get().getPriority()));

        List<AttackCandidate> lowerCandidates = candidates.stream()
                .filter(candidate -> comparePriority(candidate.priorityTier(), candidate.priority(),
                        sourcePriorityTier, sourcePriority) < 0)
                .filter(candidate -> !(candidate.attackType() instanceof DelegatingAttackType))
                .filter(candidate -> candidate.attackType().isActive(context))
                .sorted(STRONGEST_FIRST)
                .toList();

        if (index >= 0 && index < lowerCandidates.size()) {
            return lowerCandidates.get(index).attackType();
        }
        return ModAttackType.BULLET.get();
    }

    private static void collectSingleAttackCandidates(Map<ResourceLocation, Integer> ownedAttackTypes,
                                                      IForgeRegistry<AttackType> attackRegistry,
                                                      List<AttackCandidate> candidates) {
        for (Map.Entry<ResourceLocation, Integer> entry : ownedAttackTypes.entrySet()) {
            if (entry.getValue() <= 0) continue;

            ResourceLocation id = entry.getKey();
            AttackType attackType = attackRegistry.getValue(id);
            if (attackType == null) continue;

            candidates.add(new AttackCandidate(id, attackType, id, 1,
                    attackType.getPriorityTier(), attackType.getPriority()));
        }
    }

    private static void collectCombinationCandidates(AttackSelectionContext context,
                                                     IForgeRegistry<AttackType> attackRegistry,
                                                     List<AttackCandidate> candidates) {
        Map<ResourceLocation, Integer> ownedAttackTypes = context.attackTypes();
        IForgeRegistry<AttackCombinationRule> combinationRegistry =
                RegistryManager.ACTIVE.getRegistry(ModCombinationRules.ATTACK_COMBINATION_RULE_KEY);
        if (combinationRegistry == null) return;

        for (AttackCombinationRule rule : combinationRegistry.getValues()) {
            if (!rule.matches(ownedAttackTypes)) continue;
            if (!areRequiredAttacksActive(rule, context, attackRegistry)) continue;

            ResourceLocation resultId = rule.getResultAttackType();
            AttackType result = attackRegistry.getValue(resultId);
            if (result == null) continue;

            ResourceLocation ruleId = combinationRegistry.getKey(rule);
            if (ruleId == null) ruleId = resultId;

            candidates.add(new AttackCandidate(resultId, result, ruleId,
                    rule.getRequiredAttackCount(), rule.getPriorityTier(), rule.getPriority()));
        }
    }

    private static int comparePriority(int leftTier, double leftPriority, int rightTier, double rightPriority) {
        int tierCompare = Integer.compare(leftTier, rightTier);
        if (tierCompare != 0) return tierCompare;
        return Double.compare(leftPriority, rightPriority);
    }

    private static boolean areRequiredAttacksActive(AttackCombinationRule rule,
                                                    AttackSelectionContext context,
                                                    IForgeRegistry<AttackType> attackRegistry) {
        for (ResourceLocation requiredId : rule.getRequiredAttackTypes()) {
            AttackType required = attackRegistry.getValue(requiredId);
            if (required == null || !required.isActive(context)) return false;
        }
        return true;
    }
}
