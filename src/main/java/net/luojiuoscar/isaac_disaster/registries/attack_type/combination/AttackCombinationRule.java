package net.luojiuoscar.isaac_disaster.registries.attack_type.combination;

import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Registered rule that creates an attack candidate when all required attack types are owned.
 *
 * <p>The result attack type does not need to be one of the required attack types. This allows a
 * combination such as A + B + C to select an entirely separate registered attack implementation.</p>
 */
public class AttackCombinationRule {
    private final Set<ResourceLocation> requiredAttackTypes;
    private final ResourceLocation resultAttackType;
    private final int priorityTier;
    private final double priority;

    /**
     * Creates a rule from local registry objects and raw priority numbers.
     *
     * <p>This constructor keeps the original mod registration style available while internally
     * storing ids, so the selector can treat local and add-on attack types the same way.</p>
     */
    public AttackCombinationRule(Set<RegistryObject<AttackType>> triggerSet,
                                 RegistryObject<AttackType> result,
                                 int priorityTier,
                                 double priority){
        this(triggerSet.stream().map(RegistryObject::getId).collect(Collectors.toSet()),
                result.getId(), priorityTier, priority);
    }

    /**
     * Creates a rule from attack type ids and raw priority numbers.
     */
    public AttackCombinationRule(Set<ResourceLocation> requiredAttackTypes,
                                 ResourceLocation resultAttackType,
                                 int priorityTier,
                                 double priority) {
        this.requiredAttackTypes = new HashSet<>(requiredAttackTypes);
        this.resultAttackType = resultAttackType;
        this.priorityTier = priorityTier;
        this.priority = priority;
    }

    public AttackCombinationRule(Set<RegistryObject<AttackType>> triggerSet,
                                 RegistryObject<AttackType> result,
                                 double priority){
        this(triggerSet, result, 0, priority);
    }

    public AttackCombinationRule(Set<ResourceLocation> requiredAttackTypes,
                                 ResourceLocation resultAttackType,
                                 double priority) {
        this(requiredAttackTypes, resultAttackType, 0, priority);
    }

    /**
     * Returns whether all required attack types are currently owned.
     */
    public boolean matches(Map<ResourceLocation, Integer> ownedAttackTypes) {
        return requiredAttackTypes.stream()
                .allMatch(id -> ownedAttackTypes.getOrDefault(id, 0) > 0);
    }

    /**
     * Returns the number of required attack types, used as a deterministic tie-breaker.
     */
    public int getRequiredAttackCount() {
        return requiredAttackTypes.size();
    }

    /**
     * Returns a copy of the ids required by this combination rule.
     */
    public Set<ResourceLocation> getRequiredAttackTypes() {
        return new HashSet<>(requiredAttackTypes);
    }

    /**
     * Returns the attack type id produced by this rule.
     */
    public ResourceLocation getResultAttackType() {
        return resultAttackType;
    }

    /**
     * Returns the candidate priority tier used by the attack selector.
     */
    public int getPriorityTier() {
        return priorityTier;
    }

    /**
     * Returns the candidate priority value inside its tier.
     */
    public double getPriority() {
        return priority;
    }
}
