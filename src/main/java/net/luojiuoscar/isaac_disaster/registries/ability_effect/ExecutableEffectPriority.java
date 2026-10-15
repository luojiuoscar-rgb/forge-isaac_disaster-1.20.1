package net.luojiuoscar.isaac_disaster.registries.ability_effect;

public enum ExecutableEffectPriority {
    HIGHEST(100),
    HIGHER(50),
    HIGH(25),
    MEDIUM(0),
    LOW(-25),
    LOWER(-50),

    /** 乘法计算 */
    LOWEST(-100);

    public final double priority;

    ExecutableEffectPriority(double priority){
        this.priority = priority;
    }
}
