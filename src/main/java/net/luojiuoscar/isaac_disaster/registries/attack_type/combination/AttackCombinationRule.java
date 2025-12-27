package net.luojiuoscar.isaac_disaster.registries.attack_type.combination;

import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

/**
 * 特殊的攻击方式组合
 * 优先级与单个攻击方式共享，总会选择优先级更高的攻击方式
 */
public class AttackCombinationRule {
    public final Set<RegistryObject<AttackType>> triggerSet;
    public final RegistryObject<AttackType> result;
    public final double priority;

    public AttackCombinationRule(Set<RegistryObject<AttackType>> triggerSet,
                                 RegistryObject<AttackType> result,
                                 double priority){
        this.triggerSet = triggerSet;
        this.result = result;
        this.priority = priority;
    }

}
