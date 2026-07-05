package net.luojiuoscar.isaac_disaster.registries.attack_type;

/**
 * Built-in attack priority presets used by this mod.
 *
 * <p>Add-on mods can pass raw tier and priority numbers directly when registering their own
 * attack types. This enum is only a readable list of presets for this mod.</p>
 */
public enum AttackPrio {
    BULLET(0, 0),
    NEPTUNUS(0, 100),
    C_SECTION(0, 200),
    LASER(100, 0),
    NEPTUNUS_LASER_COMBO(100, 50),
    C_SECTION_LASER_COMBO(100, 100),
    BRIMSTONE(100, 200),
    CURSED_EYE(200, 0),
    NEPTUNUS_CURSED_EYE_COMBO(200, 1);

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
