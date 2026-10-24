package net.luojiuoscar.isaac_disaster.registries.attack_type;

/**
 * Built-in attack priority presets used by this mod.
 *
 * <p>Add-on mods can pass raw tier and priority numbers directly when registering their own
 * attack types. This enum is only a readable list of presets for this mod.</p>
 */
public enum AttackPrio {
    // 按 tier 升序排列，同 tier 内按 priority 升序排列，越往下优先级越高。
    BULLET(0, 0),
    NEPTUNUS(0, 100),
    C_SECTION(0, 200),
    LASER(100, 0),
    NEPTUNUS_LASER_COMBO(100, 1),
    C_SECTION_LASER_COMBO(100, 2),
    BRIMSTONE(100, 200),
    HAEMOLACRIA(100, 300),
    NEPTUNUS_HAEMOLACRIA_COMBO(100, 301),
    HAEMOLACRIA_C_SECTION_COMBO(100, 302),
    BRIMSTONE_HAEMOLACRIA_C_SECTION_COMBO(100, 303),
    CURSED_EYE(200, 0),
    NEPTUNUS_CURSED_EYE_COMBO(200, 1),
    C_SECTION_CURSED_EYE_COMBO(200, 2),
    BRIMSTONE_CURSED_EYE_COMBO(200, 3),
    MONSTROS_LUNG(200, 300),
    C_SECTION_MONSTROS_LUNG_COMBO(200, 301),
    BRIMSTONE_MONSTROS_LUNG_COMBO(200, 302),
    MOMS_KNIFE(300, 0),
    MOMS_KNIFE_C_SECTION_COMBO(300, 1),
    BRIMSTONE_MOMS_KNIFE_C_SECTION_COMBO(300, 2);

    private final int tier;
    private final double priority;

    AttackPrio(int tier, double priority){
        this.tier = tier;
        this.priority = priority;
    }

    public int getTier() {
        return tier;
    }

    public double getPriority() {
        return priority;
    }
}
