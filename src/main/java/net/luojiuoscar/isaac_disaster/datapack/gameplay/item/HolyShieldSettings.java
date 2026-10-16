package net.luojiuoscar.isaac_disaster.datapack.gameplay.item;

import com.mojang.serialization.Codec;

public record HolyShieldSettings(double strengthPerStack) {
    private static final HolyShieldSettings DEFAULTS = new HolyShieldSettings(1.0);
    public static final Codec<HolyShieldSettings> CODEC = ItemSettingCodecs.singleDouble(
            "strength_per_stack", HolyShieldSettings::defaults, HolyShieldSettings::new, HolyShieldSettings::strengthPerStack);

    public HolyShieldSettings { ItemSettingCodecs.validate("strength_per_stack", strengthPerStack); }

    public static HolyShieldSettings defaults() { return DEFAULTS; }
}
